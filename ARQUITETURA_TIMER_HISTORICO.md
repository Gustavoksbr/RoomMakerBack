# 🏗️ Arquitetura do Sistema de Timer e Histórico

## 📐 Visão Geral

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Configuração │  │ Timer Visual │  │   Histórico  │          │
│  │   de Timer   │  │  (Countdown) │  │   Partidas   │          │
│  └──────┬───────┘  └──────▲───────┘  └──────▲───────┘          │
│         │                  │                  │                  │
└─────────┼──────────────────┼──────────────────┼──────────────────┘
          │                  │                  │
          │ HTTP POST        │ WebSocket        │ HTTP GET
          │                  │ (1s updates)     │
┌─────────▼──────────────────┼──────────────────┼──────────────────┐
│                      BACKEND (Spring Boot)                       │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    CONTROLLER LAYER                         │ │
│  │  ┌──────────────────────────────────────────────────────┐  │ │
│  │  │  TicTacToeController / JokenpoController / etc.      │  │ │
│  │  │  - POST /configurar-timer                            │  │ │
│  │  │  - GET  /timer                                       │  │ │
│  │  │  - GET  /historico                                   │  │ │
│  │  │  - DELETE /historico                                 │  │ │
│  │  └──────────────────────┬───────────────────────────────┘  │ │
│  └─────────────────────────┼──────────────────────────────────┘ │
│                            │                                     │
│  ┌─────────────────────────▼──────────────────────────────────┐ │
│  │                    MANAGER LAYER                            │ │
│  │  ┌──────────────────────────────────────────────────────┐  │ │
│  │  │  TicTacToeManager implements:                        │  │ │
│  │  │    - JogoPort                                        │  │ │
│  │  │    - JogoComTimer                                    │  │ │
│  │  │    - JogoComHistorico<TicTacToe>                    │  │ │
│  │  │                                                       │  │ │
│  │  │  Métodos:                                            │  │ │
│  │  │    - configurarTimer()                               │  │ │
│  │  │    - processarTimeout()                              │  │ │
│  │  │    - obterHistorico()                                │  │ │
│  │  │    - comecar() → iniciarTimer()                      │  │ │
│  │  │    - lance() → trocarTurno()                         │  │ │
│  │  └────────┬─────────────────────┬───────────────────────┘  │ │
│  └───────────┼─────────────────────┼──────────────────────────┘ │
│              │                     │                             │
│              │                     │                             │
│  ┌───────────▼─────────────────────▼──────────────────────────┐ │
│  │              COMMON INFRASTRUCTURE                          │ │
│  │                                                              │ │
│  │  ┌────────────────────────┐  ┌──────────────────────────┐  │ │
│  │  │    TimerService        │  │  GerenciadorHistorico<T> │  │ │
│  │  │  (Singleton Service)   │  │    (Utility Class)       │  │ │
│  │  │                        │  │                          │  │ │
│  │  │  - registrarTimer()    │  │  - adicionarPartida()    │  │ │
│  │  │  - iniciarTimer()      │  │  - obterHistorico()      │  │ │
│  │  │  - trocarTurno()       │  │  - limpar()              │  │ │
│  │  │  - removerTimer()      │  │                          │  │ │
│  │  │                        │  │                          │  │ │
│  │  │  @Scheduled(100ms)     │  │                          │  │ │
│  │  │  - verificarTimers()   │  │                          │  │ │
│  │  │                        │  │                          │  │ │
│  │  │  @Scheduled(1000ms)    │  │                          │  │ │
│  │  │  - enviarAtualizacoes()│  │                          │  │ │
│  │  └────────┬───────────────┘  └──────────────────────────┘  │ │
│  │           │                                                  │ │
│  │           │ Gerencia                                         │ │
│  │           ▼                                                  │ │
│  │  ┌────────────────────────┐                                 │ │
│  │  │  ConcurrentHashMap     │                                 │ │
│  │  │  <salaId, GameTimer>   │                                 │ │
│  │  │                        │                                 │ │
│  │  │  sala1 → GameTimer     │                                 │ │
│  │  │  sala2 → GameTimer     │                                 │ │
│  │  │  sala3 → GameTimer     │                                 │ │
│  │  └────────────────────────┘                                 │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │                    REPOSITORY LAYER                          │ │
│  │  ┌──────────────────────────────────────────────────────┐   │ │
│  │  │  TicTacToeRepository (MongoDB)                       │   │ │
│  │  │                                                       │   │ │
│  │  │  TicTacToeSala:                                      │   │ │
│  │  │    - timerConfig: TimerConfig                        │   │ │
│  │  │    - historicoPorUsername: Map<String, List<T>>     │   │ │
│  │  └──────────────────────────────────────────────────────┘   │ │
│  └──────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────┘
```

## 🔄 Fluxo de Execução

### 1️⃣ Configurar Timer (Antes de Iniciar Partida)

```
Frontend                Controller              Manager                Repository
   │                        │                      │                       │
   │  POST /configurar-timer│                      │                       │
   ├───────────────────────>│                      │                       │
   │                        │ configurarTimer()    │                       │
   │                        ├─────────────────────>│                       │
   │                        │                      │ Validar dono          │
   │                        │                      │ Validar sem jogo      │
   │                        │                      │                       │
   │                        │                      │ salvar(sala)          │
   │                        │                      ├──────────────────────>│
   │                        │                      │                       │
   │                        │                      │<──────────────────────┤
   │                        │<─────────────────────┤                       │
   │<───────────────────────┤                      │                       │
   │      200 OK            │                      │                       │
