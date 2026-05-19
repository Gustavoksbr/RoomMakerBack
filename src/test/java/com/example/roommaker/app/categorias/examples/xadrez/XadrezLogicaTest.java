package com.example.roommaker.app.categorias.examples.xadrez;

import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezLogica;
import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezLogica.*;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.MotivoXadrez;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.ResultadoXadrez;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da lógica pura de xadrez.
 * Não requer Spring nem MongoDB — executa em memória.
 */
class XadrezLogicaTest {

    // =========================================================================
    // resolverMove
    // =========================================================================

    @Nested
    @DisplayName("resolverMove")
    class ResolverMove {

        @Test
        @DisplayName("aceita SAN padrão: e4")
        void sanSimples() {
            Board board = new Board();
            Move move = XadrezLogica.resolverMove(board, "e4");
            assertNotNull(move, "e4 deve ser aceito na posição inicial");
        }

        @Test
        @DisplayName("aceita captura com x: exd5")
        void capturaComX() {
            Board board = XadrezLogica.jogarSequencia("e4", "d5");
            Move move = XadrezLogica.resolverMove(board, "exd5");
            assertNotNull(move, "exd5 deve ser aceito");
        }

        @Test
        @DisplayName("aceita captura sem x: ed5 equivale a exd5")
        void capturaSemX() {
            Board board = XadrezLogica.jogarSequencia("e4", "d5");
            Move comX = XadrezLogica.resolverMove(board, "exd5");
            Move semX = XadrezLogica.resolverMove(board, "ed5");
            assertNotNull(semX, "ed5 deve ser aceito");
            assertEquals(comX, semX, "ed5 e exd5 devem resolver para o mesmo Move");
        }

        @Test
        @DisplayName("rejeita lance inválido: asasd")
        void lanceTotalmenteInvalido() {
            Board board = new Board();
            assertNull(XadrezLogica.resolverMove(board, "asasd"));
        }

        @Test
        @DisplayName("rejeita lance ilegal: e5 na posição inicial (brancas)")
        void lanceIlegal() {
            Board board = new Board();
            assertNull(XadrezLogica.resolverMove(board, "e5"),
                    "Peão branco não pode ir para e5 de imediato");
        }

        @Test
        @DisplayName("rejeita string vazia")
        void stringVazia() {
            Board board = new Board();
            assertNull(XadrezLogica.resolverMove(board, ""));
        }

        @Test
        @DisplayName("aceita roque curto: O-O")
        void roqueCurto() {
            Board board = XadrezLogica.jogarSequencia("e4", "e5", "Nf3", "Nc6", "Bc4", "Bc5");
            Move move = XadrezLogica.resolverMove(board, "O-O");
            assertNotNull(move, "O-O deve ser aceito quando o caminho está livre");
        }

        @Test
        @DisplayName("aceita promoção: e8=Q")
        void promocao() {
            Board board = new Board();
            board.loadFromFen("8/4P3/8/8/8/8/8/4K1k1 w - - 0 1");
            Move move = XadrezLogica.resolverMove(board, "e8=Q");
            assertNotNull(move, "e8=Q deve ser aceito");
        }

        @Test
        @DisplayName("aceita captura sem x com promoção: ed8=Q equivale a exd8=Q")
        void capturaSemXComPromocao() {
            Board board = new Board();
            board.loadFromFen("3r4/4P3/8/8/8/8/8/4K1k1 w - - 0 1");
            Move comX = XadrezLogica.resolverMove(board, "exd8=Q");
            Move semX = XadrezLogica.resolverMove(board, "ed8=Q");
            assertNotNull(semX, "ed8=Q deve ser aceito");
            assertEquals(comX, semX, "ed8=Q e exd8=Q devem resolver para o mesmo Move");
        }
    }

    // =========================================================================
    // sanCanonica
    // =========================================================================

    @Nested
    @DisplayName("sanCanonica — normalização")
    class SanCanonica {

        @Test
        @DisplayName("ed5 normaliza para exd5 no histórico")
        void normalizaCapturaSemX() {
            String[] san = XadrezLogica.sanCanonicaDe("e4", "d5", "ed5");
            assertEquals("exd5", san[2], "ed5 deve ser normalizado para exd5");
        }

