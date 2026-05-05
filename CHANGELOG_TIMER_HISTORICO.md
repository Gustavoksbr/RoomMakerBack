# Changelog - Sistema de Timer e Histórico

## 🎯 Objetivo

Implementar um sistema **genérico e reutilizável** de:
1. **Timer por turno** - Personalizável pelo dono da sala (tempo inicial + incremento)
2. **Histórico completo** - Cada jogador vê suas partidas, ordenadas por data

## 📦 Arquivos Criados

### Timer
```
RoomMakerBack/src/main/java/com/example/roommaker/app/categorias/common/timer/
├── TimerConfig.java                    # Configuração personalizável
├── GameTimer.java                      # Estado do timer em tempo real
├── TimerService.java                   # Gerenciador com scheduling
├── JogoComTimer.java                   # Interface para jogos
├── TimerResponse.java                  # DTO para WebSocket/HTTP
├── TimerSchedulingConfig.java          # Configuração Spring
└── dto/
    └── TimerConfigRequest.java         # DTO para requests HTTP
```

### Histórico
```
RoomMakerBack/src/main/java/com/example/roommaker/app/categorias/common/historico/
├── JogoComHistorico.java               # Interface genérica
└── GerenciadorHistorico.java           # Classe utilitária
```

### Documentação
```
RoomMakerBack/
├── TIMER_E_HISTORICO_GUIA.md           # Guia completo de implementação
├── CHANGELOG_TIMER_HISTORICO.md        # Este arquivo
└── src/main/java/com/example/roommaker/app/categorias/common/
    └── README.md                        # Documentação técnica
```

## 🔧 Arquivos Modificados

### Modelos Atualizados
- `TicTacToeSala.java` - Adicionado `timerConfig` e `historicoPorUsername`
- `JokenpoSala.java` - Adicionado `timerConfig` e `historicoPorUsername`

## ✨ Funcionalidades

### Timer
- ✅ Tempo inicial personalizável por jogador
- ✅ Incremento por lance (estilo Fischer)
- ✅ Detecção automática de timeout
- ✅ Atualizações via WebSocket a cada 1 segundo
- ✅ Verificação de timeout a cada 100ms
- ✅ Thread-safe (ConcurrentHashMap)
- ✅ Callbacks para timeout e updates
- ✅ Pausar/retomar timer
- ✅ Trocar turno automaticamente

### Histórico
- ✅ Histórico por jogador (cada um vê suas partidas)
- ✅ Histórico completo da sala
- ✅ Ordenação decrescente (mais recente primeiro)
- ✅ Evita duplicatas
- ✅ Limpar histórico (apenas dono)
- ✅ Genérico (funciona com qualquer tipo de partida)

## 🎮 Como Usar

### 1. Configurar Timer (Dono da Sala)

```java
TimerConfig config = TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(300L)      // 5 minutos
    .incrementoPorLanceSegundos(3L)   // +3 segundos por lance
    .build();

manager.configurarTimer(nomeSala, usernameDono, username, config);
```

### 2. Implementar no Manager

```java
@Service
public class MeuJogoManager implements JogoPort, JogoComTimer, JogoComHistorico<MeuJogo> {
    
    private final TimerService timerService;
    
    // Implementar métodos das interfaces
    // Ver TIMER_E_HISTORICO_GUIA.md para exemplo completo
}
```

### 3. Integrar no Fluxo do Jogo

```java
// Ao iniciar partida
if (sala.getTimerConfig().getTimerAtivo()) {
    GameTimer timer = GameTimer.criar(salaId, jogador1, jogador2, config);
    timerService.registrarTimer(timer, this::processarTimeout, this::enviarUpdate);
    timerService.iniciarTimer(salaId, primeiroJogador);
}

// Após cada lance
timerService.trocarTurno(salaId);

// Ao encerrar partida
timerService.removerTimer(salaId);
arquivarPartida(sala, jogo);
```

## 📊 Exemplos de Configuração

### Blitz (3 min + 2 seg)
```java
TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(180L)
    .incrementoPorLanceSegundos(2L)
    .build();
```

### Rapid (10 min)
```java
TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(600L)
    .incrementoPorLanceSegundos(0L)
    .build();
```

### Bullet (1 min + 1 seg)
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

## 🔌 Endpoints Sugeridos

```java
POST   /api/{jogo}/{nomeSala}/configurar-timer    # Configurar timer
GET    /api/{jogo}/{nomeSala}/timer                # Obter config timer
GET    /api/{jogo}/{nomeSala}/historico            # Obter histórico do jogador
GET    /api/{jogo}/{nomeSala}/historico/completo   # Obter histórico completo
DELETE /api/{jogo}/{nomeSala}/historico            # Limpar histórico
```

