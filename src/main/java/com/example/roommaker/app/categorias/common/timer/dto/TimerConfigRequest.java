package com.example.roommaker.app.categorias.common.timer.dto;

import com.example.roommaker.app.categorias.common.timer.TimerConfig;
import lombok.*;

/**
 * DTO para receber configuração de timer via HTTP.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimerConfigRequest {

    private Boolean timerAtivo;
    private Long tempoInicialSegundos;
    private Long incrementoPorLanceSegundos;

    /**
     * Converte para TimerConfig.
     */
    public TimerConfig toTimerConfig() {
        return TimerConfig.builder()
                .timerAtivo(timerAtivo != null ? timerAtivo : false)
                .tempoInicialSegundos(tempoInicialSegundos != null ? tempoInicialSegundos : 0L)
                .incrementoPorLanceSegundos(incrementoPorLanceSegundos != null ? incrementoPorLanceSegundos : 0L)
                .build();
    }

    /**
     * Cria a partir de TimerConfig.
     */
    public static TimerConfigRequest fromTimerConfig(TimerConfig config) {
        if (config == null) {
            return null;
        }
        return TimerConfigRequest.builder()
                .timerAtivo(config.getTimerAtivo())
                .tempoInicialSegundos(config.getTempoInicialSegundos())
                .incrementoPorLanceSegundos(config.getIncrementoPorLanceSegundos())
                .build();
    }
}