        @Test
        @DisplayName("xeque recebe sufixo +")
        void xequeRecebeSufixo() {
            String[] san = XadrezLogica.sanCanonicaDe("e4", "e5", "Bc4", "Nc6", "Qh5", "Nf6", "Qxf7");
            assertTrue(san[6].contains("+") || san[6].contains("#"),
                    "Qxf7 deve ter sufixo de xeque, foi: " + san[6]);
        }

        @Test
        @DisplayName("xeque-mate recebe sufixo #")
        void xequeMateRecebeSufixo() {
            String[] san = XadrezLogica.sanCanonicaDe("f3", "e5", "g4", "Qh4");
            assertEquals("Qh4#", san[3], "Qh4 deve ser xeque-mate");
        }
    }

    // =========================================================================
    // verificarFim
    // =========================================================================

    @Nested
    @DisplayName("verificarFim")
    class VerificarFim {

        @Test
        @DisplayName("jogo em andamento retorna null")
        void jogoEmAndamento() {
            Board board = new Board();
            assertNull(XadrezLogica.verificarFim(board));
        }

        @Test
        @DisplayName("Fool's mate: VITORIA_PRETAS por XEQUE_MATE")
        void foolsMate() {
            Board board = XadrezLogica.jogarSequencia("f3", "e5", "g4", "Qh4");
            ResultadoFim fim = XadrezLogica.verificarFim(board);
            assertNotNull(fim);
            assertEquals(ResultadoXadrez.VITORIA_PRETAS, fim.resultado());
            assertEquals(MotivoXadrez.XEQUE_MATE, fim.motivo());
        }

        @Test
        @DisplayName("Scholar's mate: VITORIA_BRANCAS por XEQUE_MATE")
        void scholarsMate() {
            Board board = XadrezLogica.jogarSequencia("e4", "e5", "Bc4", "Nc6", "Qh5", "Nf6", "Qxf7");
            ResultadoFim fim = XadrezLogica.verificarFim(board);
            assertNotNull(fim);
            assertEquals(ResultadoXadrez.VITORIA_BRANCAS, fim.resultado());
            assertEquals(MotivoXadrez.XEQUE_MATE, fim.motivo());
        }

        @Test
        @DisplayName("afogamento: EMPATE por AFOGAMENTO")
        void afogamento() {
            Board board = new Board();
            board.loadFromFen("k7/8/1QK5/8/8/8/8/8 b - - 0 1");
            ResultadoFim fim = XadrezLogica.verificarFim(board);
            assertNotNull(fim);
            assertEquals(ResultadoXadrez.EMPATE, fim.resultado());
            assertEquals(MotivoXadrez.AFOGAMENTO, fim.motivo());
        }

        @Test
        @DisplayName("material insuficiente: EMPATE por MATERIAL_INSUFICIENTE")
        void materialInsuficiente() {
            Board board = new Board();
            board.loadFromFen("8/8/4k3/8/8/4K3/8/8 w - - 0 1");
            ResultadoFim fim = XadrezLogica.verificarFim(board);
            assertNotNull(fim);
            assertEquals(ResultadoXadrez.EMPATE, fim.resultado());
            assertEquals(MotivoXadrez.MATERIAL_INSUFICIENTE, fim.motivo());
        }

        @Test
        @DisplayName("repetição tripla: EMPATE por REPETICAO_TRIPLA")
        void repeticaoTripla() {
            Board board = XadrezLogica.jogarSequencia(
                    "b4", "c5", "h3", "Nc6", "Rh2", "e5",
                    "Ba3", "e4", "f4", "exf3", "g4", "Qf6",
                    "Kf2", "Nb8", "Bb2", "Qe7", "Nc3", "Qf6",
                    "Nb1", "Nc6", "Bc1", "Be7", "Bb2", "Bf8",
                    "Ba3", "Nh6", "Nc3", "Ng8", "Nb1");
            ResultadoFim fim = XadrezLogica.verificarFim(board);
            assertNotNull(fim);
            assertEquals(ResultadoXadrez.EMPATE, fim.resultado());
            assertEquals(MotivoXadrez.REPETICAO_TRIPLA, fim.motivo());
        }

