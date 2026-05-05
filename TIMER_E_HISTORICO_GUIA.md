# 🎮 Guia de Implementação: Timer e Histórico

Este documento explica como o sistema genérico de **Timer** e **Histórico** foi implementado e como integrá-lo aos jogos existentes.

## 📋 Resumo das Mudanças

### ✅ Criado

1. **Infraestrutura de Timer**
   - `TimerConfig` - Configuração personalizável (tempo inicial + incremento)
   - `GameTimer` - Estado do timer em tempo real
   - `TimerService` - Gerenciador de timers com scheduling
   - `JogoComTimer` - Interface para jogos com timer
   - `TimerResponse` - DTO para WebSocket/HTTP

2. **Infraestrutura de Histórico**
   - `JogoComHistorico<T>` - Interface genérica para histórico
   - `GerenciadorHistorico<T>` - Classe utilitária para gerenciar histórico por jogador

3. **Modelos Atualizados**
   - `TicTacToeSala` - Adicionado `timerConfig` e `historicoPorUsername`
   - `JokenpoSala` - Adicionado `timerConfig` e `historicoPorUsername`

## 🎯 Funcionalidades

### Timer Personalizável

O dono da sala pode configurar:
- **Tempo inicial** para cada jogador (ex: 300 segundos = 5 minutos)
- **Incremento por lance** (ex: 3 segundos adicionados após cada jogada - estilo Fischer)
- **Ativar/desativar** o timer

### Histórico Completo

- Cada jogador vê apenas as partidas em que participou
- Histórico ordenado (mais recente primeiro)
- Suporte a histórico completo da sala
- Dono pode limpar o histórico

## 🔧 Como Integrar em um Jogo

### Exemplo: TicTacToe com Timer

#### 1. O modelo já foi atualizado

```java
public class TicTacToeSala extends Sala {
    // ... campos existentes ...
    
    @Builder.Default
    private TimerConfig timerConfig = TimerConfig.semTimer();
    
    @Builder.Default
    private Map<String, List<TicTacToe>> historicoPorUsername = new HashMap<>();
}
```

#### 2. Atualizar o Manager

