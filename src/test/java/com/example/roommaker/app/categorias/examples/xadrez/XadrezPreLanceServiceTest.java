package com.example.roommaker.app.categorias.examples.xadrez;

import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezLogica;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.*;
import com.example.roommaker.app.categorias.examples.xadrez.domain.service.XadrezPreLanceService;
import com.example.roommaker.app.categorias.examples.xadrez.domain.service.XadrezTempoService;
import com.github.bhlangonijr.chesslib.Board;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A cadeia de pré-lances, testada sem Spring e sem Mongo.
 *
 * Cada teste monta uma partida real (sequência de lances SAN de verdade, não FEN
 * solto) porque a SAN canônica é gerada reproduzindo a partida desde o início —
 * um tabuleiro que não bate com a lista de lances produziria anotações erradas
 * sem falhar, que é justamente o tipo de bug que estes testes existem para pegar.
 */
class XadrezPreLanceServiceTest {

    private final XadrezPreLanceService service = new XadrezPreLanceService(new XadrezTempoService());

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PartidaXadrez partidaCom(String... lances) {
        PartidaXadrez p = PartidaXadrez.builder()
                .id(1L)
                .usernameBrancas("branca")
                .usernamePretas("preta")
                .notacao(NotacaoXadrez.INGLESA)
                .build();
        p.getLances().addAll(List.of(lances));
        return p;
    }

    private Board boardDe(PartidaXadrez p) {
        return XadrezLogica.reconstruirBoard(p.getLances(), NotacaoXadrez.INGLESA);
    }

    private PreLance pre(String from, String to) {
        return new PreLance(from, to, null);
    }

    private XadrezPreLanceService.Resultado rodar(PartidaXadrez p) {
        return service.aplicarCadeia(p, boardDe(p), NotacaoXadrez.INGLESA, false);
    }

    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Aplicação básica")
    class Basico {

        @Test
        @DisplayName("aplica o pré-lance do lado da vez e o anota em SAN")
        void aplicaUm() {
            PartidaXadrez p = partidaCom("e4"); // vez das pretas
            p.definirPreLances(false, List.of(pre("e7", "e5")));

            XadrezPreLanceService.Resultado r = rodar(p);

            assertEquals(1, r.quantidadeAplicada());
            assertEquals(List.of("e4", "e5"), p.getLances());
            assertTrue(p.vezDasBrancas(), "depois do pré-lance das pretas, a vez volta às brancas");
            assertTrue(p.preLancesDe(false).isEmpty(), "o pré-lance consumido sai da fila");
            assertNull(r.ladoCanceladoBrancas());
            assertFalse(r.encerrouPartida());
        }

        @Test
        @DisplayName("não faz nada quando o lado da vez não tem fila")
        void semFila() {
            PartidaXadrez p = partidaCom("e4");
            p.definirPreLances(true, List.of(pre("g1", "f3"))); // fila do lado ERRADO

            XadrezPreLanceService.Resultado r = rodar(p);

            assertEquals(0, r.quantidadeAplicada());
            assertEquals(List.of("e4"), p.getLances());
            assertEquals(1, p.preLancesDe(true).size(), "a fila das brancas continua intacta");
        }

        @Test
        @DisplayName("consome só um pré-lance por vez quando o adversário não tem fila")
        void umPorVez() {
            PartidaXadrez p = partidaCom("e4");
            p.definirPreLances(false, List.of(pre("e7", "e5"), pre("g8", "f6")));

            XadrezPreLanceService.Resultado r = rodar(p);

            assertEquals(1, r.quantidadeAplicada());
            assertEquals(List.of("e4", "e5"), p.getLances());
            assertEquals(List.of(pre("g8", "f6")), p.preLancesDe(false),
                    "o resto da fila espera a próxima vez");
        }
    }

    @Nested
    @DisplayName("Cadeia entre os dois lados")
    class Cadeia {

        @Test
        @DisplayName("filas dos dois lados se destravam em rajada")
        void rajada() {
            PartidaXadrez p = partidaCom(); // vez das brancas, partida no início
            p.definirPreLances(true, List.of(pre("e2", "e4"), pre("g1", "f3")));
            p.definirPreLances(false, List.of(pre("e7", "e5"), pre("b8", "c6")));

            XadrezPreLanceService.Resultado r = rodar(p);

            assertEquals(4, r.quantidadeAplicada());
            assertEquals(List.of("e4", "e5", "Nf3", "Nc6"), p.getLances());
            assertTrue(p.preLancesDe(true).isEmpty());
            assertTrue(p.preLancesDe(false).isEmpty());
        }

