# Sistema Genérico de Timer e Histórico

Este pacote fornece infraestrutura reutilizável para adicionar **timer por turno** e **histórico completo de partidas** a qualquer jogo.

## 📦 Componentes

### Timer

#### `TimerConfig`
Configuração personalizável do timer:
- `tempoInicialSegundos`: Tempo inicial para cada jogador (ex: 300 = 5 minutos)
- `incrementoPorLanceSegundos`: Segundos adicionados após cada lance (ex: 3 = estilo Fischer)
- `timerAtivo`: Se o timer está ativo ou não

#### `GameTimer`
Estado do timer em tempo real:
- Mantém tempo restante de cada jogador
- Controla turno atual
- Detecta timeouts automaticamente
- Adiciona incremento após cada lance

#### `TimerService`
Serviço Spring que gerencia todos os timers:
- Verifica timeouts a cada 100ms
- Envia atualizações via WebSocket a cada 1 segundo
- Gerencia callbacks de timeout e update

#### `JogoComTimer`
Interface para jogos que usam timer:
```java
public interface JogoComTimer {
    TimerConfig obterConfigTimer(String nomeSala, String usernameDono);
    void configurarTimer(String nomeSala, String usernameDono, String username, TimerConfig config);
    void processarTimeout(String nomeSala, String usernameDono, String usernameJogadorTimeout);
}
```

### Histórico

#### `JogoComHistorico<T>`
Interface para jogos com histórico completo:
```java
public interface JogoComHistorico<T> {
    List<T> obterHistorico(String nomeSala, String usernameDono, String username);
    List<T> obterHistoricoCompleto(String nomeSala, String usernameDono);
    void limparHistorico(String nomeSala, String usernameDono, String username);
}
```

#### `GerenciadorHistorico<T>`
Classe utilitária para gerenciar histórico:
- Mantém histórico por jogador
- Evita duplicatas
- Retorna em ordem decrescente (mais recente primeiro)

## 🚀 Como Usar

### 1. Adicionar Timer ao Jogo

#### Passo 1: Atualizar o modelo da sala

```java
@Data
@SuperBuilder
public class MeuJogoSala extends Sala {
    // ... campos existentes ...
    
    @Builder.Default
    private TimerConfig timerConfig = TimerConfig.semTimer();
    
    @Builder.Default
    private Map<String, List<MeuJogo>> historicoPorUsername = new HashMap<>();
}
```

#### Passo 2: Implementar `JogoComTimer` no Manager

```java
@Service
public class MeuJogoManager implements JogoPort, JogoComTimer {
    
    private final TimerService timerService;
    private final MeuJogoRepository repository;
    
    @Override
    public TimerConfig obterConfigTimer(String nomeSala, String usernameDono) {
        MeuJogoSala sala = repository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        return sala != null ? sala.getTimerConfig() : TimerConfig.semTimer();
    }
    
    @Override
    public void configurarTimer(String nomeSala, String usernameDono, String username, TimerConfig config) {
        // Validar que é o dono
        // Validar que não há partida em andamento
        MeuJogoSala sala = repository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        sala.setTimerConfig(config);
        repository.salvar(sala);
    }
    
    @Override
    public void processarTimeout(String nomeSala, String usernameDono, String usernameJogadorTimeout) {
        // Processar derrota por tempo
        MeuJogoSala sala = repository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        MeuJogo jogo = sala.getJogoAtual();
        
        // Determinar vencedor (o outro jogador)
        String vencedor = jogo.getJogador1().equals(usernameJogadorTimeout) 
            ? jogo.getJogador2() 
            : jogo.getJogador1();
        
        jogo.setStatus(MeuJogoStatus.TIMEOUT);
        jogo.setVencedor(vencedor);
        
        // Arquivar partida
        sala.setJogoAtual(null);
        sala.getHistorico().add(jogo);
        repository.salvar(sala);
        
        // Enviar notificação via WebSocket
        enviarParaTodos(sala, jogo, "TIMEOUT");
    }
}
```

#### Passo 3: Iniciar timer ao começar partida

```java
public MeuJogoSala comecar(Sala sala) {
    MeuJogoSala meuJogoSala = repository.findByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
    
    // ... criar jogo ...
    
    // Iniciar timer se configurado
    if (meuJogoSala.getTimerConfig().getTimerAtivo()) {
        String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
        GameTimer timer = GameTimer.criar(
            salaId,
            jogo.getJogador1(),
            jogo.getJogador2(),
            meuJogoSala.getTimerConfig()
        );
        
        timerService.registrarTimer(
            timer,
            username -> processarTimeout(sala.getNome(), sala.getUsernameDono(), username),
            t -> enviarAtualizacaoTimer(sala, t)
        );
        
        timerService.iniciarTimer(salaId, jogo.getJogador1());
    }
    
    return meuJogoSala;
}
```

#### Passo 4: Trocar turno após cada lance

```java
public MeuJogo lance(MeuJogoLance lance, String username, Sala sala) {
    // ... processar lance ...
    
    // Trocar turno do timer
    if (meuJogoSala.getTimerConfig().getTimerAtivo()) {
        String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
        timerService.trocarTurno(salaId);
    }
    
    return jogo;
}
```

#### Passo 5: Pausar/remover timer ao encerrar

