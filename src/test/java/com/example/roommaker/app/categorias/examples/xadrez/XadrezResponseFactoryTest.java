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
}