        @Test
        @DisplayName("a cadeia para no lado que fica sem fila, não no que ainda tem")
        void paraNoLadoVazio() {
            PartidaXadrez p = partidaCom();
            p.definirPreLances(true, List.of(pre("e2", "e4")));
            p.definirPreLances(false, List.of(pre("e7", "e5"), pre("b8", "c6")));

            XadrezPreLanceService.Resultado r = rodar(p);

            // e4, e5 e então as brancas não têm mais nada enfileirado.
            assertEquals(2, r.quantidadeAplicada());
            assertEquals(List.of("e4", "e5"), p.getLances());
            assertEquals(1, p.preLancesDe(false).size());
        }

        @Test
        @DisplayName("uma cadeia longa termina — não roda para sempre")
        void termina() {
            PartidaXadrez p = partidaCom();
            p.definirPreLances(true, List.of(pre("e2", "e4"), pre("g1", "f3"), pre("f1", "c4"),
                    pre("e1", "g1")));
            p.definirPreLances(false, List.of(pre("e7", "e5"), pre("b8", "c6"), pre("f8", "c5"),
                    pre("g8", "f6")));

            XadrezPreLanceService.Resultado r = rodar(p);

            assertEquals(8, r.quantidadeAplicada());
            assertEquals(List.of("e4", "e5", "Nf3", "Nc6", "Bc4", "Bc5", "O-O", "Nf6"), p.getLances());
        }
    }

    @Nested
    @DisplayName("Pré-lance que virou ilegal")
    class Ilegal {

        @Test
        @DisplayName("descarta a fila INTEIRA, não só o lance impossível")
        void descartaFilaInteira() {
            // Depois de 1.e4, a dama preta em d8 não alcança h4: o próprio peão de
            // e7 tapa a diagonal.
            PartidaXadrez p = partidaCom("e4");
            p.definirPreLances(false, List.of(pre("d8", "h4"), pre("e7", "e5")));

            XadrezPreLanceService.Resultado r = rodar(p);

            assertEquals(0, r.quantidadeAplicada());
            assertEquals(List.of("e4"), p.getLances());
            assertTrue(p.preLancesDe(false).isEmpty(),
                    "o e7-e5 seguinte foi pensado para uma posição que não aconteceu");
            assertEquals(Boolean.FALSE, r.ladoCanceladoBrancas(), "quem perdeu a fila foram as pretas");
        }

        @Test
        @DisplayName("não conta como lance ilegal — o jogador não errou, a posição mudou")
        void naoPenaliza() {
            PartidaXadrez p = partidaCom("e4");
            p.definirPreLances(false, List.of(pre("d8", "h4")));

            rodar(p);

            assertEquals(0, p.getLancesIlegaisPretas());
            assertEquals(0, p.getLancesIlegaisBrancas());
        }

        @Test
        @DisplayName("a fila do adversário sobrevive ao cancelamento da minha")
        void naoDerrubaAFilaDoOutro() {
            PartidaXadrez p = partidaCom("e4");
            p.definirPreLances(false, List.of(pre("d8", "h4")));
            p.definirPreLances(true, List.of(pre("g1", "f3")));

            rodar(p);

            assertTrue(p.preLancesDe(false).isEmpty());
            assertEquals(1, p.preLancesDe(true).size());
        }

        @Test
        @DisplayName("peça capturada no meio da cadeia derruba a fila de quem ia movê-la")
        void pecaCapturada() {
            // 1.e4 e5 2.Nf3 — as pretas tinham planejado Nc6 e depois Nd4.
            // As brancas capturam o cavalo antes: a fila das pretas cai na hora.
            PartidaXadrez p = partidaCom("e4", "e5", "Nf3");
            p.definirPreLances(false, List.of(pre("b8", "c6")));
            p.definirPreLances(true, List.of(pre("f3", "e5"))); // Nxe5

            XadrezPreLanceService.Resultado r = rodar(p);

            assertEquals(List.of("e4", "e5", "Nf3", "Nc6", "Nxe5"), p.getLances());
            assertEquals(2, r.quantidadeAplicada());
        }
    }