        @Test
        @DisplayName("50 lances sem captura ou peão: EMPATE por CINQUENTA_LANCES")
        void cinquentaLances() {
            Board board = XadrezLogica.jogarSequencia(
                    "d4", "Nf6", "c4", "e6", "Nc3", "Bb4", "e3", "O-O",
                    "Bd3", "d5", "Nf3", "c5", "O-O", "Nc6", "a3", "Bxc3",
                    "bxc3", "dxc4", "Bxc4", "Qc7", "Bb5", "a6", "Bd3", "e5",
                    "Qc2", "b5", "e4", "exd4", "cxd4", "c4", "Be2", "Re8",
                    "d5", "Ne5", "Bb2", "Nxf3+", "Bxf3", "Bg4", "d6", "Qc6",
                    "Bxg4", "Nxg4", "h3", "Ne5", "Rad1", "Nd7", "Rfe1", "a5",
                    "Bc3", "f6", "Qd2", "Rxe4", "Rxe4", "Qxe4", "Bxa5", "Re8",
                    "Bb4", "Re5", "f3", "Qe3+", "Qxe3", "Rxe3", "Kf2", "Re8",
                    "f4", "Kf7", "Rd5", "Rb8", "Ke3", "Ke6", "Rd1", "f5",
                    "g4", "fxg4", "hxg4", "Nf6", "f5+", "Kd7", "g5", "Re8+",
                    "Kf4", "Nh5+", "Kg4", "g6", "fxg6", "hxg6", "Rd5", "Ng7",
                    "Rxb5", "Nf5", "Rb7+", "Ke6", "Rc7", "Rh8", "Kf3", "Rh3+",
                    "Kf2", "Rh2+", "Ke1", "Rh1+", "Kf2", "Rh2+", "Kf1", "Rh1+",
                    "Kg2", "Rd1", "Kf2", "Rd4", "Ke1", "Rh4", "Kd2", "Rh2+",
                    "Kc3", "Rh3+", "Kxc4", "Nxd6+", "Kc5", "Ne4+", "Kb5", "Nxg5",
                    "a4", "Nf3", "a5", "Nd4+", "Kc4", "Rh4", "Bc5", "Nc6+",
                    "Kb5", "Nxa5", "Kxa5", "g5", "Kb5", "g4", "Rg7", "g3",
                    "Rxg3", "Rh1", "Kc6", "Re1", "Bd4", "Rc1+", "Bc3", "Rd1",
                    "Re3+", "Kf5", "Kc5", "Rd8", "Be5", "Rc8+", "Kd5", "Ra8",
                    "Rf3+", "Kg4", "Rf7", "Ra5+", "Ke4", "Ra4+", "Bd4", "Kg5",
                    "Rg7+", "Kh4", "Ke5", "Kh3", "Rg1", "Rb4", "Be3", "Rg4",
                    "Ra1", "Kg2", "Bf4", "Rg8", "Ra2+", "Kf3", "Ra3+", "Ke2",
                    "Ke4", "Re8+", "Be5", "Re7", "Ra2+", "Ke1", "Kd4", "Kf1",
                    "Bf4", "Re2", "Ra8", "Re7", "Kd3", "Kg2", "Rf8", "Re6",
                    "Rf7", "Re8", "Be3", "Ra8", "Bc5", "Ra4", "Ke3", "Rg4",
                    "Bd6", "Rg6", "Rf2+", "Kh3", "Be5", "Kg4", "Ke4", "Kh5",
                    "Bf6", "Kg4", "Rf4+", "Kg3", "Ke3", "Kh3", "Rf5", "Rg3+",
                    "Kf2", "Rg2+", "Kf1", "Rc2", "Rg5", "Rc4", "Be5", "Kh4",
                    "Rg8", "Re4", "Bg3+", "Kh5", "Kf2", "Ra4", "Kf3", "Kh6",
                    "Be5", "Rb4", "Bf4+", "Kh7", "Rg5", "Ra4", "Kg4", "Rb4",
                    "Kf5", "Rb5+");
            ResultadoFim fim = XadrezLogica.verificarFim(board);
            assertNotNull(fim, "Deve detectar fim de jogo após 121 lances");
            assertEquals(ResultadoXadrez.EMPATE, fim.resultado());
            assertEquals(MotivoXadrez.CINQUENTA_LANCES, fim.motivo());
        }
    }

    // =========================================================================
    // classificarEntrada
    // =========================================================================

    @Nested
    @DisplayName("classificarEntrada")
    class ClassificarEntrada {

