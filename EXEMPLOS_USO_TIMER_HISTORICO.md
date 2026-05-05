# 📚 Exemplos Práticos de Uso - Timer e Histórico

Este documento contém exemplos práticos de como usar o sistema de Timer e Histórico.

## 🎮 Exemplo Completo: TicTacToe com Timer

### 1. Frontend - Configurar Timer ao Criar Sala

```typescript
// Componente de criação de sala
class CriarSalaComponent {
  
  timerAtivo = false;
  tempoInicial = 60; // segundos
  incremento = 2; // segundos
  
  async criarSala() {
    // 1. Criar sala normal
    const sala = await this.salaService.criar({
      nome: this.nomeSala,
      categoria: 'tictactoe',
      capacidade: 2
    });
    
    // 2. Configurar timer se ativo
    if (this.timerAtivo) {
      await this.tictactoeService.configurarTimer(
        sala.nome,
        sala.usernameDono,
        {
          timerAtivo: true,
          tempoInicialSegundos: this.tempoInicial,
          incrementoPorLanceSegundos: this.incremento
        }
      );
    }
    
    this.router.navigate(['/sala', sala.nome]);
  }
}
```

### 2. Frontend - Mostrar Timer em Tempo Real

```typescript
// Componente do jogo
class TicTacToeComponent implements OnInit, OnDestroy {
  
  tempoJogador1: number = 0;
  tempoJogador2: number = 0;
  jogadorAtual: string = '';
  timerAtivo: boolean = false;
  
  private stompClient: any;
  
  ngOnInit() {
    this.conectarWebSocket();
    this.carregarJogo();
  }
  
  conectarWebSocket() {
    const socket = new SockJS('/ws');
    this.stompClient = Stomp.over(socket);
    
    this.stompClient.connect({}, () => {
      // Inscrever no tópico de timer
      this.stompClient.subscribe(
        `/topic/tictactoe/${this.usernameDono}/${this.nomeSala}/timer`,
        (message) => {
          const timerUpdate = JSON.parse(message.body);
          this.atualizarTimer(timerUpdate);
        }
      );
      
      // Inscrever no tópico de timeout
      this.stompClient.subscribe(
        `/topic/tictactoe/${this.usernameDono}/${this.nomeSala}/timeout`,
        (message) => {
          const data = JSON.parse(message.body);
          this.mostrarTimeout(data.jogadorQuePerderPorTempo);
        }
      );
    });
  }
  
  atualizarTimer(timerUpdate: any) {
    this.tempoJogador1 = Math.ceil(timerUpdate.tempoRestanteJogador1Ms / 1000);
    this.tempoJogador2 = Math.ceil(timerUpdate.tempoRestanteJogador2Ms / 1000);
    this.jogadorAtual = timerUpdate.jogadorAtual;
    this.timerAtivo = !timerUpdate.pausado;
    
    // Alerta quando tempo está acabando
    if (this.jogadorAtual === this.username) {
      const meuTempo = this.jogadorAtual === timerUpdate.usernameJogador1 
        ? this.tempoJogador1 
        : this.tempoJogador2;
      
      if (meuTempo <= 10 && meuTempo > 0) {
        this.tocarAlerta();
      }
    }
  }
  
  mostrarTimeout(jogadorTimeout: string) {
    const vencedor = jogadorTimeout === this.jogo.x ? this.jogo.o : this.jogo.x;
    this.mostrarMensagem(`${jogadorTimeout} perdeu por tempo! ${vencedor} venceu!`);
  }
  
  formatarTempo(segundos: number): string {
    const min = Math.floor(segundos / 60);
    const seg = segundos % 60;
    return `${min}:${seg.toString().padStart(2, '0')}`;
  }
  
  ngOnDestroy() {
    if (this.stompClient) {
      this.stompClient.disconnect();
    }
  }
}
```

### 3. Frontend - Template HTML

```html
<!-- Timer visual -->
<div class="timer-container" *ngIf="timerAtivo">
  <div class="timer-jogador" 
       [class.ativo]="jogadorAtual === jogo.x"
       [class.alerta]="tempoJogador1 <= 10">
    <span class="nome">{{ jogo.x }}</span>
    <span class="tempo">{{ formatarTempo(tempoJogador1) }}</span>
  </div>
  
  <div class="timer-jogador" 
       [class.ativo]="jogadorAtual === jogo.o"
       [class.alerta]="tempoJogador2 <= 10">
    <span class="nome">{{ jogo.o }}</span>
    <span class="tempo">{{ formatarTempo(tempoJogador2) }}</span>
  </div>
</div>

<!-- Tabuleiro -->
<div class="tabuleiro">
  <!-- ... -->
</div>

<!-- Histórico -->
<div class="historico-container">
  <h3>Histórico de Partidas</h3>
  <button (click)="carregarHistorico()">Atualizar</button>
  
  <div class="partida" *ngFor="let partida of historico">
    <span>Partida #{{ partida.numero }}</span>
    <span>{{ partida.x }} vs {{ partida.o }}</span>
    <span class="resultado">{{ formatarResultado(partida.status) }}</span>
  </div>
</div>
```

