package com.example.roommaker.app.categorias.examples.xadrez;

import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezResponseFactory;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * O que cada jogador pode e não pode ver.
 *
 * A fila de pré-lances é o primeiro dado realmente privado desta resposta: saber
 * que o adversário pré-lançou (e para onde) entregaria a resposta dele antes de
 * você escolher o próprio lance. Nem chess.com nem lichess mostram isso, e o
 * teste existe para que ninguém volte a montar a resposta "para todos" sem
 * perceber o que está mandando junto.
 */
class XadrezResponseFactoryTest {

    private static final String BRANCAS = "ana";
    private static final String PRETAS = "bruno";
    private static final String ESPECTADOR = "carla";

    private final XadrezResponseFactory factory = new XadrezResponseFactory();

    private SalaXadrez sala;
    private PartidaXadrez partida;

    @BeforeEach
    void setup() {
        partida = PartidaXadrez.builder()
                .id(1L)
                .usernameBrancas(BRANCAS)
                .usernamePretas(PRETAS)
                .notacao(NotacaoXadrez.INGLESA)
                .modoVisual(true)
                .build();
        partida.getLances().add("e4");
        partida.definirPreLances(false, List.of(new PreLance("e7", "e5", null)));
        partida.definirPreLances(true, List.of(new PreLance("g1", "f3", null)));

        sala = SalaXadrez.builder()
                .nomeSala("sala")
                .usernameDono(BRANCAS)
                .usernameBrancas(BRANCAS)
                .usernamePretas(PRETAS)
                .notacao(NotacaoXadrez.INGLESA)
                .modoVisual(true)
                .partidaAtual(partida)
                .build();
    }

    @Nested
    @DisplayName("Privacidade da fila de pré-lances")
    class Privacidade {

        @Test
        @DisplayName("cada jogador recebe a própria fila")
        void recebeAPropria() {
            assertEquals(List.of(new PreLance("g1", "f3", null)),
                    factory.construir(sala, BRANCAS, "LANCE").getMeusPreLances());
            assertEquals(List.of(new PreLance("e7", "e5", null)),
                    factory.construir(sala, PRETAS, "LANCE").getMeusPreLances());
        }

        @Test
        @DisplayName("ninguém recebe a fila do adversário")
        void naoVazaADoOutro() {
            XadrezResponse paraBrancas = factory.construir(sala, BRANCAS, "LANCE");

            assertFalse(paraBrancas.getMeusPreLances().contains(new PreLance("e7", "e5", null)),
                    "as brancas não podem saber que as pretas pré-lançaram e7-e5");
        }

        @Test
        @DisplayName("espectador não recebe fila nenhuma")
        void espectador() {
            assertNull(factory.construir(sala, ESPECTADOR, "LANCE").getMeusPreLances());
        }

        @Test
        @DisplayName("resposta sem destinatário não carrega dado privado")
        void semDestinatario() {
            XadrezResponse generica = factory.construir(sala, null, "SALA_DELETADA");

            assertNull(generica.getMeusPreLances());
            assertNull(generica.getHistorico());
        }

        @Test
        @DisplayName("a fila entregue é cópia — mexer nela não mexe na partida")
        void entregaCopia() {
            XadrezResponse r = factory.construir(sala, PRETAS, "LANCE");
            r.getMeusPreLances().clear();

            assertEquals(1, partida.preLancesDe(false).size());
        }
    }

    @Nested
    @DisplayName("Aviso de fila cancelada")
    class FilaCancelada {

        @Test
        @DisplayName("só quem perdeu a fila é avisado")
        void soOAfetado() {
            assertEquals(Boolean.TRUE,
                    factory.construir(sala, PRETAS, "LANCE", true).getPreLancesCancelados());
            assertEquals(Boolean.FALSE,
                    factory.construir(sala, BRANCAS, "LANCE", false).getPreLancesCancelados());
        }
    }

    @Nested
    @DisplayName("Modo visual")
    class ModoVisual {

