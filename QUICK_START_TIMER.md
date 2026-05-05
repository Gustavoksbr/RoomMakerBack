# ⚡ Quick Start - Timer e Histórico

## 🎯 Em 5 Minutos

### 1️⃣ Adicionar ao Modelo da Sala

```java
@SuperBuilder
public class MeuJogoSala extends Sala {
    // ... campos existentes ...
    
    @Builder.Default
    private TimerConfig timerConfig = TimerConfig.semTimer();
    
    @Builder.Default
    private Map<String, List<MeuJogo>> historicoPorUsername = new HashMap<>();
}
```

### 2️⃣ Implementar Interfaces no Manager

```java
@Service
public class MeuJogoManager implements JogoPort, JogoComTimer, JogoComHistorico<MeuJogo> {
    
    private final TimerService timerService;
    private final MeuJogoRepository repository;
    
    // Timer
    @Override
    public TimerConfig obterConfigTimer(String nomeSala, String usernameDono) {
        MeuJogoSala sala = repository.find(nomeSala, usernameDono);
        return sala.getTimerConfig();
    }
    
    @Override
    public void configurarTimer(String nomeSala, String usernameDono, String username, TimerConfig config) {
        // Validar dono + sem jogo em andamento
        MeuJogoSala sala = repository.find(nomeSala, usernameDono);
        sala.setTimerConfig(config);
        repository.save(sala);
    }
    
    @Override
    public void processarTimeout(String nomeSala, String usernameDono, String usernameTimeout) {
        // Determinar vencedor e encerrar jogo
        MeuJogoSala sala = repository.find(nomeSala, usernameDono);
        MeuJogo jogo = sala.getJogoAtual();
        jogo.setVencedor(/* outro jogador */);
        arquivarPartida(sala, jogo);
    }
    
    // Histórico
    @Override
    public List<MeuJogo> obterHistorico(String nomeSala, String usernameDono, String username) {
        MeuJogoSala sala = repository.find(nomeSala, usernameDono);
        return new GerenciadorHistorico<>(sala.getHistoricoPorUsername()).obterHistorico(username);
    }
    
    @Override
    public List<MeuJogo> obterHistoricoCompleto(String nomeSala, String usernameDono) {
        MeuJogoSala sala = repository.find(nomeSala, usernameDono);
        return new GerenciadorHistorico<>(sala.getHistoricoPorUsername()).obterHistoricoCompleto();
    }
    
    @Override
    public void limparHistorico(String nomeSala, String usernameDono, String username) {
        // Validar dono
        MeuJogoSala sala = repository.find(nomeSala, usernameDono);
        sala.getHistoricoPorUsername().clear();
        repository.save(sala);
    }
}
```

### 3️⃣ Integrar no Fluxo do Jogo

```java
// AO INICIAR PARTIDA
public MeuJogoSala comecar(Sala sala) {
    // ... criar jogo ...
    
    if (meuJogoSala.getTimerConfig().getTimerAtivo()) {
        String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
        GameTimer timer = GameTimer.criar(salaId, jogador1, jogador2, meuJogoSala.getTimerConfig());
        
        timerService.registrarTimer(
            timer,
            username -> processarTimeout(sala.getNome(), sala.getUsernameDono(), username),
            t -> enviarTimerUpdate(sala, t)
        );
        
        timerService.iniciarTimer(salaId, primeiroJogador);
    }
    
    return meuJogoSala;
}

// APÓS CADA LANCE
public MeuJogo lance(MeuJogoLance lance, String username, Sala sala) {
    // ... processar lance ...
    
    if (meuJogoSala.getTimerConfig().getTimerAtivo()) {
        String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
        timerService.trocarTurno(salaId);
    }
    
    if (jogoTerminou) {
        arquivarPartida(meuJogoSala, jogo);
        
        if (meuJogoSala.getTimerConfig().getTimerAtivo()) {
            String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
            timerService.removerTimer(salaId);
        }
    }
    
    return jogo;
}

// ARQUIVAR PARTIDA
private void arquivarPartida(MeuJogoSala sala, MeuJogo jogo) {
    sala.setJogoAtual(null);
    sala.getHistorico().add(jogo);
    
    GerenciadorHistorico<MeuJogo> gerenciador = new GerenciadorHistorico<>(sala.getHistoricoPorUsername());
    gerenciador.adicionarPartidaParaJogadores(jogo, jogo.getJogador1(), jogo.getJogador2());
    sala.setHistoricoPorUsername(gerenciador.obterMapa());
    
    repository.save(sala);
}

// AO SAIR/DELETAR
@Override
public void saidaDeParticipante(String username, Sala sala) {
    String salaId = gerarSalaId(sala.getNome(), sala.getUsernameDono());
    if (timerService.existeTimer(salaId)) {
        timerService.removerTimer(salaId);
    }
    repository.delete(sala.getNome(), sala.getUsernameDono());
}
```

### 4️⃣ Adicionar Endpoints

```java
@RestController
@RequestMapping("/api/meujogo")
public class MeuJogoController {
    
    @PostMapping("/{nomeSala}/configurar-timer")
    public ResponseEntity<?> configurarTimer(
        @PathVariable String nomeSala,
        @RequestParam String usernameDono,
        @RequestBody TimerConfigRequest request,
        Principal principal
    ) {
        manager.configurarTimer(nomeSala, usernameDono, principal.getName(), request.toTimerConfig());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{nomeSala}/historico")
    public ResponseEntity<List<MeuJogo>> obterHistorico(
        @PathVariable String nomeSala,
        @RequestParam String usernameDono,
        Principal principal
    ) {
        return ResponseEntity.ok(manager.obterHistorico(nomeSala, usernameDono, principal.getName()));
    }
}
```

## 🎮 Configurações Prontas

```java
// Bullet (1 min + 1 seg)
TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(60L)
    .incrementoPorLanceSegundos(1L)
    .build();

// Blitz (3 min + 2 seg)
TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(180L)
    .incrementoPorLanceSegundos(2L)
    .build();

// Rapid (10 min)
TimerConfig.builder()
    .timerAtivo(true)
    .tempoInicialSegundos(600L)
    .incrementoPorLanceSegundos(0L)
    .build();

// Sem Timer
TimerConfig.semTimer();
```

## ✅ Pronto!

Agora seu jogo tem:
- ✅ Timer personalizável
- ✅ Histórico completo
- ✅ Atualizações em tempo real
- ✅ Detecção de timeout

## 📚 Documentação Completa

- [TIMER_E_HISTORICO_GUIA.md](./TIMER_E_HISTORICO_GUIA.md) - Guia detalhado
- [EXEMPLOS_USO_TIMER_HISTORICO.md](./EXEMPLOS_USO_TIMER_HISTORICO.md) - Exemplos práticos
- [ARQUITETURA_TIMER_HISTORICO.md](./ARQUITETURA_TIMER_HISTORICO.md) - Arquitetura
