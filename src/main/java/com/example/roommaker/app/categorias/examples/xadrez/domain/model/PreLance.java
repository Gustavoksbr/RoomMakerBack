package com.example.roommaker.app.categorias.examples.xadrez.domain.model;

import lombok.*;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Um pré-lance enfileirado: o lance que o jogador quer fazer assim que chegar a
 * vez dele, escolhido antes de o adversário ter jogado.
 *
 * É guardado em COORDENADAS, e não em SAN, porque a SAN depende da posição —
 * e a posição em que o pré-lance vai cair ainda não existe quando ele é
 * enfileirado. "Cf3" pode ser ambíguo (ou ilegal) na posição real; "g1f3" não
 * depende de nada.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreLance {

    private static final Pattern CASA = Pattern.compile("^[a-h][1-8]$");
    private static final Pattern PROMOCAO = Pattern.compile("^[qrbn]$");
    private static final Pattern UCI = Pattern.compile("^([a-h][1-8])([a-h][1-8])([qrbn]?)$");

    /** Casa de origem, minúscula (ex: "e2"). */
    private String from;

    /** Casa de destino, minúscula (ex: "e4"). */
    private String to;

    /** Peça da promoção em minúscula ("q", "r", "b", "n") ou null. */
    private String promocao;

    /**
     * Normaliza e valida o formato de um pré-lance.
     *
     * Só valida FORMATO — a legalidade é impossível de saber aqui, já que
     * depende da posição futura. Ela é conferida na hora de aplicar.
     *
     * @return o pré-lance normalizado, ou null se o formato for inválido.
     */
    public static PreLance normalizar(String from, String to, String promocao) {
        if (from == null || to == null)
            return null;

        String origem = from.trim().toLowerCase(Locale.ROOT);
        String destino = to.trim().toLowerCase(Locale.ROOT);
        if (!CASA.matcher(origem).matches() || !CASA.matcher(destino).matches())
            return null;
        if (origem.equals(destino))
            return null;

        String promo = null;
        if (promocao != null && !promocao.isBlank()) {
            promo = promocao.trim().toLowerCase(Locale.ROOT);
            if (!PROMOCAO.matcher(promo).matches())
                return null;
        }

        return new PreLance(origem, destino, promo);
    }

    /** Formato compacto usado na persistência: "e2e4", "e7e8q". */
    public String toUci() {
        return from + to + (promocao != null ? promocao : "");
    }

    /** Inverso de {@link #toUci()}. Retorna null se a string for inválida. */
    public static PreLance deUci(String uci) {
        if (uci == null)
            return null;
        var m = UCI.matcher(uci.trim().toLowerCase(Locale.ROOT));
        if (!m.matches())
            return null;
        String promo = m.group(3).isEmpty() ? null : m.group(3);
        return new PreLance(m.group(1), m.group(2), promo);
    }
}