        @Test
        @DisplayName("e4 na posição inicial: VALIDO")
        void validoE4() {
            Board board = new Board();
            Classificacao c = XadrezLogica.classificarEntrada(board, "e4");
            assertEquals(TipoEntrada.VALIDO, c.tipo());
            assertNotNull(c.move());
        }

        @Test
        @DisplayName("ed5 (captura sem x) após 1.e4 d5: VALIDO")
        void validoCapturaSemX() {
            Board board = XadrezLogica.jogarSequencia("e4", "d5");
            Classificacao c = XadrezLogica.classificarEntrada(board, "ed5");
            assertEquals(TipoEntrada.VALIDO, c.tipo());
            assertNotNull(c.move());
        }

        @Test
        @DisplayName("d4 para pretas no 1º lance: LANCE_ILEGAL")
        void ilegalD4Pretas() {
            Board board = XadrezLogica.jogarSequencia("e4");
            Classificacao c = XadrezLogica.classificarEntrada(board, "d4");
            assertEquals(TipoEntrada.LANCE_ILEGAL, c.tipo());
            assertNull(c.move());
        }

        @Test
        @DisplayName("e5 para brancas na posição inicial: LANCE_ILEGAL")
        void ilegalE5Brancas() {
            Board board = new Board();
            Classificacao c = XadrezLogica.classificarEntrada(board, "e5");
            assertEquals(TipoEntrada.LANCE_ILEGAL, c.tipo());
        }

        @Test
        @DisplayName("O-O quando rei já se moveu: LANCE_ILEGAL")
        void ilegalRoqueSemDireito() {
            Board board = XadrezLogica.jogarSequencia("e4", "e5", "Ke2", "Ke7");
            Classificacao c = XadrezLogica.classificarEntrada(board, "O-O");
            assertEquals(TipoEntrada.LANCE_ILEGAL, c.tipo());
        }

        @Test
        @DisplayName("Nf3 quando f3 está ocupado: LANCE_ILEGAL")
        void ilegalCavaloBlockeado() {
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/5P2/PPPPP1PP/RNBQKBNR w KQkq - 0 1");
            Classificacao c = XadrezLogica.classificarEntrada(board, "Nf3");
            assertEquals(TipoEntrada.LANCE_ILEGAL, c.tipo());
        }

        @Test
        @DisplayName("Ye4: NOTACAO_INVALIDA")
        void invalidoLetraY() {
            Board board = new Board();
            Classificacao c = XadrezLogica.classificarEntrada(board, "Ye4");
            assertEquals(TipoEntrada.NOTACAO_INVALIDA, c.tipo());
        }

        @Test
        @DisplayName("e9: NOTACAO_INVALIDA")
        void invalidoLinha9() {
            Board board = new Board();
            Classificacao c = XadrezLogica.classificarEntrada(board, "e9");
            assertEquals(TipoEntrada.NOTACAO_INVALIDA, c.tipo());
        }

        @Test
        @DisplayName("asasd: NOTACAO_INVALIDA")
        void invalidoAsasd() {
            Board board = new Board();
            Classificacao c = XadrezLogica.classificarEntrada(board, "asasd");
            assertEquals(TipoEntrada.NOTACAO_INVALIDA, c.tipo());
        }

        @Test
        @DisplayName("123: NOTACAO_INVALIDA")
        void invalido123() {
            Board board = new Board();
            Classificacao c = XadrezLogica.classificarEntrada(board, "123");
            assertEquals(TipoEntrada.NOTACAO_INVALIDA, c.tipo());
        }

        @Test
        @DisplayName("string vazia: NOTACAO_INVALIDA")
        void invalidoVazio() {
            Board board = new Board();
            Classificacao c = XadrezLogica.classificarEntrada(board, "");
            assertEquals(TipoEntrada.NOTACAO_INVALIDA, c.tipo());
        }

        @Test
        @DisplayName("null: NOTACAO_INVALIDA")
        void invalidoNull() {
            Board board = new Board();
            Classificacao c = XadrezLogica.classificarEntrada(board, null);
            assertEquals(TipoEntrada.NOTACAO_INVALIDA, c.tipo());
        }

        @Test
        @DisplayName("O-O-O-O: NOTACAO_INVALIDA")
        void invalidoRoqueQuadruplo() {
            Board board = new Board();
            Classificacao c = XadrezLogica.classificarEntrada(board, "O-O-O-O");
            assertEquals(TipoEntrada.NOTACAO_INVALIDA, c.tipo());
        }
    }

