package com.example.roommaker.app.categorias.common.timer;

import lombok.*;

import java.time.Instant;

/**
 * Estado do timer de um jogo em andamento.
 * Mantém o tempo restante de cada jogador e controla o turno atual.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameTimer {

    /**
     * Identificador único da sala (nomeSala + usernameDono).
     */
    private String salaId;

    /**
     * Username do jogador 1 (geralmente o dono ou jogador de brancas).
     */
    private String usernameJogador1;

    /**
     * Username do jogador 2 (geralmente o oponente ou jogador de pretas).
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
     * Username do jogador que está no turno atual.
     */
    private String jogadorAtual;

    /**
     * Timestamp de quando o turno atual começou.
     */
    private Instant inicioTurnoAtual;

    /**
     * Configuração do timer (tempo inicial e incremento).
     */
    private TimerConfig config;

    /**
     * Se true, o timer está pausado (partida não iniciada ou encerrada).
     */
    @Builder.Default
    private Boolean pausado = true;

    /**
     * Se true, o tempo de algum jogador acabou.
     */
    @Builder.Default
    private Boolean tempoEsgotado = false;

    /**
     * Username do jogador que perdeu por tempo (se aplicável).
     */
    private String jogadorQuePerderPorTempo;

    /**
     * Calcula o tempo restante atual do jogador no turno,
     * considerando o tempo decorrido desde o início do turno.
     */
    public Long calcularTempoRestanteAtual(String username) {
        if (pausado || tempoEsgotado) {
            return username.equals(usernameJogador1) ? tempoRestanteJogador1Ms : tempoRestanteJogador2Ms;
        }

        if (!username.equals(jogadorAtual)) {
            // Não é o turno deste jogador, retorna o tempo salvo
            return username.equals(usernameJogador1) ? tempoRestanteJogador1Ms : tempoRestanteJogador2Ms;
        }

        // É o turno deste jogador, calcula o tempo decorrido
        long tempoDecorridoMs = Instant.now().toEpochMilli() - inicioTurnoAtual.toEpochMilli();
        long tempoRestante = username.equals(usernameJogador1) ? tempoRestanteJogador1Ms : tempoRestanteJogador2Ms;
        long tempoAtual = tempoRestante - tempoDecorridoMs;

        return Math.max(0, tempoAtual);
    }

    /**
     * Inicia o timer para o jogador especificado.
     */
    public void iniciar(String primeiroJogador) {
        this.jogadorAtual = primeiroJogador;
        this.inicioTurnoAtual = Instant.now();
        this.pausado = false;
    }

    /**
     * Pausa o timer (salva o tempo atual).
     */
    public void pausar() {
        if (!pausado && !tempoEsgotado) {
            // Atualiza o tempo restante do jogador atual antes de pausar
            atualizarTempoRestante();
        }
        this.pausado = true;
    }

    /**
     * Troca o turno para o outro jogador e adiciona o incremento.
     */
    public void trocarTurno() {
        if (pausado || tempoEsgotado) {
            return;
        }

        // Atualiza o tempo do jogador atual
        atualizarTempoRestante();

        // Adiciona incremento ao jogador que acabou de jogar
        if (config.getIncrementoPorLanceSegundos() > 0) {
            long incrementoMs = config.getIncrementoPorLanceSegundos() * 1000;
            if (jogadorAtual.equals(usernameJogador1)) {
                tempoRestanteJogador1Ms += incrementoMs;
            } else {
                tempoRestanteJogador2Ms += incrementoMs;
            }
        }

        // Troca para o outro jogador
        jogadorAtual = jogadorAtual.equals(usernameJogador1) ? usernameJogador2 : usernameJogador1;
        inicioTurnoAtual = Instant.now();
    }

    /**
     * Atualiza o tempo restante do jogador atual baseado no tempo decorrido.
     */
    private void atualizarTempoRestante() {
        long tempoDecorridoMs = Instant.now().toEpochMilli() - inicioTurnoAtual.toEpochMilli();

        if (jogadorAtual.equals(usernameJogador1)) {
            tempoRestanteJogador1Ms -= tempoDecorridoMs;
            if (tempoRestanteJogador1Ms <= 0) {
                tempoRestanteJogador1Ms = 0L;
                tempoEsgotado = true;
                jogadorQuePerderPorTempo = usernameJogador1;
            }
        } else {
            tempoRestanteJogador2Ms -= tempoDecorridoMs;
            if (tempoRestanteJogador2Ms <= 0) {
                tempoRestanteJogador2Ms = 0L;
                tempoEsgotado = true;
                jogadorQuePerderPorTempo = usernameJogador2;
            }
        }
    }

    /**
     * Verifica se o tempo de algum jogador esgotou.
     */
    public boolean verificarTimeout() {
        if (pausado || tempoEsgotado) {
            return tempoEsgotado;
        }

        atualizarTempoRestante();
        return tempoEsgotado;
    }

    /**
     * Cria um novo timer a partir da configuração.
     */
    public static GameTimer criar(String salaId, String jogador1, String jogador2, TimerConfig config) {
        long tempoInicialMs = config.getTempoInicialSegundos() * 1000;

        return GameTimer.builder()
                .salaId(salaId)
                .usernameJogador1(jogador1)
                .usernameJogador2(jogador2)
                .tempoRestanteJogador1Ms(tempoInicialMs)
                .tempoRestanteJogador2Ms(tempoInicialMs)
                .config(config)
                .pausado(true)
                .tempoEsgotado(false)
                .build();
    }
}
