package com.example.roommaker.app.categorias.examples.xadrez.domain;

import com.example.roommaker.app.categorias.examples.xadrez.domain.model.NotacaoXadrez;

import java.util.regex.Pattern;

/**
 * Validador de notação de xadrez.
 * Garante que o lance está escrito na notação correta (portuguesa ou inglesa).
 */
public class NotacaoValidator {

    // Regex para notação INGLESA: permite K Q R B N (Rei, Dama, Torre, Bispo,
    // Cavalo)
    // NÃO permite R D T C (letras portuguesas)
    private static final Pattern PATTERN_INGLESA = Pattern.compile("^[a-hKQRBNO1-8=+#x\\-!?]+$");

    // Regex para notação PORTUGUESA: permite R D T B C (Rei, Dama, Torre, Bispo,
    // Cavalo)
    // NÃO permite K Q N (letras inglesas, exceto O para roque)
    private static final Pattern PATTERN_PORTUGUESA = Pattern.compile("^[a-hRDTBCO1-8=+#x\\-!?]+$");

    /**
     * Valida se o lance está escrito na notação correta.
     * 
     * @param san     Lance em notação algébrica
     * @param notacao Notação esperada (PORTUGUESA ou INGLESA)
     * @return true se o lance está na notação correta, false caso contrário
     */
    public static boolean validarNotacao(String san, NotacaoXadrez notacao) {
        if (san == null || san.isBlank()) {
            return false;
        }

        Pattern pattern = (notacao == NotacaoXadrez.PORTUGUESA)
                ? PATTERN_PORTUGUESA
                : PATTERN_INGLESA;

        return pattern.matcher(san.trim()).matches();
    }

    /**
     * Verifica se o lance contém caracteres da notação errada.
     * 
     * @param san     Lance em notação algébrica
     * @param notacao Notação esperada
     * @return Mensagem de erro descritiva, ou null se estiver correto
     */
    public static String obterErroNotacao(String san, NotacaoXadrez notacao) {
        if (san == null || san.isBlank()) {
            return null;
        }

        String sanTrimmed = san.trim();

        if (notacao == NotacaoXadrez.PORTUGUESA) {
            // Verifica se contém letras inglesas (K, Q, N)
            if (sanTrimmed.matches(".*[KQN].*")) {
                StringBuilder erro = new StringBuilder("Lance usa notação inglesa. Use notação portuguesa: ");
                if (sanTrimmed.contains("K"))
                    erro.append("K→R (rei), ");
                if (sanTrimmed.contains("Q"))
                    erro.append("Q→D (dama), ");
                if (sanTrimmed.contains("N"))
                    erro.append("N→C (cavalo), ");
                return erro.substring(0, erro.length() - 2);
            }
        } else {
            // Verifica se contém letras portuguesas (R, D, T, C) em posições de peça
            // R pode ser torre em inglês, então verificamos contexto
            if (sanTrimmed.matches(".*[DTC].*")) {
                StringBuilder erro = new StringBuilder("Lance usa notação portuguesa. Use notação inglesa: ");
                if (sanTrimmed.contains("D"))
                    erro.append("D→Q (dama), ");
                if (sanTrimmed.contains("T"))
                    erro.append("T→R (torre), ");
                if (sanTrimmed.contains("C"))
                    erro.append("C→N (cavalo), ");
                return erro.substring(0, erro.length() - 2);
            }
        }

        return null;
    }
}