    // =========================================================================
    // jogarSequencia
    // =========================================================================

    @Nested
    @DisplayName("jogarSequencia")
    class JogarSequencia {

        @Test
        @DisplayName("sequência válida não lança exceção")
        void sequenciaValida() {
            assertDoesNotThrow(() -> XadrezLogica.jogarSequencia("e4", "e5", "Nf3", "Nc6", "Bb5"));
        }

        @Test
        @DisplayName("lance inválido na sequência lança IllegalArgumentException")
        void lanceInvalidoNaSequencia() {
            assertThrows(IllegalArgumentException.class,
                    () -> XadrezLogica.jogarSequencia("e4", "e5", "INVALIDO"));
        }

        @Test
        @DisplayName("após 1.e4 e5 é a vez das brancas")
        void vezCorretaAposLances() {
            Board board = XadrezLogica.jogarSequencia("e4", "e5");
            assertEquals("WHITE", board.getSideToMove().value());
        }

        @Test
        @DisplayName("Ruy Lopez: 10 lances sem erro")
        void ruyLopez() {
            assertDoesNotThrow(() -> XadrezLogica.jogarSequencia(
                    "e4", "e5", "Nf3", "Nc6", "Bb5", "a6", "Ba4", "Nf6", "O-O", "Be7"));
        }

        @Test
        @DisplayName("Siciliana com captura sem x: cd4")
        void siciliana() {
            assertDoesNotThrow(() -> XadrezLogica.jogarSequencia(
                    "e4", "c5", "Nf3", "d6", "d4", "cd4", "Nd4"));
        }
    }

    @Nested
    @DisplayName("Material Suficiente")
    class MaterialSuficiente {

        @Test
        @DisplayName("Posição inicial - ambos têm material suficiente")
        void posicaoInicial() {
            Board board = new Board();
            assertTrue(XadrezLogica.temMaterialSuficiente(board, true));
            assertTrue(XadrezLogica.temMaterialSuficiente(board, false));
        }

        @Test
        @DisplayName("Rei vs Rei - nenhum tem material suficiente")
        void reiVsRei() {
            Board board = new Board();
            board.loadFromFen("8/8/8/4k3/8/8/4K3/8 w - - 0 1");
            assertFalse(XadrezLogica.temMaterialSuficiente(board, true));
            assertFalse(XadrezLogica.temMaterialSuficiente(board, false));
        }

        @Test
        @DisplayName("Rei + Bispo vs Rei - bispo não tem material suficiente")
        void reiBispoVsRei() {
            Board board = new Board();
            board.loadFromFen("8/8/8/4k3/8/8/4KB2/8 w - - 0 1");
            assertFalse(XadrezLogica.temMaterialSuficiente(board, true));
            assertFalse(XadrezLogica.temMaterialSuficiente(board, false));
        }

        @Test
        @DisplayName("Rei + Cavalo vs Rei - cavalo não tem material suficiente")
        void reiCavaloVsRei() {
            Board board = new Board();
            board.loadFromFen("8/8/8/4k3/8/8/4KN2/8 w - - 0 1");
            assertFalse(XadrezLogica.temMaterialSuficiente(board, true));
            assertFalse(XadrezLogica.temMaterialSuficiente(board, false));
        }

        @Test
        @DisplayName("Rei + 2 Cavalos vs Rei - tem material suficiente")
        void reiDoisCavalosVsRei() {
            Board board = new Board();
            board.loadFromFen("8/8/8/4k3/8/8/3KNN2/8 w - - 0 1");
            assertTrue(XadrezLogica.temMaterialSuficiente(board, true));
            assertFalse(XadrezLogica.temMaterialSuficiente(board, false));
        }

        @Test
        @DisplayName("Rei + Bispo + Cavalo vs Rei - tem material suficiente")
        void reiBispoCavaloVsRei() {
            Board board = new Board();
            board.loadFromFen("8/8/8/4k3/8/8/3KBN2/8 w - - 0 1");
            assertTrue(XadrezLogica.temMaterialSuficiente(board, true));
            assertFalse(XadrezLogica.temMaterialSuficiente(board, false));
        }