## 📡 WebSocket

### Tópicos Sugeridos
```
/topic/{jogo}/{usernameDono}/{nomeSala}/timer      # Atualizações de timer
/topic/{jogo}/{usernameDono}/{nomeSala}/timeout    # Notificação de timeout
```

### Payload de Timer Update
```json
{
  "usernameJogador1": "player1",
  "usernameJogador2": "player2",
  "tempoRestanteJogador1Ms": 295000,
  "tempoRestanteJogador2Ms": 300000,
  "jogadorAtual": "player1",
  "pausado": false,
  "tempoEsgotado": false,
  "jogadorQuePerderPorTempo": null,
  "config": {
    "timerAtivo": true,
    "tempoInicialSegundos": 300,
    "incrementoPorLanceSegundos": 3
  }
}
```

## 🧪 Testes

### Testar Timer
```java
@Test
void testarTimerComIncremento() {
    TimerConfig config = TimerConfig.builder()
        .timerAtivo(true)
        .tempoInicialSegundos(10L)
        .incrementoPorLanceSegundos(2L)
        .build();
    
    GameTimer timer = GameTimer.criar("sala1", "j1", "j2", config);
    timer.iniciar("j1");
    
    Thread.sleep(5000); // 5 segundos
    
    timer.trocarTurno(); // Adiciona 2s de incremento
    
    long tempo = timer.getTempoRestanteJogador1Ms();
    assertTrue(tempo > 5000 && tempo < 8000); // ~5s + 2s
}
```

### Testar Histórico
```java
@Test
void testarHistoricoPorJogador() {
    GerenciadorHistorico<Partida> gerenciador = new GerenciadorHistorico<>();
    
    Partida p1 = new Partida("j1", "j2");
    Partida p2 = new Partida("j1", "j3");
    
    gerenciador.adicionarPartidaParaJogadores(p1, "j1", "j2");
    gerenciador.adicionarPartidaParaJogadores(p2, "j1", "j3");
    
    List<Partida> historicoJ1 = gerenciador.obterHistorico("j1");
    assertEquals(2, historicoJ1.size());
    
    List<Partida> historicoJ2 = gerenciador.obterHistorico("j2");
    assertEquals(1, historicoJ2.size());
}
```

## 🚀 Próximos Passos

### Fase 1: Integração Básica
- [ ] Integrar timer no TicTacToe
- [ ] Integrar histórico no TicTacToe
- [ ] Criar endpoints no TicTacToeController
- [ ] Testar integração

### Fase 2: Outros Jogos
- [ ] Integrar no Jokenpo
- [ ] Integrar no Coup
- [ ] Integrar no Who is the Impostor
- [ ] Atualizar Xadrez para usar novo sistema

### Fase 3: Frontend
- [ ] Criar componente de timer visual
- [ ] Criar componente de histórico
- [ ] Integrar WebSocket para updates
- [ ] Adicionar configuração de timer na criação de sala

### Fase 4: Melhorias
- [ ] Persistir estado do timer (recuperar após restart)
- [ ] Adicionar estatísticas (vitórias, derrotas, empates)
- [ ] Adicionar filtros no histórico (por data, resultado)
- [ ] Adicionar paginação no histórico
- [ ] Adicionar exportação de histórico (CSV, JSON)

## 📝 Notas Técnicas

### Performance
- Verificação de timeout: 100ms (precisão adequada)
- Atualizações WebSocket: 1 segundo (evita sobrecarga)
- Thread-safe: ConcurrentHashMap para timers ativos

### Limitações
- Timer não é persistido (recriado ao reiniciar servidor)
- Histórico limitado apenas pela memória/banco
- Precisão do timer: ~100ms

### Compatibilidade
- ✅ Compatível com histórico existente (TicTacToe, Jokenpo)
- ✅ Não quebra código existente (campos novos com defaults)
- ✅ Xadrez pode migrar para o novo sistema gradualmente

## 🎓 Referências

- [TIMER_E_HISTORICO_GUIA.md](./TIMER_E_HISTORICO_GUIA.md) - Guia completo de implementação
- [common/README.md](./src/main/java/com/example/roommaker/app/categorias/common/README.md) - Documentação técnica
- Spring `@Scheduled` - https://spring.io/guides/gs/scheduling-tasks/
- Fischer Time Control - https://en.wikipedia.org/wiki/Time_control#Increment_and_delay_methods

## 👥 Autor

Implementado por: Kiro AI Assistant
Data: 2026-05-05
Branch: `upgrade-all-games`