```

### 2️⃣ Iniciar Partida com Timer

```
Frontend                Controller              Manager                TimerService
   │                        │                      │                       │
   │  POST /comecar         │                      │                       │
   ├───────────────────────>│                      │                       │
   │                        │ comecar()            │                       │
   │                        ├─────────────────────>│                       │
   │                        │                      │ Criar jogo            │
   │                        │                      │                       │
   │                        │                      │ if (timerAtivo)       │
   │                        │                      │   GameTimer.criar()   │
   │                        │                      │                       │
   │                        │                      │ registrarTimer()      │
   │                        │                      ├──────────────────────>│
   │                        │                      │                       │
   │                        │                      │ iniciarTimer()        │
   │                        │                      ├──────────────────────>│
   │                        │                      │                       │
   │                        │<─────────────────────┤                       │
   │<───────────────────────┤                      │                       │
   │                        │                      │                       │
   │                        │                      │   @Scheduled(1s)      │
   │                        │                      │   enviarAtualizacoes()│
   │<───────────────────────────────────────────────────────────────────────┤
   │  WebSocket: TimerUpdate│                      │                       │
```

### 3️⃣ Fazer Lance (Trocar Turno)

```
Frontend                Controller              Manager                TimerService
   │                        │                      │                       │
   │  POST /lance           │                      │                       │
   ├───────────────────────>│                      │                       │
   │                        │ lance()              │                       │
   │                        ├─────────────────────>│                       │
   │                        │                      │ Processar lance       │
   │                        │                      │                       │
   │                        │                      │ if (timerAtivo)       │
   │                        │                      │   trocarTurno()       │
   │                        │                      ├──────────────────────>│
   │                        │                      │                       │
   │                        │                      │  - Atualizar tempo    │
   │                        │                      │  - Adicionar incremento│
   │                        │                      │  - Trocar jogador     │
   │                        │                      │                       │
   │                        │<─────────────────────┤                       │
   │<───────────────────────┤                      │                       │
```

### 4️⃣ Timeout (Tempo Esgotado)

```
TimerService            Manager                Repository            WebSocket
   │                       │                       │                    │
   │ @Scheduled(100ms)     │                       │                    │
   │ verificarTimers()     │                       │                    │
   │                       │                       │                    │
   │ if (timeout)          │                       │                    │
   │   callback.accept()   │                       │                    │
   ├──────────────────────>│                       │                    │
   │                       │ processarTimeout()    │                    │
   │                       │                       │                    │
   │                       │ Determinar vencedor   │                    │
   │                       │ Encerrar jogo         │                    │
   │                       │                       │                    │
   │                       │ arquivarPartida()     │                    │
   │                       ├──────────────────────>│                    │
   │                       │                       │                    │
   │                       │ enviarNotificacao()   │                    │
   │                       ├───────────────────────────────────────────>│
   │                       │                       │  "TIMEOUT"         │
   │                       │                       │                    │
   │ removerTimer()        │                       │                    │
   │<──────────────────────┤                       │                    │
```

### 5️⃣ Obter Histórico

```
Frontend                Controller              Manager                Repository
   │                        │                      │                       │
   │  GET /historico        │                      │                       │
   ├───────────────────────>│                      │                       │
   │                        │ obterHistorico()     │                       │
   │                        ├─────────────────────>│                       │
   │                        │                      │ findByNomeSala()      │
   │                        │                      ├──────────────────────>│
   │                        │                      │                       │
   │                        │                      │<──────────────────────┤
   │                        │                      │ GerenciadorHistorico  │
   │                        │                      │   .obterHistorico()   │
   │                        │                      │                       │
   │                        │<─────────────────────┤                       │
   │<───────────────────────┤                      │                       │
   │  List<Partida>         │                      │                       │
