package com.example.roommaker.app.categorias.examples.xadrez.controller.request;

import lombok.Data;

import java.util.List;

/**
 * A fila de pré-lances INTEIRA, e não um acréscimo.
 *
 * Mandar sempre o estado completo torna a operação idempotente: um pacote
 * duplicado pela rede, ou dois pacotes que chegam fora de ordem, não conseguem
 * embaralhar a fila do jogador — o último a chegar simplesmente vale.
 */
@Data
public class XadrezPreLancesRequest {

    private List<PreLanceDto> fila;

    @Data
    public static class PreLanceDto {
        private String from;
        private String to;
        /** "q", "r", "b" ou "n", ou null. */
        private String promocao;
    }
}
