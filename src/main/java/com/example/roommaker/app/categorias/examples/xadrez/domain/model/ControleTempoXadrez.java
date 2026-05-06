package com.example.roommaker.app.categorias.examples.xadrez.domain.model;

import lombok.*;

/**
 * Representa o controle de tempo de uma partida de xadrez.
 * 
 * MODELO BASEADO EM TIMESTAMP:
 * - Tempos armazenados em MILISSEGUNDOS (não segundos)
 * - Tempo restante é calculado dinamicamente: tempoRestante - (agora -
 * timestampUltimoLance)
 * - Não há decremento contínuo, apenas cálculo sob demanda
 * - Atualização ocorre apenas em eventos (lance, início, fim)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ControleTempoXadrez {

    /** Tempo inicial das brancas em milissegundos (null = infinito) */
    private Long tempoInicialBrancas;

    /** Tempo inicial das pretas em milissegundos (null = infinito) */
    private Long tempoInicialPretas;

    /** Incremento por lance das brancas em milissegundos */
    @Builder.Default
    private long incrementoBrancas = 0;

    /** Incremento por lance das pretas em milissegundos */
    @Builder.Default
    private long incrementoPretas = 0;

    /**
     * Tempo restante das brancas em milissegundos NO MOMENTO DO ÚLTIMO LANCE.
     * Para obter o tempo atual, use getTempoRestanteAtual(true)
     */
    private Long tempoRestanteBrancas;

    /**
     * Tempo restante das pretas em milissegundos NO MOMENTO DO ÚLTIMO LANCE.
     * Para obter o tempo atual, use getTempoRestanteAtual(false)
     */
    private Long tempoRestantePretas;

    /** Timestamp (em milissegundos) do último lance ou início da partida */
    private Long timestampUltimoLance;

    /**
     * Verifica se o tempo é infinito para ambos os jogadores.
     */
    public boolean tempoInfinito() {
        return tempoInicialBrancas == null && tempoInicialPretas == null;
    }

    /**
     * Inicializa o controle de tempo no início da partida.
     * Define o tempo restante igual ao tempo inicial e marca o timestamp.
     */
    public void inicializar() {
        this.tempoRestanteBrancas = tempoInicialBrancas;
        this.tempoRestantePretas = tempoInicialPretas;
        this.timestampUltimoLance = System.currentTimeMillis();
    }

    /**
     * Calcula o tempo restante ATUAL de um jogador de forma dinâmica.
     * 
     * Fórmula: tempoAtual = tempoRestante - (agora - timestampUltimoLance)
     * 
     * @param vezBrancas true para brancas, false para pretas
     * @return tempo restante em milissegundos, ou null se infinito
     */
    public Long getTempoRestanteAtual(boolean vezBrancas) {
        Long tempoRestante = vezBrancas ? tempoRestanteBrancas : tempoRestantePretas;

        if (tempoRestante == null) {
            return null; // Tempo infinito
        }

        if (timestampUltimoLance == null) {
            return tempoRestante;
        }

        long agora = System.currentTimeMillis();
        long decorrido = agora - timestampUltimoLance;
        long tempoAtual = tempoRestante - decorrido;

        return Math.max(0, tempoAtual);
    }

    /**
     * Verifica se o tempo de um jogador esgotou.
     * 
     * @param vezBrancas true para brancas, false para pretas
     * @return true se o tempo esgotou
     */
    public boolean tempoEsgotado(boolean vezBrancas) {
        if (tempoInfinito()) {
            return false;
        }

        Long tempoAtual = getTempoRestanteAtual(vezBrancas);
        return tempoAtual != null && tempoAtual <= 0;
    }

    /**
     * Atualiza o tempo restante após um lance.
     * 
     * IMPORTANTE: Este método deve ser chamado ANTES de trocar a vez.
     * 
     * @param vezBrancas true se foi a vez das brancas (quem acabou de jogar)
     */
    public void atualizarAposLance(boolean vezBrancas) {
        if (tempoInfinito()) {
            return;
        }

        // Calcula o tempo atual (com o tempo decorrido)
        Long tempoAtual = getTempoRestanteAtual(vezBrancas);

        if (tempoAtual == null) {
            return;
        }

        // Adiciona o incremento
        long incremento = vezBrancas ? incrementoBrancas : incrementoPretas;
        long novoTempo = tempoAtual + incremento;

        // Atualiza o tempo restante e o timestamp
        if (vezBrancas) {
            this.tempoRestanteBrancas = novoTempo;
        } else {
            this.tempoRestantePretas = novoTempo;
        }

        this.timestampUltimoLance = System.currentTimeMillis();
    }

    /**
     * Congela o tempo no estado atual.
     * Útil quando a partida termina para preservar o tempo final.
     */
    public void congelar() {
        if (tempoInfinito()) {
            return;
        }

        // Atualiza os tempos restantes para o valor atual
        if (tempoRestanteBrancas != null) {
            this.tempoRestanteBrancas = getTempoRestanteAtual(true);
        }
        if (tempoRestantePretas != null) {
            this.tempoRestantePretas = getTempoRestanteAtual(false);
        }

        // Marca o timestamp como agora para que cálculos futuros não alterem o tempo
        this.timestampUltimoLance = System.currentTimeMillis();
    }

    /**
     * Converte milissegundos para segundos (para compatibilidade com API antiga).
     */
    public Integer getTempoRestanteBrancasSegundos() {
        Long tempo = getTempoRestanteAtual(true);
        return tempo != null ? (int) (tempo / 1000) : null;
    }

    /**
     * Converte milissegundos para segundos (para compatibilidade com API antiga).
     */
    public Integer getTempoRestantePretasSegundos() {
        Long tempo = getTempoRestanteAtual(false);
        return tempo != null ? (int) (tempo / 1000) : null;
    }

    /**
     * Converte tempo inicial de milissegundos para segundos.
     */
    public Integer getTempoInicialBrancasSegundos() {
        return tempoInicialBrancas != null ? (int) (tempoInicialBrancas / 1000) : null;
    }

    /**
     * Converte tempo inicial de milissegundos para segundos.
     */
    public Integer getTempoInicialPretasSegundos() {
        return tempoInicialPretas != null ? (int) (tempoInicialPretas / 1000) : null;
    }

    /**
     * Converte incremento de milissegundos para segundos.
     */
    public int getIncrementoBrancasSegundos() {
        return (int) (incrementoBrancas / 1000);
    }

    /**
     * Converte incremento de milissegundos para segundos.
     */
    public int getIncrementoPretasSegundos() {
        return (int) (incrementoPretas / 1000);
    }
}