```

## 🎯 Componentes Principais

### GameTimer (Estado)
```java
class GameTimer {
    String salaId;
    String usernameJogador1;
    String usernameJogador2;
    Long tempoRestanteJogador1Ms;
    Long tempoRestanteJogador2Ms;
    String jogadorAtual;
    Instant inicioTurnoAtual;
    TimerConfig config;
    Boolean pausado;
    Boolean tempoEsgotado;
    String jogadorQuePerderPorTempo;
}
```

### TimerService (Gerenciador)
```java
@Service
class TimerService {
    Map<String, GameTimer> timersAtivos;
    Map<String, Consumer<String>> timeoutCallbacks;
    Map<String, Consumer<GameTimer>> updateCallbacks;
    
    @Scheduled(fixedRate = 100)  // Verifica timeout
    void verificarTimers();
    
    @Scheduled(fixedRate = 1000) // Envia updates
    void enviarAtualizacoes();
}
```

### GerenciadorHistorico (Utilitário)
```java
class GerenciadorHistorico<T> {
    Map<String, List<T>> historicoPorUsername;
    
    void adicionarPartida(String username, T partida);
    List<T> obterHistorico(String username);
    List<T> obterHistoricoCompleto();
    void limpar();
}
```

## 🔌 Interfaces

### JogoComTimer
```java
interface JogoComTimer {
    TimerConfig obterConfigTimer(String nomeSala, String usernameDono);
    void configurarTimer(String nomeSala, String usernameDono, String username, TimerConfig config);
    void processarTimeout(String nomeSala, String usernameDono, String usernameJogadorTimeout);
}
```

### JogoComHistorico<T>
```java
interface JogoComHistorico<T> {
    List<T> obterHistorico(String nomeSala, String usernameDono, String username);
    List<T> obterHistoricoCompleto(String nomeSala, String usernameDono);
    void limparHistorico(String nomeSala, String usernameDono, String username);
}
```

## 📊 Modelo de Dados

### Sala de Jogo (MongoDB)
```json
{
  "_id": "...",
  "nomeSala": "Sala do João",
  "usernameDono": "joao",
  "usernameOponente": "maria",
  "jogoAtual": { ... },
  "historico": [ ... ],
  "timerConfig": {
    "timerAtivo": true,
    "tempoInicialSegundos": 300,
    "incrementoPorLanceSegundos": 3
  },
  "historicoPorUsername": {
    "joao": [
      { "numero": 1, "x": "joao", "o": "maria", "status": "x_WIN", ... },
      { "numero": 2, "x": "maria", "o": "joao", "status": "o_WIN", ... }
    ],
    "maria": [
      { "numero": 1, "x": "joao", "o": "maria", "status": "x_WIN", ... },
      { "numero": 2, "x": "maria", "o": "joao", "status": "o_WIN", ... }
    ]
  }
}
```

## ⚡ Performance

### TimerService
- **Verificação de timeout**: 100ms (10x por segundo)
- **Atualizações WebSocket**: 1000ms (1x por segundo)
- **Thread-safe**: ConcurrentHashMap
- **Callbacks assíncronos**: Não bloqueia o scheduler

### GerenciadorHistorico
- **Complexidade**: O(1) para adicionar, O(n) para obter
- **Memória**: Proporcional ao número de partidas
- **Ordenação**: Invertida na leitura (não na escrita)

## 🔒 Segurança

### Validações
- ✅ Apenas dono pode configurar timer
- ✅ Apenas dono pode limpar histórico
- ✅ Não pode alterar timer com jogo em andamento
- ✅ Validação de configuração (valores positivos)
- ✅ Validação de turno (apenas jogador da vez pode jogar)

### Thread Safety
- ✅ ConcurrentHashMap para timers ativos
- ✅ Sincronização em callbacks
- ✅ Tratamento de exceções em callbacks

## 🚀 Escalabilidade

### Limitações Atuais
- Timer não persistido (perdido ao reiniciar servidor)
- Todos os timers em memória (limitado por RAM)
- Scheduling global (não distribuído)

### Melhorias Futuras
- Persistir estado do timer no MongoDB
- Usar Redis para timers distribuídos
- Implementar clustering com Hazelcast
- Adicionar métricas (Prometheus/Grafana)