    @Nested
    @DisplayName("Fim de partida dentro da cadeia")
    class FimDePartida {

        @Test
        @DisplayName("pré-lance que dá mate encerra a partida ali mesmo")
        void mate() {
            // Mate do pastor: as brancas tinham Qxf7 pré-lançado.
            PartidaXadrez p = partidaCom("e4", "e5", "Bc4", "Nc6", "Qh5", "Nf6");
            p.definirPreLances(true, List.of(pre("h5", "f7"), pre("e1", "g1")));

            XadrezPreLanceService.Resultado r = rodar(p);

            assertTrue(r.encerrouPartida());
            assertEquals(ResultadoXadrez.VITORIA_BRANCAS, r.fim().resultado());
            assertEquals(MotivoXadrez.XEQUE_MATE, r.fim().motivo());
            assertEquals("Qxf7#", p.getLances().get(p.getLances().size() - 1));
            assertFalse(p.emAndamento());
        }

        @Test
        @DisplayName("encerrar limpa as duas filas — nada fica pendurado na próxima partida")
        void limpaFilasAoEncerrar() {
            PartidaXadrez p = partidaCom("e4", "e5", "Bc4", "Nc6", "Qh5", "Nf6");
            p.definirPreLances(true, List.of(pre("h5", "f7"), pre("e1", "g1")));
            p.definirPreLances(false, List.of(pre("d7", "d6")));

            rodar(p);

            assertTrue(p.preLancesDe(true).isEmpty());
            assertTrue(p.preLancesDe(false).isEmpty());
        }
    }

    @Nested
    @DisplayName("Lances especiais")
    class Especiais {

        @Test
        @DisplayName("roque: e1-g1 vira O-O")
        void roque() {
            PartidaXadrez p = partidaCom("e4", "e5", "Nf3", "Nc6", "Bc4", "Bc5");
            p.definirPreLances(true, List.of(pre("e1", "g1")));

            rodar(p);

            assertEquals("O-O", p.getLances().get(6));
        }

        @Test
        @DisplayName("en passant: e5-d6 vira exd6")
        void enPassant() {
            PartidaXadrez p = partidaCom("e4", "Nf6", "e5", "d5");
            p.definirPreLances(true, List.of(pre("e5", "d6")));

            rodar(p);

            assertEquals("exd6", p.getLances().get(4));
        }

        @Test
        @DisplayName("promoção explícita respeita a peça pedida")
        void promocaoExplicita() {
            PartidaXadrez p = partidaCom("e4", "d5", "exd5", "c6", "dxc6", "h6", "cxb7", "h5");
            p.definirPreLances(true, List.of(new PreLance("b7", "a8", "n")));

            rodar(p);

            assertEquals("bxa8=N", p.getLances().get(8));
        }

        @Test
        @DisplayName("promoção sem peça informada vira dama, e não fila descartada")
        void promocaoImplicita() {
            // O jogador enfileirou b7-a8 antes de saber que aquilo seria uma
            // promoção. Perder a fila inteira por causa de uma letra faltando seria
            // pior do que assumir dama, que é o que o chess.com faz.
            PartidaXadrez p = partidaCom("e4", "d5", "exd5", "c6", "dxc6", "h6", "cxb7", "h5");
            p.definirPreLances(true, List.of(pre("b7", "a8")));

            XadrezPreLanceService.Resultado r = rodar(p);

            assertEquals(1, r.quantidadeAplicada());
            assertEquals("bxa8=Q", p.getLances().get(8));
        }
    }

    @Nested
    @DisplayName("Relógio")
    class Relogio {

        private static final long UM_MINUTO = 60_000L;

        private PartidaXadrez partidaComRelogio(long incrementoMs, String... lances) {
            PartidaXadrez p = partidaCom(lances);
            ControleTempoXadrez ct = ControleTempoXadrez.builder()
                    .tempoInicialBrancas(UM_MINUTO)
                    .tempoInicialPretas(UM_MINUTO)
                    .incrementoBrancas(incrementoMs)
                    .incrementoPretas(incrementoMs)
                    .build();
            ct.inicializar();
            p.setControleTempo(ct);
            return p;
        }