### 4. Backend - Controller

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
        try {
            TimerConfig config = request.toTimerConfig();
            manager.configurarTimer(nomeSala, usernameDono, principal.getName(), config);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/{nomeSala}/timer")
    public ResponseEntity<TimerConfigRequest> obterConfigTimer(
        @PathVariable String nomeSala,
        @RequestParam String usernameDono
    ) {
        TimerConfig config = manager.obterConfigTimer(nomeSala, usernameDono);
        return ResponseEntity.ok(TimerConfigRequest.fromTimerConfig(config));
    }
    
    @GetMapping("/{nomeSala}/historico")
    public ResponseEntity<List<TicTacToe>> obterHistorico(
        @PathVariable String nomeSala,
        @RequestParam String usernameDono,
        Principal principal
    ) {
        List<TicTacToe> historico = manager.obterHistorico(
            nomeSala, 
            usernameDono, 
            principal.getName()
        );
        return ResponseEntity.ok(historico);
    }
    
    @GetMapping("/{nomeSala}/historico/completo")
    public ResponseEntity<List<TicTacToe>> obterHistoricoCompleto(
        @PathVariable String nomeSala,
        @RequestParam String usernameDono,
        Principal principal
    ) {
        // Validar que é o dono
        Sala sala = salaManager.mostrarSala(nomeSala, usernameDono);
        if (!sala.getUsernameDono().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<TicTacToe> historico = manager.obterHistoricoCompleto(nomeSala, usernameDono);
        return ResponseEntity.ok(historico);
    }
    
    @DeleteMapping("/{nomeSala}/historico")
    public ResponseEntity<?> limparHistorico(
        @PathVariable String nomeSala,
        @RequestParam String usernameDono,
        Principal principal
    ) {
        try {
            manager.limparHistorico(nomeSala, usernameDono, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
```

### 5. Backend - Manager (Integração Completa)

```java
@Component
public class TicTacToeManager implements JogoPort, JogoComTimer, JogoComHistorico<TicTacToe> {
    
    private final TicTacToeRepository ticTacToeRepository;
    private final TimerService timerService;
    private final TicTacToeWebSocketSender sender;
    private final SalaManager salaManager;
    
    // ===== IMPLEMENTAÇÃO DE JogoComTimer =====
    
    @Override
    public TimerConfig obterConfigTimer(String nomeSala, String usernameDono) {
        TicTacToeSala sala = ticTacToeRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        return sala != null ? sala.getTimerConfig() : TimerConfig.semTimer();
    }
    
    @Override
    public void configurarTimer(String nomeSala, String usernameDono, String username, TimerConfig config) {
        TicTacToeSala sala = ticTacToeRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        
        if (sala == null) {
            throw new ErroDeRequisicaoGeral("Sala não encontrada");
        }
        
        if (!usernameDono.equals(username)) {
            throw new UsuarioNaoAutorizado("Apenas o dono pode configurar o timer");
        }
        
        if (sala.getJogoAtual() != null) {
            throw new ErroDeRequisicaoGeral("Não é possível alterar timer com jogo em andamento");
        }
        
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
            return;
        }
        
        boolean timeoutEraX = jogo.getX().equals(usernameJogadorTimeout);
        jogo.setStatus(timeoutEraX ? TicTacToeStatus.o_WIN : TicTacToeStatus.x_WIN);
        
        arquivarPartida(sala, jogo);
        
        Sala salaBase = salaManager.mostrarSala(nomeSala, usernameDono);
        sender.enviarTimeout(salaBase, jogo, usernameJogadorTimeout);
    }
    
    // ===== IMPLEMENTAÇÃO DE JogoComHistorico =====
    
    @Override
    public List<TicTacToe> obterHistorico(String nomeSala, String usernameDono, String username) {
        TicTacToeSala sala = ticTacToeRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        if (sala == null) {
            return new ArrayList<>();
        }
        
        GerenciadorHistorico<TicTacToe> gerenciador = new GerenciadorHistorico<>(sala.getHistoricoPorUsername());
        return gerenciador.obterHistorico(username);
    }
    
    @Override
    public List<TicTacToe> obterHistoricoCompleto(String nomeSala, String usernameDono) {
        TicTacToeSala sala = ticTacToeRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        if (sala == null) {
            return new ArrayList<>();
        }
        
        GerenciadorHistorico<TicTacToe> gerenciador = new GerenciadorHistorico<>(sala.getHistoricoPorUsername());
        return gerenciador.obterHistoricoCompleto();
    }
    
    @Override
    public void limparHistorico(String nomeSala, String usernameDono, String username) {
        TicTacToeSala sala = ticTacToeRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        
        if (sala == null) {
            throw new ErroDeRequisicaoGeral("Sala não encontrada");
        }
        
        if (!usernameDono.equals(username)) {
            throw new UsuarioNaoAutorizado("Apenas o dono pode limpar o histórico");
        }
        
        sala.getHistoricoPorUsername().clear();
        sala.getHistorico().clear();
        ticTacToeRepository.salvar(sala);
    }
    
    // ===== MÉTODOS EXISTENTES MODIFICADOS =====
    
    public TicTacToeSala comecar(Sala sala) {
        String x;
        String o;
        TicTacToeSala ticTacToeSala = this.ticTacToeRepository.findByNomeSalaAndUsernameDono(
            sala.getNome(), 
            sala.getUsernameDono()
        );
        
        if (ticTacToeSala == null) {
            ticTacToeSala = TicTacToeSala.builder()
                    .nomeSala(sala.getNome())
                    .usernameDono(sala.getUsernameDono())
                    .usernameOponente(sala.getUsernameParticipantes().get(0))
                    .jogoAtual(null)
                    .historico(new ArrayList<>())
                    .timerConfig(TimerConfig.semTimer())
                    .historicoPorUsername(new HashMap<>())
                    .build();
            x = this.sortearJogadorInicial(sala.getUsernameDono(), sala.getUsernameParticipantes().get(0));
            o = x.equals(sala.getUsernameDono()) ? sala.getUsernameParticipantes().get(0) : sala.getUsernameDono();
            this.ticTacToeRepository.criar(ticTacToeSala);
        } else {
            if (ticTacToeSala.getJogoAtual() == null) {
                x = ticTacToeSala.getHistorico().get(ticTacToeSala.getHistorico().size() - 1).getO();
                o = x.equals(sala.getUsernameDono()) ? sala.getUsernameParticipantes().get(0) : sala.getUsernameDono();
            } else {
                return ticTacToeSala;
            }
        }
        
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
        TicTacToeSala ticTacToeSala = this.ticTacToeRepository.findByNomeSalaAndUsernameDono(
            sala.getNome(), 
            sala.getUsernameDono()
        );
        
        if (ticTacToeSala == null) {
            throw new ErroDeRequisicaoGeral("Sala de jogo não inicializada");
        }
        
        TicTacToe jogoAtual = ticTacToeSala.getJogoAtual();
        if (jogoAtual == null) {
            throw new ErroDeRequisicaoGeral("Jogo não inicializado");
        }
        
        if (jogoAtual.getStatus() == TicTacToeStatus.x_TURN && username.equals(jogoAtual.getX())) {
            jogoAtual = this.jogar(jogoAtual, lance.getLance(), "x");
        } else if (jogoAtual.getStatus() == TicTacToeStatus.o_TURN && username.equals(jogoAtual.getO())) {
            jogoAtual = this.jogar(jogoAtual, lance.getLance(), "o");
        } else {
            throw new ErroDeRequisicaoGeral("Não é a sua vez de jogar");
        }
        
        // NOVO: Trocar turno do timer
        if (ticTacToeSala.getTimerConfig().getTimerAtivo()) {
            String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
            timerService.trocarTurno(salaId);
        }
        
        if (jogoAtual.getStatus() == TicTacToeStatus.o_WIN || 
            jogoAtual.getStatus() == TicTacToeStatus.x_WIN || 
            jogoAtual.getStatus() == TicTacToeStatus.DRAW) {
            
            // NOVO: Arquivar no histórico por jogador
            arquivarPartida(ticTacToeSala, jogoAtual);
            
            // NOVO: Remover timer
            if (ticTacToeSala.getTimerConfig().getTimerAtivo()) {
                String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
                timerService.removerTimer(salaId);
            }
        } else {
            ticTacToeSala.setJogoAtual(jogoAtual);
            this.ticTacToeRepository.salvar(ticTacToeSala);
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
        Sala salaBase = salaManager.mostrarSala(sala.getNomeSala(), sala.getUsernameDono());
        sender.enviarTimerUpdate(salaBase, response);
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
    
    @Override
    public void saidaDeParticipante(String usernameParticipante, Sala sala) {
        // Remover timer se existir
        String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
        if (timerService.existeTimer(salaId)) {
            timerService.removerTimer(salaId);
        }
        
        this.ticTacToeRepository.deleteByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
    }
    
    @Override
    public void deletarJogo(Sala sala) {
        // Remover timer se existir
        String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
        if (timerService.existeTimer(salaId)) {
            timerService.removerTimer(salaId);
        }
        
        this.ticTacToeRepository.deleteByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
    }
}
```

## 🧪 Testes

### Teste Unitário - GameTimer

```java
@Test
void testarTimerComIncremento() {
    TimerConfig config = TimerConfig.builder()
        .timerAtivo(true)
        .tempoInicialSegundos(10L)
        .incrementoPorLanceSegundos(2L)
        .build();
    
    GameTimer timer = GameTimer.criar("sala1", "jogador1", "jogador2", config);
    timer.iniciar("jogador1");
    
    // Simular 5 segundos
    try {
        Thread.sleep(5000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    
    long tempoRestante = timer.calcularTempoRestanteAtual("jogador1");
    assertTrue(tempoRestante < 10000 && tempoRestante > 4000);
    
    timer.trocarTurno();
    
    // Jogador1 deve ter recebido incremento
    long tempoAposIncremento = timer.getTempoRestanteJogador1Ms();
    assertTrue(tempoAposIncremento > 5000); // ~5s + 2s incremento
}

@Test
void testarTimeout() {
    TimerConfig config = TimerConfig.builder()
        .timerAtivo(true)
        .tempoInicialSegundos(1L)
        .incrementoPorLanceSegundos(0L)
        .build();
    
    GameTimer timer = GameTimer.criar("sala1", "jogador1", "jogador2", config);
    timer.iniciar("jogador1");
    
    // Aguardar timeout
    try {
        Thread.sleep(1500);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    
    boolean timeout = timer.verificarTimeout();
    assertTrue(timeout);
    assertEquals("jogador1", timer.getJogadorQuePerderPorTempo());
}
```

### Teste Unitário - GerenciadorHistorico

```java
@Test
void testarHistoricoPorJogador() {
    GerenciadorHistorico<TicTacToe> gerenciador = new GerenciadorHistorico<>();
    
    TicTacToe jogo1 = TicTacToe.builder()
        .x("jogador1")
        .o("jogador2")
        .status(TicTacToeStatus.x_WIN)
        .build();
    
    TicTacToe jogo2 = TicTacToe.builder()
        .x("jogador1")
        .o("jogador3")
        .status(TicTacToeStatus.o_WIN)
        .build();
    
    gerenciador.adicionarPartidaParaJogadores(jogo1, "jogador1", "jogador2");
    gerenciador.adicionarPartidaParaJogadores(jogo2, "jogador1", "jogador3");
    
    List<TicTacToe> historicoJ1 = gerenciador.obterHistorico("jogador1");
    assertEquals(2, historicoJ1.size());
    
    List<TicTacToe> historicoJ2 = gerenciador.obterHistorico("jogador2");
    assertEquals(1, historicoJ2.size());
    
    List<TicTacToe> historicoJ3 = gerenciador.obterHistorico("jogador3");
    assertEquals(1, historicoJ3.size());
}
```

## 📱 Exemplos de Requisições HTTP

### Configurar Timer
```bash
curl -X POST http://localhost:8080/api/tictactoe/MinhaS ala/configurar-timer?usernameDono=joao \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "timerAtivo": true,
    "tempoInicialSegundos": 300,
    "incrementoPorLanceSegundos": 3
  }'
```

### Obter Configuração de Timer
```bash
curl -X GET http://localhost:8080/api/tictactoe/MinhaSala/timer?usernameDono=joao \
  -H "Authorization: Bearer <token>"
```

### Obter Histórico
```bash
curl -X GET http://localhost:8080/api/tictactoe/MinhaSala/historico?usernameDono=joao \
  -H "Authorization: Bearer <token>"
```

### Limpar Histórico
```bash
curl -X DELETE http://localhost:8080/api/tictactoe/MinhaSala/historico?usernameDono=joao \
  -H "Authorization: Bearer <token>"
```

## 🎨 CSS para Timer Visual

```css
.timer-container {
  display: flex;
  justify-content: space-around;
  margin: 20px 0;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 10px;
}

.timer-jogador {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px 30px;
  background: white;
  border-radius: 8px;
  border: 2px solid #ddd;
  transition: all 0.3s;
}

.timer-jogador.ativo {
  border-color: #4CAF50;
  box-shadow: 0 0 15px rgba(76, 175, 80, 0.3);
}

.timer-jogador.alerta {
  border-color: #f44336;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 15px rgba(244, 67, 54, 0.3);
  }
  50% {
    box-shadow: 0 0 25px rgba(244, 67, 54, 0.6);
  }
}

.timer-jogador .nome {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.timer-jogador .tempo {
  font-size: 32px;
  font-weight: bold;
  font-family: 'Courier New', monospace;
  color: #333;
}

.timer-jogador.alerta .tempo {
  color: #f44336;
}
```

Este arquivo fornece exemplos práticos completos de como implementar e usar o sistema de Timer e Histórico!
