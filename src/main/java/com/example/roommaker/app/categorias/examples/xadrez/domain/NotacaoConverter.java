package com.example.roommaker.app.categorias.examples.xadrez.domain;

import com.example.roommaker.app.categorias.examples.xadrez.domain.model.NotacaoXadrez;

/**
 * Conversor entre notação portuguesa e inglesa de xadrez.
 * 
 * Mapeamento:
 * - Rei: R (PT) <-> K (EN)
 * - Dama: D (PT) <-> Q (EN)
 * - Torre: T (PT) <-> R (EN)
 * - Bispo: B (PT) <-> B (EN) [igual]
 * - Cavalo: C (PT) <-> N (EN)
 * - Peão: sem letra em ambos
 */
public class NotacaoConverter {

    /**
     * Converte uma jogada da notação configurada para notação inglesa (SAN padrão).
     * Se a notação já for inglesa, retorna a entrada sem modificação.
     * 
     * @param entrada Lance na notação configurada (portuguesa ou inglesa)
     * @param notacao Notação configurada na sala
     * @return Lance em notação inglesa (SAN padrão)
     */
    public static String paraIngles(String entrada, NotacaoXadrez notacao) {
        if (entrada == null || notacao == null || notacao == NotacaoXadrez.INGLESA) {
            return entrada;
        }

        // Notação portuguesa -> inglesa
        // Usa placeholders temporários para evitar conflitos
        String resultado = entrada;

        resultado = resultado.replace("R", "§REI§"); // Rei (placeholder temporário)
        resultado = resultado.replace("D", "Q"); // Dama
        resultado = resultado.replace("T", "R"); // Torre
        resultado = resultado.replace("C", "N"); // Cavalo
        resultado = resultado.replace("§REI§", "K"); // Rei (substitui placeholder)
        // B permanece B (Bispo)

        return resultado;
    }

    /**
     * Converte uma jogada da notação inglesa (SAN padrão) para a notação
     * configurada.
     * Se a notação for inglesa, retorna a entrada sem modificação.
     * 
     * @param sanIngles Lance em notação inglesa (SAN padrão)
     * @param notacao   Notação configurada na sala
     * @return Lance na notação configurada
     */
    public static String deIngles(String sanIngles, NotacaoXadrez notacao) {
        if (sanIngles == null || notacao == null || notacao == NotacaoXadrez.INGLESA) {
            return sanIngles;
        }

        // Notação inglesa -> portuguesa
        // Usa placeholders temporários para evitar conflitos
        String resultado = sanIngles;

        resultado = resultado.replace("R", "§TORRE§"); // Torre (placeholder temporário)
        resultado = resultado.replace("K", "R"); // Rei
        resultado = resultado.replace("Q", "D"); // Dama
        resultado = resultado.replace("N", "C"); // Cavalo
        resultado = resultado.replace("§TORRE§", "T"); // Torre (substitui placeholder)
        // B permanece B (Bispo)

        return resultado;
    }
}