```java
@Component
public class TicTacToeManager implements JogoPort, JogoComTimer, JogoComHistorico<TicTacToe> {
    
    private final TicTacToeRepository ticTacToeRepository;
    private final TimerService timerService;
    private final TicTacToeWebSocketSender sender; // para enviar updates
    
    // ===== MÉTODOS DE TIMER =====
    
    @Override
    public TimerConfig obterConfigTimer(String nomeSala, String usernameDono) {
        TicTacToeSala sala = ticTacToeRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        return sala != null ? sala.getTimerConfig() : TimerConfig.semTimer();
    }
    
    @Override
    public void configurarTimer(String nomeSala, String usernameDono, String username, TimerConfig config) {
        TicTacToeSala sala = ticTacToeRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        
        // Validar que é o dono
        if (!usernameDono.equals(username)) {
            throw new UsuarioNaoAutorizado("Apenas o dono pode configurar o timer");
        }
        
        // Validar que não há jogo em andamento
        if (sala.getJogoAtual() != null) {
            throw new ErroDeRequisicaoGeral("Não é possível alterar timer com jogo em andamento");
        }
        
        // Validar configuração
        if (!config.isValida()) {
            throw new ErroDeRequisicaoGeral("Configuração de timer inválida");
        }
        
        sala.setTimerConfig(config);
        ticTacToeRepository.salvar(sala);
    }
    
    @Override
    public void processarTimeout(String nomeSala, String usernameDono, String usernameJogadorTimeout) {
        TicTacToeSala sala = ticTacToeRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        TicTacToe jogo = sala.getJogoAtual();
        
        if (jogo == null) {
            return; // Jogo já encerrado
        }
        
        // Determinar vencedor (o outro jogador)
        boolean timeoutEraX = jogo.getX().equals(usernameJogadorTimeout);
        jogo.setStatus(timeoutEraX ? TicTacToeStatus.o_WIN : TicTacToeStatus.x_WIN);
        
        // Arquivar partida
        arquivarPartida(sala, jogo);
        
        // Enviar notificação
        // enviarParaTodos(sala, jogo, "TIMEOUT");
    }
    
    // ===== MÉTODOS DE HISTÓRICO =====
    
    @Override
    public List<TicTacToe> obterHistorico(String nomeSala, String usernameDono, String username) {
        TicTacToeSala sala = ticTacToeRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        GerenciadorHistorico<TicTacToe> gerenciador = new GerenciadorHistorico<>(sala.getHistoricoPorUsername());
        return gerenciador.obterHistorico(username);
    }
    
    @Override
    public List<TicTacToe> obterHistoricoCompleto(String nomeSala, String usernameDono) {
        TicTacToeSala sala = ticTacToeRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        GerenciadorHistorico<TicTacToe> gerenciador = new GerenciadorHistorico<>(sala.getHistoricoPorUsername());
        return gerenciador.obterHistoricoCompleto();
    }
    
    @Override
    public void limparHistorico(String nomeSala, String usernameDono, String username) {
        TicTacToeSala sala = ticTacToeRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        
        if (!usernameDono.equals(username)) {
            throw new UsuarioNaoAutorizado("Apenas o dono pode limpar o histórico");
        }
        
        sala.getHistoricoPorUsername().clear();
        sala.getHistorico().clear();
        ticTacToeRepository.salvar(sala);
    }
    
    // ===== INTEGRAÇÃO NO FLUXO DO JOGO =====
    
    public TicTacToeSala comecar(Sala sala) {
        // ... código existente para criar jogo ...
        
        TicTacToe ticTacToe = TicTacToe.builder()
                .x(x)
                .o(o)
                .posicao("_________")
                .status(TicTacToeStatus.x_TURN)
                .build();
        ticTacToeSala.setJogoAtual(ticTacToe);
        this.ticTacToeRepository.salvar(ticTacToeSala);
        
        // NOVO: Iniciar timer se configurado
        if (ticTacToeSala.getTimerConfig().getTimerAtivo()) {
            iniciarTimer(ticTacToeSala, x);
        }
        
        return ticTacToeSala;
    }
    
    public TicTacToe lance(TicTacToeLance lance, String username, Sala sala) {
        // ... código existente para processar lance ...
        
        // NOVO: Trocar turno do timer
        if (ticTacToeSala.getTimerConfig().getTimerAtivo()) {
            String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
            timerService.trocarTurno(salaId);
        }
        
        // Se jogo terminou
        if (jogoTerminou) {
            // NOVO: Arquivar no histórico por jogador
            arquivarPartida(ticTacToeSala, jogoAtual);
            
            // NOVO: Remover timer
            if (ticTacToeSala.getTimerConfig().getTimerAtivo()) {
                String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
                timerService.removerTimer(salaId);
            }
        }
        
        return jogoAtual;
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    private void iniciarTimer(TicTacToeSala sala, String primeiroJogador) {
        String salaId = gerarSalaId(sala.getNomeSala(), sala.getUsernameDono());
        
        GameTimer timer = GameTimer.criar(
            salaId,
            sala.getUsernameDono(),
            sala.getUsernameOponente(),
            sala.getTimerConfig()
        );
        
        timerService.registrarTimer(
            timer,
            username -> processarTimeout(sala.getNomeSala(), sala.getUsernameDono(), username),
            t -> enviarAtualizacaoTimer(sala, t)
        );
        
        timerService.iniciarTimer(salaId, primeiroJogador);
    }
    
    private void enviarAtualizacaoTimer(TicTacToeSala sala, GameTimer timer) {
        TimerResponse response = TimerResponse.fromGameTimer(timer);
        // sender.enviarTimerParaTodos(sala, response);
    }
    
    private void arquivarPartida(TicTacToeSala sala, TicTacToe jogo) {
        // Histórico simples (compatibilidade)
        sala.setJogoAtual(null);
        sala.getHistorico().add(jogo);
        
        // Histórico por jogador
        GerenciadorHistorico<TicTacToe> gerenciador = new GerenciadorHistorico<>(sala.getHistoricoPorUsername());
        gerenciador.adicionarPartidaParaJogadores(jogo, jogo.getX(), jogo.getO());
        sala.setHistoricoPorUsername(gerenciador.obterMapa());
        
        ticTacToeRepository.salvar(sala);
    }
}
```

#### 3. Adicionar Endpoints no Controller