        @Test
        @DisplayName("Rei + Torre vs Rei - tem material suficiente")
        void reiTorreVsRei() {
            Board board = new Board();
            board.loadFromFen("8/8/8/4k3/8/8/4KR2/8 w - - 0 1");
            assertTrue(XadrezLogica.temMaterialSuficiente(board, true));
            assertFalse(XadrezLogica.temMaterialSuficiente(board, false));
        }

        @Test
        @DisplayName("Rei + Dama vs Rei - tem material suficiente")
        void reiDamaVsRei() {
            Board board = new Board();
            board.loadFromFen("8/8/8/4k3/8/8/4KQ2/8 w - - 0 1");
            assertTrue(XadrezLogica.temMaterialSuficiente(board, true));
            assertFalse(XadrezLogica.temMaterialSuficiente(board, false));
        }

        @Test
        @DisplayName("Rei + Peão vs Rei - tem material suficiente")
        void reiPeaoVsRei() {
            Board board = new Board();
            board.loadFromFen("8/8/8/4k3/8/8/4KP2/8 w - - 0 1");
            assertTrue(XadrezLogica.temMaterialSuficiente(board, true));
            assertFalse(XadrezLogica.temMaterialSuficiente(board, false));
        }

        @Test
        @DisplayName("Rei + 2 Bispos vs Rei - tem material suficiente")
        void reiDoisBisposVsRei() {
            Board board = new Board();
            board.loadFromFen("8/8/8/4k3/8/8/3KBB2/8 w - - 0 1");
            assertTrue(XadrezLogica.temMaterialSuficiente(board, true));
            assertFalse(XadrezLogica.temMaterialSuficiente(board, false));
        }

        @Test
        @DisplayName("Ambos com material insuficiente - Rei + Bispo vs Rei + Cavalo")
        void ambosMaterialInsuficiente() {
            Board board = new Board();
            board.loadFromFen("8/8/8/4kn2/8/8/4KB2/8 w - - 0 1");
            assertFalse(XadrezLogica.temMaterialSuficiente(board, true));
            assertFalse(XadrezLogica.temMaterialSuficiente(board, false));
        }
    }

    // =========================================================================
    // Detecção de Ambiguidade
    // =========================================================================

    @Nested
    @DisplayName("Detecção de Ambiguidade")
    class DeteccaoAmbiguidade {

        @Test
        @DisplayName("Nf3 sem ambiguidade: VALIDO")
        void cavaloSemAmbiguidade() {
            Board board = new Board();
            Classificacao c = XadrezLogica.classificarEntrada(board, "Nf3");
            assertEquals(TipoEntrada.VALIDO, c.tipo());
            assertNotNull(c.move());
        }

        @Test
        @DisplayName("Nf3 com dois cavalos que podem ir para f3 sem desambiguação: LANCE_AMBIGUO")
        void cavaloComAmbiguidade() {
            // Posição onde cavalos em g1 e d2 podem ir para f3
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPNPPPP/R1BQKBNR w KQkq - 0 1");
            Classificacao c = XadrezLogica.classificarEntrada(board, "Nf3");
            assertEquals(TipoEntrada.LANCE_AMBIGUO, c.tipo());
            assertNull(c.move());
        }

        @Test
        @DisplayName("Ngf3 com desambiguação de coluna: VALIDO")
        void cavaloComDesambiguacaoColuna() {
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPNPPPP/R1BQKBNR w KQkq - 0 1");
            Classificacao c = XadrezLogica.classificarEntrada(board, "Ngf3");
            assertEquals(TipoEntrada.VALIDO, c.tipo());
            assertNotNull(c.move());
        }

        @Test
        @DisplayName("Ndf3 com desambiguação de coluna: VALIDO")
        void cavaloComDesambiguacaoColuna2() {
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPNPPPP/R1BQKBNR w KQkq - 0 1");
            Classificacao c = XadrezLogica.classificarEntrada(board, "Ndf3");
            assertEquals(TipoEntrada.VALIDO, c.tipo());
            assertNotNull(c.move());
        }

        @Test
        @DisplayName("Rd1 com duas torres que podem ir para d1 sem desambiguação: LANCE_ILEGAL")
        void torreComAmbiguidade() {
            // Torres em a1 e h1 podem ir para d1
            Board board = new Board();
            board.loadFromFen("4k3/8/8/8/8/8/8/R2K3R w - - 0 1");
            Classificacao c = XadrezLogica.classificarEntrada(board, "Rd1");
            assertEquals(TipoEntrada.LANCE_ILEGAL, c.tipo());
            assertNull(c.move());
        }

