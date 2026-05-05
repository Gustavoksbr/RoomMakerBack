package com.example.roommaker.app.categorias.common.timer;

import lombok.*;

/**
 * DTO para enviar informações do timer via WebSocket ou HTTP.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimerResponse {

    /**
     * Username do jogador 1.
     */
    private String usernameJogador1;

    /**
     * Username do jogador 2.
     */
    private String usernameJogador2;

    /**
     * Tempo restante do jogador 1 em milissegundos.
     */
    private Long tempoRestanteJogador1Ms;

    /**
     * Tempo restante do jogador 2 em milissegundos.
     */
    private Long tempoRestanteJogador2Ms;

    /**
     * Username do jogador no turno atual.
     */
    private String jogadorAtual;

    /**
     * Se o timer está pausado.
     */
    private Boolean pausado;

    /**
     * Se o tempo esgotou.
     */
    private Boolean tempoEsgotado;

    /**
     * Username do jogador que perdeu por tempo (se aplicável).
     */
    private String jogadorQuePerderPorTempo;

    /**
     * Configuração do timer.
     */
    private TimerConfig config;

    /**
     * Cria um TimerResponse a partir de um GameTimer.
     */
    public static TimerResponse fromGameTimer(GameTimer timer) {
        if (timer == null) {
            return null;
        }

        return TimerResponse.builder()
                .usernameJogador1(timer.getUsernameJogador1())
                .usernameJogador2(timer.getUsernameJogador2())
                .tempoRestanteJogador1Ms(timer.calcularTempoRestanteAtual(timer.getUsernameJogador1()))
                .tempoRestanteJogador2Ms(timer.calcularTempoRestanteAtual(timer.getUsernameJogador2()))
                .jogadorAtual(timer.getJogadorAtual())
                .pausado(timer.getPausado())
                .tempoEsgotado(timer.getTempoEsgotado())
                .jogadorQuePerderPorTempo(timer.getJogadorQuePerderPorTempo())
                .config(timer.getConfig())
                .build();
    }
}
