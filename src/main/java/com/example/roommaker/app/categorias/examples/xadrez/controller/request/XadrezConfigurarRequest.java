package com.example.roommaker.app.categorias.examples.xadrez.controller.request;

import com.example.roommaker.app.categorias.examples.xadrez.domain.model.NotacaoXadrez;
import lombok.Data;

@Data
public class XadrezConfigurarRequest {
    private String usernameBrancas;
    private String usernamePretas;
    private NotacaoXadrez notacao; // opcional

    // Configurações de tempo (null = tempo infinito)
    private Integer tempoInicialBrancasMinutos;
    private Integer tempoInicialBrancasSegundos;
    private Integer incrementoBrancasSegundos;

    private Integer tempoInicialPretasMinutos;
    private Integer tempoInicialPretasSegundos;
    private Integer incrementoPretasSegundos;

    /**
     * Converte minutos + segundos para total em segundos.
     * Retorna null se ambos forem null (tempo infinito).
     */
    public Integer calcularTempoTotalBrancas() {
        if (tempoInicialBrancasMinutos == null && tempoInicialBrancasSegundos == null) {
            return null; // tempo infinito
        }
        int minutos = tempoInicialBrancasMinutos != null ? tempoInicialBrancasMinutos : 0;
        int segundos = tempoInicialBrancasSegundos != null ? tempoInicialBrancasSegundos : 0;
        return minutos * 60 + segundos;
    }

    public Integer calcularTempoTotalPretas() {
        if (tempoInicialPretasMinutos == null && tempoInicialPretasSegundos == null) {
            return null; // tempo infinito
        }
        int minutos = tempoInicialPretasMinutos != null ? tempoInicialPretasMinutos : 0;
        int segundos = tempoInicialPretasSegundos != null ? tempoInicialPretasSegundos : 0;
        return minutos * 60 + segundos;
    }
}
