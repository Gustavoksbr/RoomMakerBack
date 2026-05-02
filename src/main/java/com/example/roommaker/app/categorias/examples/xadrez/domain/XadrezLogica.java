package com.example.roommaker.app.categorias.examples.xadrez.domain;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.MotivoXadrez;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.ResultadoXadrez;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lógica pura do xadrez — sem I/O, testável de forma isolada.
 * Adaptado de xadrez-as-cegas/XadrezLogica.java
 */
public class XadrezLogica {

    private static final Pattern CAPTURA_SEM_X = Pattern.compile("^([a-h])([a-h][1-8].*)$");

    private static final Pattern SAN_LIBERAL = Pattern.compile(
            "^(" +
                    "O-O(-O)?" +
                    "|[KQRBN][a-h1-8]?[a-h1-8]?x?[a-h][1-8][+#]?" +
                    "|[a-h]x?[a-h][1-8](=[QRBN])?[+#]?" +
                    "|[a-h][1-8](=[QRBN])?[+#]?" +
                    ")$");

    public static Move resolverMove(Board board, String entrada) {
        Move m = parseSan(board, entrada);
        if (m != null)
            return m;
        Matcher mat = CAPTURA_SEM_X.matcher(entrada);
        if (mat.matches()) {
            String comX = mat.group(1) + "x" + mat.group(2);
            m = parseSan(board, comX);
            if (m != null)
                return m;
        }
        return null;
    }

    public static Move parseSan(Board board, String san) {
        try {
            MoveList lista = new MoveList(board.getFen());
            lista.loadFromSan(san);
            if (lista.isEmpty())
                return null;
            Move candidato = lista.get(0);
            List<Move> legais = MoveGenerator.generateLegalMoves(board);
            return legais.contains(candidato) ? candidato : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Classificacao classificarEntrada(Board board, String entrada) {
        if (entrada == null || entrada.isBlank()) {
            return new Classificacao(TipoEntrada.NOTACAO_INVALIDA, null);
        }
        Move move = resolverMove(board, entrada);
        if (move != null)
            return new Classificacao(TipoEntrada.VALIDO, move);

        String semSufixo = entrada.trim().replaceAll("[+#!?]+$", "");
        if (SAN_LIBERAL.matcher(semSufixo).matches()) {
            return new Classificacao(TipoEntrada.LANCE_ILEGAL, null);
        }
        Matcher mat = CAPTURA_SEM_X.matcher(semSufixo);
        if (mat.matches()) {
            String comX = mat.group(1) + "x" + mat.group(2);
            if (SAN_LIBERAL.matcher(comX).matches()) {
                return new Classificacao(TipoEntrada.LANCE_ILEGAL, null);
            }
        }
        return new Classificacao(TipoEntrada.NOTACAO_INVALIDA, null);
    }

    public static ResultadoFim verificarFim(Board board) {
        if (board.isMated()) {
            boolean vezBrancas = board.getSideToMove().value().equals("WHITE");
            ResultadoXadrez vencedor = vezBrancas ? ResultadoXadrez.VITORIA_PRETAS : ResultadoXadrez.VITORIA_BRANCAS;
            return new ResultadoFim(vencedor, MotivoXadrez.XEQUE_MATE);
        }
        if (board.isStaleMate())
            return new ResultadoFim(ResultadoXadrez.EMPATE, MotivoXadrez.AFOGAMENTO);
        if (board.isInsufficientMaterial())
            return new ResultadoFim(ResultadoXadrez.EMPATE, MotivoXadrez.MATERIAL_INSUFICIENTE);
        if (board.isRepetition())
            return new ResultadoFim(ResultadoXadrez.EMPATE, MotivoXadrez.REPETICAO_TRIPLA);
        if (board.getHalfMoveCounter() >= 100)
            return new ResultadoFim(ResultadoXadrez.EMPATE, MotivoXadrez.CINQUENTA_LANCES);
        return null;
    }

    public static Board reconstruirBoard(List<String> lances) {
        Board board = new Board();
        for (String san : lances) {
            Move move = resolverMove(board, san);
            if (move == null)
                throw new IllegalStateException("Lance corrompido no histórico: '" + san + "'");
            board.doMove(move);
        }
        return board;
    }

    public static String sanCanonica(Board boardAntes, Move move, List<String> lancesAnteriores) {
        try {
            Board temp = new Board();
            MoveList acumulada = new MoveList();
            for (String san : lancesAnteriores) {
                Move m = resolverMove(temp, san);
                if (m != null) {
                    temp.doMove(m);
                    acumulada.add(m);
                }
            }
            acumulada.add(move);
            String[] arr = acumulada.toSanArray();
            if (arr != null && arr.length > 0)
                return arr[arr.length - 1];
        } catch (Exception ignored) {
        }
        return move.toString();
    }


    public enum TipoEntrada {
        VALIDO, LANCE_ILEGAL, NOTACAO_INVALIDA
    }

    public record Classificacao(TipoEntrada tipo, Move move) {
    }

    public record ResultadoFim(ResultadoXadrez resultado, MotivoXadrez motivo) {
    }
    public static Board jogarSequencia(String... sans) {
        Board board = new Board();

        if (sans == null) {
            return board;
        }

        for (int i = 0; i < sans.length; i++) {
            String san = sans[i];
            Move move = resolverMove(board, san);

            if (move == null) {
                throw new IllegalArgumentException(
                        "Lance inválido na jogada " + (i + 1) + ": '" + san + "'"
                );
            }

            board.doMove(move);
        }

        return board;
    }
    public static String[] sanCanonicaDe(String... lances) {
        Board board = new Board();
        List<String> canonicos = new java.util.ArrayList<>();

        if (lances == null) {
            return new String[0];
        }

        for (int i = 0; i < lances.length; i++) {
            String entrada = lances[i];
            Move move = resolverMove(board, entrada);

            if (move == null) {
                throw new IllegalArgumentException(
                        "Lance inválido na jogada " + (i + 1) + ": '" + entrada + "'"
                );
            }

            // Gera SAN canônica considerando o histórico já normalizado.
            String san = sanCanonica(board, move, canonicos);
            canonicos.add(san);

            board.doMove(move);
        }

        return canonicos.toArray(new String[0]);
    }

}
