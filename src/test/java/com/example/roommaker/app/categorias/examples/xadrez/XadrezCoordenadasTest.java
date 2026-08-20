package com.example.roommaker.app.categorias.examples.xadrez;

import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezLogica;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.PreLance;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * O caminho do tabuleiro visual: duas casas viram um lance.
 *
 * Aqui dá para usar FEN à vontade, porque nada neste ponto depende do histórico
 * da partida — é só "esta posição, estas duas casas".
 */
class XadrezCoordenadasTest {

    private Board board(String fen) {
        Board b = new Board();
        b.loadFromFen(fen);
        return b;
    }

    private Move resolver(Board b, String from, String to) {
        return XadrezLogica.resolverPorCoordenadas(b, from, to, null);
    }

    @Nested
    @DisplayName("Lances comuns")
    class Comuns {

        @Test
        @DisplayName("resolve um lance simples da posição inicial")
        void simples() {
            assertNotNull(resolver(new Board(), "e2", "e4"));
        }

        @Test
        @DisplayName("recusa lance ilegal")
        void ilegal() {
            assertNull(resolver(new Board(), "e2", "e5"), "peão não anda três casas");
        }

        @Test
        @DisplayName("recusa mover peça do adversário")
        void pecaDoAdversario() {
            assertNull(resolver(new Board(), "e7", "e5"), "é a vez das brancas");
        }

        @Test
        @DisplayName("recusa origem e destino iguais")
        void mesmaCasa() {
            assertNull(resolver(new Board(), "e2", "e2"));
        }

        @Test
        @DisplayName("recusa coordenada fora do tabuleiro sem explodir")
        void foraDoTabuleiro() {
            assertNull(resolver(new Board(), "j9", "e4"));
            assertNull(resolver(new Board(), "e2", "e9"));
            assertNull(XadrezLogica.resolverPorCoordenadas(new Board(), null, "e4", null));
        }

        @Test
        @DisplayName("aceita coordenada em maiúscula")
        void maiuscula() {
            assertNotNull(resolver(new Board(), "E2", "E4"));
        }
    }

    @Nested
    @DisplayName("Ambiguidade")
    class Ambiguidade {

        @Test
        @DisplayName("dois cavalos podendo ir à mesma casa: a coordenada escolhe sozinha")
        void doisCavalos() {
            // Cavalos brancos em c3 e g1; os dois alcançam e2.
            Board b = board("4k3/8/8/8/8/2N5/8/4K1N1 w - - 0 1");

            Move deC3 = resolver(b, "c3", "e2");
            Move deG1 = resolver(b, "g1", "e2");

            assertNotNull(deC3);
            assertNotNull(deG1);
            assertNotEquals(deC3.getFrom(), deG1.getFrom(),
                    "é exatamente a ambiguidade que derruba a notação SAN — e que coordenada não tem");
        }
    }

    @Nested
    @DisplayName("Roque")
    class Roque {

        @Test
        @DisplayName("rei andando duas casas é roque curto")
        void curto() {
            Board b = board("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
            Move m = resolver(b, "e1", "g1");
            assertNotNull(m);
        }

        @Test
        @DisplayName("roque longo")
        void longo() {
            Board b = board("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
            assertNotNull(resolver(b, "e1", "c1"));
        }

        @Test
        @DisplayName("recusa roque sem direito")
        void semDireito() {
            Board b = board("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w kq - 0 1");
            assertNull(resolver(b, "e1", "g1"));
        }
    }

    @Nested
    @DisplayName("Promoção")
    class Promocao {

        private final String FEN = "8/P6k/8/8/8/8/8/K7 w - - 0 1";

        @Test
        @DisplayName("promove na peça pedida")
        void explicita() {
            Board b = board(FEN);
            Move m = XadrezLogica.resolverPorCoordenadas(b, "a7", "a8", "n");
            assertNotNull(m);
            assertEquals(com.github.bhlangonijr.chesslib.Piece.WHITE_KNIGHT, m.getPromotion());
        }

        @Test
        @DisplayName("sem peça informada, assume dama")
        void implicita() {
            Board b = board(FEN);
            Move m = XadrezLogica.resolverPorCoordenadas(b, "a7", "a8", null);
            assertNotNull(m);
            assertEquals(com.github.bhlangonijr.chesslib.Piece.WHITE_QUEEN, m.getPromotion());
        }

        @Test
        @DisplayName("promoção pedida num lance que não promove não vira lance normal")
        void promocaoOndeNaoCabe() {
            assertNull(XadrezLogica.resolverPorCoordenadas(new Board(), "e2", "e4", "q"));
        }

        @Test
        @DisplayName("promove com a cor certa quando são as pretas")
        void corDasPretas() {
            Board b = board("K7/8/8/8/8/8/p6k/8 b - - 0 1");
            Move m = XadrezLogica.resolverPorCoordenadas(b, "a2", "a1", "q");
            assertNotNull(m);
            assertEquals(com.github.bhlangonijr.chesslib.Piece.BLACK_QUEEN, m.getPromotion());
        }
    }

    @Nested
    @DisplayName("Normalização de PreLance")
    class Normalizacao {

        @Test
        @DisplayName("aceita e normaliza para minúsculas")
        void normaliza() {
            PreLance p = PreLance.normalizar(" E2 ", "E4", "Q");
            assertNotNull(p);
            assertEquals("e2", p.getFrom());
            assertEquals("e4", p.getTo());
            assertEquals("q", p.getPromocao());
        }

        @Test
        @DisplayName("recusa formatos impossíveis")
        void recusa() {
            assertNull(PreLance.normalizar("e9", "e4", null));
            assertNull(PreLance.normalizar("i2", "e4", null));
            assertNull(PreLance.normalizar("e2", "e2", null), "origem igual ao destino");
            assertNull(PreLance.normalizar("e2", "e4", "k"), "não se promove a rei");
            assertNull(PreLance.normalizar("e2", "e4", "p"), "nem a peão");
            assertNull(PreLance.normalizar(null, "e4", null));
            assertNull(PreLance.normalizar("e2e4", "e4", null));
        }

        @Test
        @DisplayName("promoção vazia é o mesmo que sem promoção")
        void promocaoVazia() {
            PreLance p = PreLance.normalizar("e2", "e4", "  ");
            assertNotNull(p);
            assertNull(p.getPromocao());
        }

        @Test
        @DisplayName("ida e volta por UCI preserva o pré-lance")
        void uciIdaEVolta() {
            for (String uci : new String[] { "e2e4", "e7e8q", "a1h8", "b7a8n" }) {
                assertEquals(uci, PreLance.deUci(uci).toUci());
            }
        }

        @Test
        @DisplayName("UCI corrompido vira null em vez de exceção")
        void uciCorrompido() {
            assertNull(PreLance.deUci("lixo"));
            assertNull(PreLance.deUci("e2e4k"));
            assertNull(PreLance.deUci(""));
            assertNull(PreLance.deUci(null));
        }
    }
}
