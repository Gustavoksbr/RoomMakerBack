# Arquitetura do Sistema de Tempo no Xadrez

## Visão Geral

O sistema de tempo foi refatorado de um modelo baseado em **polling contínuo** para um modelo **orientado a eventos** e **baseado em timestamp**, seguindo as melhores práticas de sistemas escaláveis.

## Mudanças Principais

### 1. Modelo de Dados: Timestamp ao invés de Decremento

**ANTES (❌ Problema):**
- Tempo armazenado em segundos
- Decrementado a cada segundo por um scheduler
- `@Scheduled(fixedRate = 1000)` rodando para TODAS as salas
- `repository.findAll()` a cada segundo
- Múltiplos `repository.save()` por segundo

**DEPOIS (✅ Solução Híbrida):**
- Tempo armazenado em **milissegundos**
- **Nunca decrementado automaticamente**
- Tempo calculado dinamicamente: `tempoAtual = tempoRestante - (agora - timestampUltimoLance)`
- Atualização apenas em **eventos** (lance, início, fim)
- Scheduler LEVE apenas para **verificar timeout** (não modifica dados)
- Zero overhead quando não há partidas ativas

### 2. Separação de Responsabilidades

#### `ControleTempoXadrez` (Modelo de Domínio)
```java
// Cálculo dinâmico do tempo
public Long getTempoRestanteAtual(boolean vezBrancas) {
    long decorrido = System.currentTimeMillis() - timestampUltimoLance;
    return Math.max(0, tempoRestante - decorrido);
}

// Verifica timeout sem reconstruir board
public boolean tempoEsgotado(boolean vezBrancas) {
    return getTempoRestanteAtual(vezBrancas) <= 0;
}

// Atualiza após lance (adiciona incremento)
public void atualizarAposLance(boolean vezBrancas) {
    long tempoAtual = getTempoRestanteAtual(vezBrancas);
    long novoTempo = tempoAtual + incremento;
    this.tempoRestante = novoTempo;
    this.timestampUltimoLance = System.currentTimeMillis();
}
```

#### `XadrezTempoService` (Lógica de Negócio)
```java
// Cria controle de tempo (converte segundos → milissegundos)
public ControleTempoXadrez criarControleTempo(...)

// Verifica timeout e determina resultado
public ResultadoTimeout verificarTimeout(PartidaXadrez, SalaXadrez)

// Processa tempo após lance
public void processarAposLance(PartidaXadrez, boolean vezBrancas)

// Congela tempo ao fim da partida
public void congelarTempo(PartidaXadrez)
```

#### `XadrezManager` (Orquestração)
```java
@Transactional
public void jogar(...) {
    // EVENTO 1: Verifica timeout ANTES do lance
    ResultadoTimeout timeout = tempoService.verificarTimeout(partida, salaXadrez);
    if (timeout != null) {
        encerrarPorTimeout(timeout);
    }
    
    // Processa o lance...
    
    // EVENTO 2: Atualiza tempo APÓS o lance
    tempoService.processarAposLance(partida, vezBrancas);
    
    // EVENTO 3: Congela tempo se partida terminou
    if (fim != null) {
        tempoService.congelarTempo(partida);
    }
}
```

### 3. Fluxo de Eventos

```
┌─────────────────────────────────────────────────────────────┐
│ INÍCIO DA PARTIDA                                           │
│ ├─ ControleTempoXadrez.inicializar()                        │
│ └─ timestampUltimoLance = agora                             │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ JOGADOR FAZ LANCE                                           │
│ ├─ 1. Verifica timeout (cálculo dinâmico)                   │
│ │   └─ Se esgotou: encerra partida                          │
│ ├─ 2. Valida e processa lance                               │
│ ├─ 3. Atualiza tempo:                                        │
│ │   ├─ Calcula tempo atual                                  │
│ │   ├─ Adiciona incremento                                  │
│ │   └─ Atualiza timestamp                                   │
│ └─ 4. Salva UMA VEZ no banco                                │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ SCHEDULER (a cada 1 segundo)                                │
│ ├─ Filtra apenas partidas ativas com tempo                  │
│ ├─ Para cada partida:                                        │
│ │   ├─ Calcula tempo atual (dinâmico, sem modificar)        │
│ │   └─ Se esgotou: encerra partida e notifica               │
│ └─ Se não há partidas ativas: não faz nada                  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ FIM DA PARTIDA                                              │
│ ├─ ControleTempoXadrez.congelar()                           │
│ └─ Preserva tempo final para histórico                      │
└─────────────────────────────────────────────────────────────┘
```

### 4. Compatibilidade com API Antiga

Para manter compatibilidade com o frontend, o `ControleTempoXadrez` fornece métodos de conversão:

