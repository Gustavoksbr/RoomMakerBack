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

        // Tenta resolver com desambiguação manual
        m = tentarResolverComDesambiguacao(board, entrada);
        if (m != null)
            return m;

        Matcher mat = CAPTURA_SEM_X.matcher(entrada);
        if (mat.matches()) {
            String comX = mat.group(1) + "x" + mat.group(2);
            m = parseSan(board, comX);
            if (m != null)
                return m;
            // Tenta com desambiguação manual também
            m = tentarResolverComDesambiguacao(board, comX);
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

    /**
     * Verifica se há ambiguidade não resolvida na notação.
     * Retorna true se múltiplas peças do mesmo tipo podem ir para o destino
     * e o usuário não especificou qual peça mover (sem desambiguação).
     */
    private static boolean temAmbiguidadeNaoResolvida(Board board, String entrada, Move moveResolvido) {
        // Remove sufixos como +, #, !, ?
        String entradaLimpa = entrada.trim().replaceAll("[+#!?]+$", "");

        // Identifica o tipo de peça e casa de destino
        char primeiroCar = entradaLimpa.charAt(0);

        // Peões não têm ambiguidade do tipo que estamos procurando
        // (capturas de peão já exigem a coluna de origem)
        if (Character.isLowerCase(primeiroCar)) {
            return false;
        }

        // Roque não tem ambiguidade
        if (entradaLimpa.startsWith("O-O")) {
            return false;
        }

        // Verifica se é movimento de peça (K, Q, R, B, N)
        if (!"KQRBN".contains(String.valueOf(primeiroCar))) {
            return false;
        }

        // Rei não pode ter ambiguidade (só existe um)
        if (primeiroCar == 'K') {
            return false;
        }

        // Verifica se já tem desambiguação (coluna, linha ou ambos)
        // Exemplos: Nbd7 (tem 'b'), N1d7 (tem '1'), Nb1d7 (tem 'b1')
        // Formato: [Peça][desambiguação?][x?][destino]
        // Padrões possíveis:
        // - Nf3 (sem desambiguação)
        // - Ngf3 (desambiguação de coluna)
        // - N1f3 (desambiguação de linha)
        // - Ng1f3 (desambiguação completa)
        // - Nxf3 (captura sem desambiguação)
        // - Ngxf3 (captura com desambiguação)

        // Remove o 'x' se houver para facilitar a análise
        String semCaptura = entradaLimpa.replace("x", "");

        // Formato: [Peça][destino] sem desambiguação
        // Exemplo: Nf3, Rd1, Qd4
        Pattern semDesambiguacao = Pattern.compile("^[QRBN][a-h][1-8]$");
        if (semDesambiguacao.matcher(semCaptura).matches()) {
            // Não tem desambiguação, precisa verificar se há ambiguidade
            // Continua para a verificação abaixo
        } else {
            // Tem desambiguação (ou formato diferente), não é ambíguo
            return false;
        }

        // Agora verifica se há múltiplas peças do mesmo tipo que podem ir para o
        // destino
        List<Move> movimentosLegais = MoveGenerator.generateLegalMoves(board);

        // Filtra movimentos que vão para o mesmo destino e são da mesma peça
        com.github.bhlangonijr.chesslib.Square destino = moveResolvido.getTo();
        com.github.bhlangonijr.chesslib.Piece pecaMovida = board.getPiece(moveResolvido.getFrom());

        int contagem = 0;
        for (Move m : movimentosLegais) {
            if (m.getTo().equals(destino)) {
                com.github.bhlangonijr.chesslib.Piece peca = board.getPiece(m.getFrom());
                if (peca.equals(pecaMovida)) {
                    contagem++;
                }
            }
        }

        // Se mais de uma peça do mesmo tipo pode ir para o destino, há ambiguidade
        return contagem > 1;
    }

    public static Classificacao classificarEntrada(Board board, String entrada) {
        if (entrada == null || entrada.isBlank()) {
            return new Classificacao(TipoEntrada.NOTACAO_INVALIDA, null);
        }
        Move move = resolverMove(board, entrada);
        if (move != null) {
            // Verifica se há ambiguidade não resolvida
            if (temAmbiguidadeNaoResolvida(board, entrada, move)) {
                return new Classificacao(TipoEntrada.LANCE_AMBIGUO, null);
            }
            return new Classificacao(TipoEntrada.VALIDO, move);
        }

        // Se o move é null, pode ser porque é ambíguo ou ilegal
        // Verifica se é um lance potencialmente ambíguo
        String semSufixo = entrada.trim().replaceAll("[+#!?]+$", "");
        if (podeSerLanceAmbiguo(board, semSufixo)) {
            return new Classificacao(TipoEntrada.LANCE_AMBIGUO, null);
        }

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

    /**
     * Verifica se uma entrada que não foi resolvida pode ser um lance ambíguo.
     * Isso acontece quando a notação está correta mas falta desambiguação.
     */
    private static boolean podeSerLanceAmbiguo(Board board, String entrada) {
        // Remove o 'x' se houver
        String semCaptura = entrada.replace("x", "");

        // Verifica se é um movimento de peça sem desambiguação
        // Formato: [Peça][destino] (ex: Rd1, Qd4, Bd3, Nf3)
        Pattern semDesambiguacao = Pattern.compile("^[QRBN]([a-h][1-8])$");
        Matcher matcher = semDesambiguacao.matcher(semCaptura);

        if (!matcher.matches()) {
            return false;
        }

        char tipoPeca = semCaptura.charAt(0);
        String destinoStr = matcher.group(1);

        // Converte o destino para Square
        com.github.bhlangonijr.chesslib.Square destino;
        try {
            destino = com.github.bhlangonijr.chesslib.Square.valueOf(destinoStr.toUpperCase());
        } catch (Exception e) {
            return false;
        }

        // Determina o tipo de peça
        com.github.bhlangonijr.chesslib.PieceType pieceType;
        switch (tipoPeca) {
            case 'Q':
                pieceType = com.github.bhlangonijr.chesslib.PieceType.QUEEN;
                break;
            case 'R':
                pieceType = com.github.bhlangonijr.chesslib.PieceType.ROOK;
                break;
            case 'B':
                pieceType = com.github.bhlangonijr.chesslib.PieceType.BISHOP;
                break;
            case 'N':
                pieceType = com.github.bhlangonijr.chesslib.PieceType.KNIGHT;
                break;
            default:
                return false;
        }

        // Conta quantas peças do mesmo tipo podem ir para o destino
        List<Move> movimentosLegais = MoveGenerator.generateLegalMoves(board);
        com.github.bhlangonijr.chesslib.Piece pecaProcurada = com.github.bhlangonijr.chesslib.Piece
                .make(board.getSideToMove(), pieceType);

        int contagem = 0;
        for (Move m : movimentosLegais) {
            if (m.getTo().equals(destino)) {
                com.github.bhlangonijr.chesslib.Piece peca = board.getPiece(m.getFrom());
                if (peca.equals(pecaProcurada)) {
                    contagem++;
                }
            }
        }

        // Se mais de uma peça do mesmo tipo pode ir para o destino, é ambíguo
        return contagem > 1;
    }

    /**
     * Tenta resolver um movimento com desambiguação manual.
     * Usado quando o parseSan falha, mas a notação parece válida.
     */
    private static Move tentarResolverComDesambiguacao(Board board, String entrada) {
        // Remove sufixos e 'x'
        String limpo = entrada.trim().replaceAll("[+#!?]+$", "").replace("x", "");

        // Padrão para movimento com desambiguação: [Peça][coluna?][linha?][destino]
        // Exemplos: Rad1, R1d1, Ra1d1, Nbd7, N1f3, Ke2
        Pattern comDesambiguacao = Pattern.compile("^([KQRBN])([a-h])?([1-8])?([a-h][1-8])$");
        Matcher matcher = comDesambiguacao.matcher(limpo);

        if (!matcher.matches()) {
            return null;
        }

        char tipoPeca = matcher.group(1).charAt(0);
        String colunaOrigem = matcher.group(2); // pode ser null
        String linhaOrigem = matcher.group(3); // pode ser null
        String destinoStr = matcher.group(4);

        // Converte o destino
        com.github.bhlangonijr.chesslib.Square destino;
        try {
            destino = com.github.bhlangonijr.chesslib.Square.valueOf(destinoStr.toUpperCase());
        } catch (Exception e) {
            return null;
        }

        // Determina o tipo de peça
        com.github.bhlangonijr.chesslib.PieceType pieceType;
        switch (tipoPeca) {
            case 'K':
                pieceType = com.github.bhlangonijr.chesslib.PieceType.KING;
                break;
            case 'Q':
                pieceType = com.github.bhlangonijr.chesslib.PieceType.QUEEN;
                break;
            case 'R':
                pieceType = com.github.bhlangonijr.chesslib.PieceType.ROOK;
                break;
            case 'B':
                pieceType = com.github.bhlangonijr.chesslib.PieceType.BISHOP;
                break;
            case 'N':
                pieceType = com.github.bhlangonijr.chesslib.PieceType.KNIGHT;
                break;
            default:
                return null;
        }

        // Busca o movimento correto
        List<Move> movimentosLegais = MoveGenerator.generateLegalMoves(board);
        com.github.bhlangonijr.chesslib.Piece pecaProcurada = com.github.bhlangonijr.chesslib.Piece.make(
                board.getSideToMove(),
                pieceType);

        Move movimentoEncontrado = null;
        for (Move m : movimentosLegais) {
            if (!m.getTo().equals(destino)) {
                continue;
            }

            com.github.bhlangonijr.chesslib.Piece peca = board.getPiece(m.getFrom());
            if (!peca.equals(pecaProcurada)) {
                continue;
            }

            // Verifica desambiguação de coluna
            if (colunaOrigem != null) {
                String colunaMovimento = m.getFrom().getFile().getNotation();
                if (!colunaMovimento.equals(colunaOrigem)) {
                    continue;
                }
            }

            // Verifica desambiguação de linha
            if (linhaOrigem != null) {
                String linhaMovimento = m.getFrom().getRank().getNotation();
                if (!linhaMovimento.equals(linhaOrigem)) {
                    continue;
                }
            }

            // Se já encontramos um movimento e encontramos outro, há ambiguidade
            if (movimentoEncontrado != null) {
                return null; // Ambíguo
            }

            movimentoEncontrado = m;
        }

        return movimentoEncontrado;
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

    /**
     * Reconstrói o board a partir de lances armazenados em uma notação específica.
     * Converte os lances para inglês antes de processar.
     */
    public static Board reconstruirBoard(List<String> lances,
            com.example.roommaker.app.categorias.examples.xadrez.domain.model.NotacaoXadrez notacao) {
        Board board = new Board();
        for (String san : lances) {
            // Converte para inglês antes de processar
            String sanIngles = NotacaoConverter.paraIngles(san, notacao);
            Move move = resolverMove(board, sanIngles);
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

    /**
     * Gera a SAN canônica (em inglês) para um movimento, considerando lances
     * anteriores em uma notação específica.
     */
    public static String sanCanonica(Board boardAntes, Move move, List<String> lancesAnteriores,
            com.example.roommaker.app.categorias.examples.xadrez.domain.model.NotacaoXadrez notacao) {
        try {
            Board temp = new Board();
            MoveList acumulada = new MoveList();
            for (String san : lancesAnteriores) {
                // Converte para inglês antes de processar
                String sanIngles = NotacaoConverter.paraIngles(san, notacao);
                Move m = resolverMove(temp, sanIngles);
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
        VALIDO, LANCE_ILEGAL, NOTACAO_INVALIDA, LANCE_AMBIGUO
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
                        "Lance inválido na jogada " + (i + 1) + ": '" + san + "'");
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
                        "Lance inválido na jogada " + (i + 1) + ": '" + entrada + "'");
            }

            // Gera SAN canônica considerando o histórico já normalizado.
            String san = sanCanonica(board, move, canonicos);
            canonicos.add(san);

            board.doMove(move);
        }

        return canonicos.toArray(new String[0]);
    }

    /**
     * Verifica se um jogador tem material suficiente para dar xeque-mate.
     * Retorna false apenas nos casos de material insuficiente:
     * - Rei vs Rei
     * - Rei + Bispo vs Rei
     * - Rei + Cavalo vs Rei
     * - Rei + Bispo vs Rei + Bispo (bispos da mesma cor)
     */
    public static boolean temMaterialSuficiente(Board board, boolean brancas) {
        com.github.bhlangonijr.chesslib.Side lado = brancas
                ? com.github.bhlangonijr.chesslib.Side.WHITE
                : com.github.bhlangonijr.chesslib.Side.BLACK;

        long pecas = board.getBitboard(lado);
        long reis = board.getBitboard(
                com.github.bhlangonijr.chesslib.Piece.make(lado, com.github.bhlangonijr.chesslib.PieceType.KING));
        long peoes = board.getBitboard(
                com.github.bhlangonijr.chesslib.Piece.make(lado, com.github.bhlangonijr.chesslib.PieceType.PAWN));
        long torres = board.getBitboard(
                com.github.bhlangonijr.chesslib.Piece.make(lado, com.github.bhlangonijr.chesslib.PieceType.ROOK));
        long damas = board.getBitboard(
                com.github.bhlangonijr.chesslib.Piece.make(lado, com.github.bhlangonijr.chesslib.PieceType.QUEEN));
        long bispos = board.getBitboard(
                com.github.bhlangonijr.chesslib.Piece.make(lado, com.github.bhlangonijr.chesslib.PieceType.BISHOP));
        long cavalos = board.getBitboard(
                com.github.bhlangonijr.chesslib.Piece.make(lado, com.github.bhlangonijr.chesslib.PieceType.KNIGHT));

        // Se tem peão, torre ou dama, sempre tem material suficiente
        if (peoes != 0 || torres != 0 || damas != 0) {
            return true;
        }

        // Conta bispos e cavalos
        int numBispos = Long.bitCount(bispos);
        int numCavalos = Long.bitCount(cavalos);

        // Rei sozinho
        if (numBispos == 0 && numCavalos == 0) {
            return false;
        }

        // Rei + Bispo ou Rei + Cavalo
        if ((numBispos == 1 && numCavalos == 0) || (numBispos == 0 && numCavalos == 1)) {
            return false;
        }

        // Qualquer outra combinação tem material suficiente
        return true;
    }

}
