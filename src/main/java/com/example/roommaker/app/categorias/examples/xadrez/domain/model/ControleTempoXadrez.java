package com.example.roommaker.app.categorias.examples.xadrez.domain.model;

import lombok.*;

/**
 * Representa o controle de tempo de uma partida de xadrez.
 * Tempos em segundos.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ControleTempoXadrez {

    /** Tempo inicial das brancas em segundos (null = infinito) */
    private Integer tempoInicialBrancas;

    /** Tempo inicial das pretas em segundos (null = infinito) */
    private Integer tempoInicialPretas;

    /** Incremento por lance das brancas em segundos */
    @Builder.Default
    private int incrementoBrancas = 0;

    /** Incremento por lance das pretas em segundos */
    @Builder.Default
    private int incrementoPretas = 0;

    /** Tempo restante das brancas em segundos */
    private Integer tempoRestanteBrancas;

    /** Tempo restante das pretas em segundos */
    private Integer tempoRestantePretas;

    /** Timestamp do último lance (para calcular tempo decorrido) */
    private Long timestampUltimoLance;

    public boolean tempoInfinito() {
        return tempoInicialBrancas == null && tempoInicialPretas == null;
    }

    public void inicializar() {
        this.tempoRestanteBrancas = tempoInicialBrancas;
        this.tempoRestantePretas = tempoInicialPretas;
        this.timestampUltimoLance = System.currentTimeMillis();
    }

    public boolean tempoEsgotado(boolean brancas) {
        if (tempoInfinito())
            return false;
        Integer tempo = brancas ? tempoRestanteBrancas : tempoRestantePretas;
        return tempo != null && tempo <= 0;
    }
}