```java
// Converte milissegundos → segundos para API
public Integer getTempoRestanteBrancasSegundos() {
    return (int) (getTempoRestanteAtual(true) / 1000);
}

public Integer getTempoInicialBrancasSegundos() {
    return (int) (tempoInicialBrancas / 1000);
}
```

O `XadrezManager.construirResponse()` usa esses métodos para enviar dados em segundos via WebSocket.

## Benefícios da Nova Arquitetura

### ✅ Performance
- **Overhead mínimo**: scheduler só roda se há partidas ativas
- **Sem decremento**: tempo calculado dinamicamente, não modificado
- **Menos writes**: salva apenas quando timeout ocorre (raro)
- **Cálculo eficiente**: O(1) por partida ativa

### ✅ Escalabilidade
- Suporta **milhares de salas simultâneas** sem degradação
- Carga proporcional ao número de partidas ATIVAS, não total de salas
- Stateless: não mantém estado em memória
- Scheduler filtra partidas antes de processar

### ✅ Precisão
- Cálculo baseado em **milissegundos** (não segundos)
- Timestamp preciso do sistema operacional
- Sincronização automática entre backend e frontend
- Timeout detectado em até 1 segundo

### ✅ Manutenibilidade
- Separação clara de responsabilidades
- Código testável (lógica separada do scheduler)
- Logs estruturados com SLF4J
- Transações explícitas com `@Transactional`

### ✅ Confiabilidade
- Timeout verificado em dois pontos: lance + scheduler
- Tempo nunca "pula" ou "atrasa"
- Comportamento determinístico
- Graceful degradation se scheduler falhar

## Comparação: Antes vs Depois

| Aspecto | ANTES (Polling) | DEPOIS (Híbrido) |
|---------|----------------|------------------|
| **Overhead** | Constante (1 query/s) | Proporcional a partidas ativas |
| **Queries/s** | N salas × 1 Hz | N partidas ativas × 1 Hz |
| **Writes/s** | N salas × 1 Hz | Apenas em timeout (raro) |
| **Precisão** | ±1 segundo | ±1 milissegundo |
| **Escalabilidade** | Linear (O(n)) | Filtrada (O(partidas ativas)) |
| **Complexidade** | Alta (scheduler + manager) | Média (scheduler leve + eventos) |
| **Testabilidade** | Difícil (async) | Fácil (lógica separada) |
| **Timeout** | Apenas no lance | Lance + scheduler (redundante) |

## Exemplo de Uso

```java
// Criar partida com tempo
ControleTempoXadrez tempo = tempoService.criarControleTempo(
    5 * 60,  // 5 minutos para brancas
    3,       // +3 segundos por lance
    5 * 60,  // 5 minutos para pretas
    3        // +3 segundos por lance
);
tempo.inicializar();

// Durante o jogo
ResultadoTimeout timeout = tempoService.verificarTimeout(partida, sala);
if (timeout != null) {
    // Tempo esgotou!
}

// Após lance
tempoService.processarAposLance(partida, vezBrancas);

// Ao fim
tempoService.congelarTempo(partida);
```

## Testes

Os testes existentes em `ControleTempoXadrezTest.java` foram mantidos e continuam passando, pois a interface pública não mudou significativamente.

Novos testes podem ser adicionados para:
- `XadrezTempoService.verificarTimeout()`
- `XadrezTempoService.processarAposLance()`
- Cálculo dinâmico de tempo em diferentes cenários

## Migração de Dados

**Não é necessária migração de dados**, pois:
- O campo `timestampUltimoLance` já existia
- Os campos de tempo são convertidos automaticamente (segundos → milissegundos)
- Partidas antigas no histórico continuam funcionando

## Conclusão

A refatoração transforma o sistema de um modelo **reativo e ineficiente** (polling) para um modelo **híbrido e escalável** (eventos + verificação leve), seguindo os princípios de:

- **Event-Driven Architecture** (eventos primários)
- **Lazy Evaluation** (cálculo sob demanda)
- **Separation of Concerns** (lógica separada)
- **Single Responsibility Principle** (cada classe uma responsabilidade)
- **Defensive Programming** (verificação redundante de timeout)

O resultado é um sistema:
- ✅ Mais rápido (cálculo dinâmico)
- ✅ Mais preciso (milissegundos)
- ✅ Mais confiável (timeout verificado em 2 pontos)
- ✅ Preparado para escalar (overhead proporcional a partidas ativas)

### Arquitetura Híbrida

A solução combina o melhor de dois mundos:

1. **Eventos** (primário): Verificação imediata quando jogador faz lance
2. **Scheduler** (backup): Garante que timeout seja detectado mesmo sem lance

Isso garante que a partida termine **imediatamente** quando o tempo esgota, sem depender de ação do jogador.
