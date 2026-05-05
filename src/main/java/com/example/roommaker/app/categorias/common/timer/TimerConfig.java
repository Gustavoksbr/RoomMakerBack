package com.example.roommaker.app.categorias.common.timer;

import lombok.*;

/**
 * Configuração personalizável de timer para jogos.
 * O dono da sala define:
 * - Tempo inicial para cada jogador (em segundos)
 * - Incremento por lance (segundos adicionados após cada jogada)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimerConfig {

    /**
     * Tempo inicial para cada jogador em segundos.
     * Exemplo: 300 = 5 minutos
     */
    private Long tempoInicialSegundos;

    /**
     * Incremento em segundos adicionado após cada lance.
     * Exemplo: 3 = adiciona 3 segundos após cada jogada (estilo Fischer)
     */
    @Builder.Default
    private Long incrementoPorLanceSegundos = 0L;

    /**
     * Se true, o timer está ativo para esta sala.
     * Se false, o jogo não tem limite de tempo.
     */
    @Builder.Default
    private Boolean timerAtivo = false;

    /**
     * Valida se a configuração é válida.
     */
    public boolean isValida() {
        if (!timerAtivo) {
            return true; // Timer desativado é sempre válido
        }
        return tempoInicialSegundos != null &&
                tempoInicialSegundos > 0 &&
                incrementoPorLanceSegundos != null &&
                incrementoPorLanceSegundos >= 0;
    }

    /**
     * Cria uma configuração padrão sem timer.
     */
    public static TimerConfig semTimer() {
        return TimerConfig.builder()
                .timerAtivo(false)
                .tempoInicialSegundos(0L)
                .incrementoPorLanceSegundos(0L)
                .build();
    }

    /**
     * Cria uma configuração padrão com timer (5 minutos + 3 segundos por lance).
     */
    public static TimerConfig padrao() {
        return TimerConfig.builder()
                .timerAtivo(true)
                .tempoInicialSegundos(300L) // 5 minutos
                .incrementoPorLanceSegundos(3L)
                .build();
    }
}