        @Test
        @DisplayName("Rad1 com desambiguação de coluna: LANCE_ILEGAL")
        void torreComDesambiguacaoColuna() {
            Board board = new Board();
            board.loadFromFen("4k3/8/8/8/8/8/8/R2K3R w - - 0 1");
            Classificacao c = XadrezLogica.classificarEntrada(board, "Rad1");
            assertEquals(TipoEntrada.LANCE_ILEGAL, c.tipo());
            assertNull(c.move());
        }

        @Test
        @DisplayName("Rhd1 com desambiguação de coluna: LANCE_ILEGAL")
        void torreComDesambiguacaoColuna2() {
            Board board = new Board();
            board.loadFromFen("4k3/8/8/8/8/8/8/R2K3R w - - 0 1");
            Classificacao c = XadrezLogica.classificarEntrada(board, "Rhd1");
            assertEquals(TipoEntrada.LANCE_ILEGAL, c.tipo());
            assertNull(c.move());
        }

        @Test
        @DisplayName("Qd4 com duas damas que podem ir para d4 sem desambiguação: LANCE_AMBIGUO")
        void damaComAmbiguidade() {
            // Damas em d1 e d8 podem ir para d4 (posição artificial)
            Board board = new Board();
            board.loadFromFen("3Qk3/8/8/8/8/8/8/3QK3 w - - 0 1");
            Classificacao c = XadrezLogica.classificarEntrada(board, "Qd4");
            assertEquals(TipoEntrada.LANCE_AMBIGUO, c.tipo());
            assertNull(c.move());
        }

        @Test
        @DisplayName("Q1d4 com desambiguação de linha: VALIDO")
        void damaComDesambiguacaoLinha() {
            Board board = new Board();
            board.loadFromFen("3Qk3/8/8/8/8/8/8/3QK3 w - - 0 1");
            Classificacao c = XadrezLogica.classificarEntrada(board, "Q1d4");
            assertEquals(TipoEntrada.VALIDO, c.tipo());
            assertNotNull(c.move());
        }

        @Test
        @DisplayName("Bd3 com dois bispos que podem ir para d3 sem desambiguação: LANCE_AMBIGUO")
        void bispoComAmbiguidade() {
            // Bispos em a6 e f1 podem ir para d3
            Board board = new Board();
            board.loadFromFen("4k3/8/B7/8/8/8/8/4KB2 w - - 0 1");
            Classificacao c = XadrezLogica.classificarEntrada(board, "Bd3");
            assertEquals(TipoEntrada.LANCE_AMBIGUO, c.tipo());
            assertNull(c.move());
        }

        @Test
        @DisplayName("Bfd3 com desambiguação de coluna: VALIDO")
        void bispoComDesambiguacaoColuna() {
            Board board = new Board();
            board.loadFromFen("4k3/8/B7/8/8/8/8/4KB2 w - - 0 1");
            Classificacao c = XadrezLogica.classificarEntrada(board, "Bfd3");
            assertEquals(TipoEntrada.VALIDO, c.tipo());
            assertNotNull(c.move());
        }

        @Test
        @DisplayName("Rei nunca tem ambiguidade (só existe um)")
        void reiSemAmbiguidade() {
            Board board = new Board();
            Classificacao c = XadrezLogica.classificarEntrada(board, "Ke2");
            assertEquals(TipoEntrada.LANCE_ILEGAL, c.tipo());
            assertNull(c.move());
        }

        @Test
        @DisplayName("Peão nunca tem ambiguidade do tipo verificado")
        void peaoSemAmbiguidade() {
            Board board = new Board();
            Classificacao c = XadrezLogica.classificarEntrada(board, "e4");
            assertEquals(TipoEntrada.VALIDO, c.tipo());
            assertNotNull(c.move());
        }

        @Test
        @DisplayName("Roque nunca tem ambiguidade")
        void roqueSemAmbiguidade() {
            Board board = XadrezLogica.jogarSequencia("e4", "e5", "Nf3", "Nc6", "Bc4", "Bc5");
            Classificacao c = XadrezLogica.classificarEntrada(board, "O-O");
            assertEquals(TipoEntrada.VALIDO, c.tipo());
            assertNotNull(c.move());
        }
    }
}
