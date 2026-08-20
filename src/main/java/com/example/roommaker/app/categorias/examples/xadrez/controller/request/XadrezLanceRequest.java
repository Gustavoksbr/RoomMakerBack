package com.example.roommaker.app.categorias.examples.xadrez.controller.request;

import lombok.Data;

/**
 * Um lance pode chegar de duas formas, dependendo do modo da partida:
 *
 * <ul>
 * <li>ÀS CEGAS: {@code san} — o jogador digitou "Nf3".</li>
 * <li>VISUAL: {@code from}/{@code to}/{@code promocao} — o jogador arrastou uma
 * peça, e duas casas é tudo o que um clique produz.</li>
 * </ul>
 *
 * As coordenadas têm precedência quando as duas vêm preenchidas.
 */
@Data
public class XadrezLanceRequest {
    private String san;

    private String from;
    private String to;
    /** "q", "r", "b" ou "n". Null quando o lance não é uma promoção. */
    private String promocao;

    public boolean temCoordenadas() {
        return from != null && !from.isBlank() && to != null && !to.isBlank();
    }
}