```java
@RestController
@RequestMapping("/api/tictactoe")
public class TicTacToeController {
    
    private final TicTacToeManager manager;
    
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
        
        manager.configurarTimer(nomeSala, usernameDono, principal.getName(), config);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{nomeSala}/timer")
    public ResponseEntity<TimerConfig> obterConfigTimer(
        @PathVariable String nomeSala,
        @RequestParam String usernameDono
    ) {
        TimerConfig config = manager.obterConfigTimer(nomeSala, usernameDono);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/{nomeSala}/historico")
    public ResponseEntity<List<TicTacToe>> obterHistorico(
        @PathVariable String nomeSala,
        @RequestParam String usernameDono,
        Principal principal
    ) {
        List<TicTacToe> historico = manager.obterHistorico(nomeSala, usernameDono, principal.getName());
        return ResponseEntity.ok(historico);
    }
    
    @DeleteMapping("/{nomeSala}/historico")
    public ResponseEntity<?> limparHistorico(
        @PathVariable String nomeSala,
        @RequestParam String usernameDono,
        Principal principal
    ) {
        manager.limparHistorico(nomeSala, usernameDono, principal.getName());
        return ResponseEntity.ok().build();
    }
}
```

## 📊 Exemplos de Uso

### Configurar Timer (Frontend)

```typescript
// Configurar 5 minutos + 3 segundos por lance
const config = {
  timerAtivo: true,
  tempoInicialSegundos: 300,
  incrementoPorLanceSegundos: 3
};

await http.post(`/api/tictactoe/${nomeSala}/configurar-timer?usernameDono=${dono}`, config);
```

### Receber Atualizações de Timer (WebSocket)

```typescript
stompClient.subscribe(`/topic/tictactoe/${dono}/${nomeSala}/timer`, (message) => {
  const timerUpdate = JSON.parse(message.body);
  
  console.log('Tempo Jogador 1:', timerUpdate.tempoRestanteJogador1Ms / 1000, 's');
  console.log('Tempo Jogador 2:', timerUpdate.tempoRestanteJogador2Ms / 1000, 's');
  console.log('Turno atual:', timerUpdate.jogadorAtual);
  
  if (timerUpdate.tempoEsgotado) {
    console.log('Timeout!', timerUpdate.jogadorQuePerderPorTempo, 'perdeu por tempo');
  }
});
```

### Ver Histórico

```typescript
const historico = await http.get(`/api/tictactoe/${nomeSala}/historico?usernameDono=${dono}`);
console.log('Minhas partidas:', historico.data);
```

## 🎮 Configurações Sugeridas por Jogo

### TicTacToe
```java
TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(60L)  // 1 minuto
    .incrementoPorLanceSegundos(2L)
    .build();
```

### Jokenpo
```java
TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(15L)  // 15 segundos
    .incrementoPorLanceSegundos(0L)  // Sem incremento
    .build();
```

### Xadrez
```java
TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(600L)  // 10 minutos
    .incrementoPorLanceSegundos(5L)
    .build();
```

### Coup
```java
TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(90L)  // 1.5 minutos
    .incrementoPorLanceSegundos(3L)
    .build();
```

## ✅ Checklist de Integração

Para integrar timer e histórico em um jogo:

- [ ] Adicionar `timerConfig` e `historicoPorUsername` ao modelo da sala
- [ ] Implementar `JogoComTimer` no Manager
- [ ] Implementar `JogoComHistorico<T>` no Manager
- [ ] Injetar `TimerService` no Manager
- [ ] Chamar `iniciarTimer()` ao começar partida
- [ ] Chamar `trocarTurno()` após cada lance
- [ ] Chamar `removerTimer()` ao encerrar partida
- [ ] Usar `GerenciadorHistorico` para arquivar partidas
- [ ] Adicionar endpoints de configuração no Controller
- [ ] Implementar envio de atualizações via WebSocket
- [ ] Atualizar frontend para mostrar timer
- [ ] Atualizar frontend para mostrar histórico

## 🚀 Próximos Passos

1. Integrar timer e histórico no **TicTacToe**
2. Integrar timer e histórico no **Jokenpo**
3. Atualizar **Xadrez** para usar o novo sistema (já tem histórico próprio)
4. Integrar nos demais jogos (Coup, Who is the Impostor)
5. Criar testes unitários
6. Documentar API no Swagger