        /** Finge que o último lance foi há {@code ms} milissegundos. */
        private void relogioCorrendoHa(PartidaXadrez p, long ms) {
            p.getControleTempo().setTimestampUltimoLance(System.currentTimeMillis() - ms);
        }

        @Test
        @DisplayName("pré-lance NÃO cobra o tempo que passou — é isso que ele existe para fazer")
        void naoCobraODecorrido() {
            PartidaXadrez p = partidaComRelogio(0, "e4");
            p.definirPreLances(false, List.of(pre("e7", "e5")));
            relogioCorrendoHa(p, 10_000);

            service.aplicarCadeia(p, boardDe(p), NotacaoXadrez.INGLESA, false);

            long pretas = p.getControleTempo().getTempoRestantePretas();
            assertEquals(UM_MINUTO, pretas,
                    "os 10s decorridos são do adversário e da rede, não de quem pré-lançou");
        }

        @Test
        @DisplayName("pré-lance recebe o incremento, como no chess.com")
        void ganhaIncremento() {
            PartidaXadrez p = partidaComRelogio(3_000, "e4");
            p.definirPreLances(false, List.of(pre("e7", "e5")));

            service.aplicarCadeia(p, boardDe(p), NotacaoXadrez.INGLESA, false);

            assertEquals(UM_MINUTO + 3_000, p.getControleTempo().getTempoRestantePretas());
        }

        @Test
        @DisplayName("fila que chega quando JÁ é a sua vez paga o tempo do primeiro lance")
        void cobraQuandoJaEraSuaVez() {
            // Sem isto, mandar a fila em vez de mandar o lance seria um jeito de
            // jogar de graça: bastaria pensar 30s e depois chamar de "pré-lance".
            PartidaXadrez p = partidaComRelogio(0, "e4");
            p.definirPreLances(false, List.of(pre("e7", "e5")));
            relogioCorrendoHa(p, 10_000);

            service.aplicarCadeia(p, boardDe(p), NotacaoXadrez.INGLESA, true);

            long pretas = p.getControleTempo().getTempoRestantePretas();
            assertTrue(pretas <= UM_MINUTO - 9_500 && pretas >= UM_MINUTO - 11_000,
                    "esperado ~50s, veio " + pretas + "ms");
        }

        @Test
        @DisplayName("na cadeia, só o primeiro lance pode ser cobrado; os seguintes são livres")
        void cadeiaCobraSoOPrimeiro() {
            PartidaXadrez p = partidaComRelogio(0);
            p.definirPreLances(true, List.of(pre("e2", "e4"), pre("g1", "f3")));
            p.definirPreLances(false, List.of(pre("e7", "e5"), pre("b8", "c6")));
            relogioCorrendoHa(p, 10_000);

            service.aplicarCadeia(p, boardDe(p), NotacaoXadrez.INGLESA, true);

            long brancas = p.getControleTempo().getTempoRestanteBrancas();
            long pretas = p.getControleTempo().getTempoRestantePretas();
            assertTrue(brancas <= UM_MINUTO - 9_500, "as brancas pagaram o primeiro lance: " + brancas);
            assertEquals(UM_MINUTO, pretas, "as pretas só pré-lançaram; não devem pagar nada");
        }

        @Test
        @DisplayName("tempo infinito não quebra a cadeia")
        void tempoInfinito() {
            PartidaXadrez p = partidaCom("e4"); // sem controle de tempo
            p.definirPreLances(false, List.of(pre("e7", "e5")));

            assertDoesNotThrow(() -> rodar(p));
            assertEquals(List.of("e4", "e5"), p.getLances());
        }
    }

    @Nested
    @DisplayName("Notação portuguesa")
    class Portuguesa {

        @Test
        @DisplayName("o pré-lance é anotado na notação da sala, não em inglês")
        void anotaEmPortugues() {
            PartidaXadrez p = PartidaXadrez.builder()
                    .id(1L).notacao(NotacaoXadrez.PORTUGUESA).build();
            p.getLances().add("e4");
            p.definirPreLances(false, List.of(pre("g8", "f6")));

            Board board = XadrezLogica.reconstruirBoard(p.getLances(), NotacaoXadrez.PORTUGUESA);
            service.aplicarCadeia(p, board, NotacaoXadrez.PORTUGUESA, false);

            assertEquals("Cf6", p.getLances().get(1), "cavalo é C em português, não N");
        }
    }
}