```java
private void encerrarPartida(Sala sala) {
    String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
    timerService.removerTimer(salaId);
}
```

### 2. Adicionar Histórico Completo

#### Passo 1: Implementar `JogoComHistorico`

```java
@Service
public class MeuJogoManager implements JogoPort, JogoComHistorico<MeuJogo> {
    
    @Override
    public List<MeuJogo> obterHistorico(String nomeSala, String usernameDono, String username) {
        MeuJogoSala sala = repository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        GerenciadorHistorico<MeuJogo> gerenciador = new GerenciadorHistorico<>(sala.getHistoricoPorUsername());
        return gerenciador.obterHistorico(username);
    }
    
    @Override
    public List<MeuJogo> obterHistoricoCompleto(String nomeSala, String usernameDono) {
        MeuJogoSala sala = repository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        GerenciadorHistorico<MeuJogo> gerenciador = new GerenciadorHistorico<>(sala.getHistoricoPorUsername());
        return gerenciador.obterHistoricoCompleto();
    }
    
    @Override
    public void limparHistorico(String nomeSala, String usernameDono, String username) {
        // Validar que é o dono
        MeuJogoSala sala = repository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        sala.getHistoricoPorUsername().clear();
        sala.getHistorico().clear();
        repository.salvar(sala);
    }
}
```

#### Passo 2: Arquivar partidas no histórico

```java
private void arquivarPartida(MeuJogoSala sala, MeuJogo jogo) {
    // Adicionar ao histórico simples (compatibilidade)
    sala.getHistorico().add(jogo);
    
    // Adicionar ao histórico por jogador
    GerenciadorHistorico<MeuJogo> gerenciador = new GerenciadorHistorico<>(sala.getHistoricoPorUsername());
    gerenciador.adicionarPartidaParaJogadores(jogo, jogo.getJogador1(), jogo.getJogador2());
    
    sala.setHistoricoPorUsername(gerenciador.obterMapa());
    repository.salvar(sala);
}
```

## 📝 Exemplo de Configuração via API

### Configurar Timer (Endpoint do Controller)

```java
@PostMapping("/{nomeSala}/configurar-timer")
public ResponseEntity<?> configurarTimer(
    @PathVariable String nomeSala,
    @RequestParam String usernameDono,
    @RequestBody TimerConfigRequest request,
    Principal principal
) {
    TimerConfig config = TimerConfig.builder()
        .timerAtivo(request.getTimerAtivo())
        .tempoInicialSegundos(request.getTempoInicialSegundos())
        .incrementoPorLanceSegundos(request.getIncrementoPorLanceSegundos())
        .build();
    
    meuJogoManager.configurarTimer(nomeSala, usernameDono, principal.getName(), config);
    return ResponseEntity.ok().build();
}
```

### Exemplo de Request

```json
{
  "timerAtivo": true,
  "tempoInicialSegundos": 300,
  "incrementoPorLanceSegundos": 3
}
```

## 🎮 Exemplos de Configuração

### Blitz (3 minutos + 2 segundos)
```java
TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(180L)
    .incrementoPorLanceSegundos(2L)
    .build();
```

### Rapid (10 minutos sem incremento)
```java
TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(600L)
    .incrementoPorLanceSegundos(0L)
    .build();
```

### Bullet (1 minuto + 1 segundo)
```java
TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(60L)
    .incrementoPorLanceSegundos(1L)
    .build();
```

### Sem Timer
```java
TimerConfig.semTimer();
```

## 🔔 WebSocket - Atualizações de Timer

O `TimerService` envia atualizações automáticas via callback. Exemplo de integração:

```java
private void enviarAtualizacaoTimer(Sala sala, GameTimer timer) {
    TimerResponse response = TimerResponse.fromGameTimer(timer);
    
    List<String> jogadores = obterJogadoresDaSala(sala);
    webSocketSender.enviarParaTodos(
        sala.getUsernameDono(),
        sala.getNome(),
        jogadores,
        response
    );
}
```

## ⚠️ Considerações Importantes

1. **Thread Safety**: O `TimerService` usa `ConcurrentHashMap` para ser thread-safe
2. **Precisão**: Verificações a cada 100ms garantem precisão adequada
3. **Performance**: Atualizações WebSocket a cada 1 segundo evitam sobrecarga
4. **Cleanup**: Sempre remover timer ao encerrar partida ou deletar sala
5. **Persistência**: O `GameTimer` não é persistido - é recriado ao reiniciar servidor

## 🧪 Testando

```java
@Test
void testarTimer() {
    TimerConfig config = TimerConfig.builder()
        .timerAtivo(true)
        .tempoInicialSegundos(10L)
        .incrementoPorLanceSegundos(2L)
        .build();
    
    GameTimer timer = GameTimer.criar("sala1", "jogador1", "jogador2", config);
    timer.iniciar("jogador1");
    
    // Simular 5 segundos
    Thread.sleep(5000);
    
    long tempoRestante = timer.calcularTempoRestanteAtual("jogador1");
    assertTrue(tempoRestante < 10000 && tempoRestante > 4000);
    
    timer.trocarTurno();
    
    // Jogador1 deve ter recebido incremento
    long tempoAposIncremento = timer.getTempoRestanteJogador1Ms();
    assertTrue(tempoAposIncremento > 5000); // ~5s + 2s incremento
}
```