        @Test
        @DisplayName("vai na resposta da partida atual")
        void naPartidaAtual() {
            assertEquals(Boolean.TRUE, factory.construir(sala, BRANCAS, "LANCE").getModoVisual());
        }

        @Test
        @DisplayName("vai no histórico, para a partida antiga saber como foi jogada")
        void noHistorico() {
            partida.encerrar(ResultadoXadrez.VITORIA_BRANCAS, MotivoXadrez.DESISTENCIA);
            sala.arquivarPartida(partida);

            List<XadrezResponse.PartidaXadrezResumo> historico = factory
                    .construir(sala, BRANCAS, "FIM").getHistorico();

            assertEquals(1, historico.size());
            assertEquals(Boolean.TRUE, historico.get(0).getModoVisual());
        }
    }

    @Nested
    @DisplayName("Resultado da partida que acabou de terminar")
    class PartidaEncerrada {
        // arquivarPartida zera partidaAtual ANTES do factory montar a resposta —
        // tanto no próprio evento FIM quanto num reload feito logo depois. Sem
        // isso, resultado/lances chegam nulos bem na hora em que o cliente mais
        // precisa deles: pra deixar o tabuleiro na tela pra reanalisar.

        @Test
        @DisplayName("resultado, motivo e lances vêm da partida arquivada, não da (agora nula) partidaAtual")
        void vemDaArquivada() {
            partida.encerrar(ResultadoXadrez.VITORIA_BRANCAS, MotivoXadrez.XEQUE_MATE);
            sala.arquivarPartida(partida);

            XadrezResponse r = factory.construir(sala, BRANCAS, "FIM");

            assertEquals("VITORIA_BRANCAS", r.getResultado());
            assertEquals("XEQUE_MATE", r.getMotivo());
            assertEquals(List.of("e4"), r.getLances());
            assertFalse(r.getPartidaEmAndamento());
        }

        @Test
        @DisplayName("vale pros dois jogadores e pro espectador — não é dado privado")
        void valeParaQualquerUm() {
            partida.encerrar(ResultadoXadrez.EMPATE, MotivoXadrez.REPETICAO_TRIPLA);
            sala.arquivarPartida(partida);

            assertEquals("EMPATE", factory.construir(sala, PRETAS, "FIM").getResultado());
            assertEquals("EMPATE", factory.construir(sala, ESPECTADOR, "FIM").getResultado());
            assertEquals("EMPATE", factory.construir(sala, null, "FIM").getResultado());
        }

        @Test
        @DisplayName("continua valendo num reload — não é só o evento FIM ao vivo")
        void sobreviveAoReload() {
            partida.encerrar(ResultadoXadrez.VITORIA_PRETAS, MotivoXadrez.TEMPO_ESGOTADO);
            sala.arquivarPartida(partida);

            // Um reload chama mostrar(), que pede a resposta sem nenhum evento.
            XadrezResponse r = factory.construir(sala, BRANCAS, null);

            assertEquals("VITORIA_PRETAS", r.getResultado());
            assertEquals("TEMPO_ESGOTADO", r.getMotivo());
        }

        @Test
        @DisplayName("sem pré-lances — não fazem sentido numa partida que já acabou")
        void semPreLances() {
            partida.encerrar(ResultadoXadrez.VITORIA_BRANCAS, MotivoXadrez.DESISTENCIA);
            sala.arquivarPartida(partida);

            assertNull(factory.construir(sala, BRANCAS, "FIM").getMeusPreLances());
        }

        @Test
        @DisplayName("sala sem nenhuma partida jogada: sem partida atual e sem arquivada, não inventa resultado")
        void semPartidaNenhuma() {
            SalaXadrez vazia = SalaXadrez.builder()
                    .nomeSala("vazia").usernameDono(BRANCAS)
                    .usernameBrancas(BRANCAS).usernamePretas(PRETAS)
                    .notacao(NotacaoXadrez.INGLESA).modoVisual(true)
                    .build();

            XadrezResponse r = factory.construir(vazia, BRANCAS, null);

            assertNull(r.getResultado());
            assertNull(r.getLances());
        }
    }
}
