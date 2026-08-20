package com.example.roommaker.app.categorias.examples.xadrez;

import com.example.roommaker.RoommakerApplication;
import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezManager;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.*;
import com.example.roommaker.app.categorias.examples.xadrez.repository.SalaXadrezRepository;
import com.example.roommaker.app.categorias.examples.xadrez.sender.XadrezSender;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.domain.models.Sala;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pré-lances e modo visual atravessando o sistema inteiro: manager, MongoDB e
 * volta.
 *
 * A cadeia em si já é coberta por {@link XadrezPreLanceServiceTest}, sem banco.
 * O que se prova aqui é o que só aparece com persistência de verdade — que a
 * fila e o modo sobrevivem ao ida-e-volta do Mongo — e as regras que moram no
 * manager: quem pode enfileirar, o que zera a fila, e a corrida entre "o
 * adversário jogou" e "meu pré-lance saiu".
 *
 * Usa ROOMMAKER_MONGODB_URI_TESTES do .env.
 */
@SpringBootTest(classes = RoommakerApplication.class)
@ActiveProfiles("test")
class XadrezPreLanceIntegrationTest {

    static {
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
                .ignoreIfMissing().load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
    }

    @Autowired
    private XadrezManager xadrezManager;

    @Autowired
    private SalaXadrezRepository repository;

    @MockBean
    private SalaManager salaManager;

    @MockBean
    private XadrezSender xadrezSender;

    private static final String DONO = "donoPre";
    private static final String PARTICIPANTE = "participantePre";
    private static final String SALA = "salaPreLance";

    private Sala salaMock;

    @BeforeEach
    void setup() {
        repository.deleteByNomeSalaAndUsernameDono(SALA, DONO);

        salaMock = Sala.builder()
                .usernameDono(DONO)
                .nome(SALA)
                .categoria("xadrez")
                .qtdCapacidade(2L)
                .disponivel(true)
                .usernameParticipantes(new ArrayList<>(List.of(PARTICIPANTE)))
                .build();

        when(salaManager.mostrarSala(SALA, DONO)).thenReturn(salaMock);
        when(salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(anyString(), anyString(), anyString()))
                .thenReturn(salaMock);
        doNothing().when(xadrezSender).enviarParaUsuario(anyString(), anyString(), anyString(), any());
        doNothing().when(xadrezSender).enviarParaTodos(anyString(), anyString(), any(), any());

        xadrezManager.criarSalaDeJogo(salaMock);
    }

    @AfterEach
    void limpar() {
        repository.deleteByNomeSalaAndUsernameDono(SALA, DONO);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** DONO joga de brancas, PARTICIPANTE de pretas. Sem controle de tempo. */
    private void iniciarPartida(boolean modoVisual) {
        xadrezManager.configurarEIniciar(SALA, DONO, DONO, DONO, PARTICIPANTE,
                NotacaoXadrez.INGLESA, null, null, null, null, modoVisual);
    }

    private void iniciarPartidaComTempo(int segundos, int incremento) {
        xadrezManager.configurarEIniciar(SALA, DONO, DONO, DONO, PARTICIPANTE,
                NotacaoXadrez.INGLESA, segundos, incremento, segundos, incremento, true);
    }

    private SalaXadrez recarregar() {
        return repository.findByNomeSalaAndUsernameDono(SALA, DONO);
    }

    private PartidaXadrez partida() {
        return recarregar().getPartidaAtual();
    }

    private List<PreLance> fila(String... ucis) {
        List<PreLance> l = new ArrayList<>();
        for (String uci : ucis)
            l.add(PreLance.deUci(uci));
        return l;
    }

    // =========================================================================
    // Persistência
    // =========================================================================

    @Nested
    @DisplayName("Persistência")
    class Persistencia {

        @Test
        @DisplayName("modoVisual sobrevive ao ida-e-volta do Mongo")
        void modoVisualPersistido() {
            iniciarPartida(true);

            assertEquals(Boolean.TRUE, recarregar().getModoVisual());
            assertEquals(Boolean.TRUE, partida().getModoVisual(),
                    "a partida guarda o modo com que FOI jogada, para o histórico não mentir depois");
        }

        @Test
        @DisplayName("modo às cegas continua sendo o padrão")
        void asCegasPorPadrao() {
            iniciarPartida(false);
            assertEquals(Boolean.FALSE, recarregar().getModoVisual());
        }

        @Test
        @DisplayName("a fila de pré-lances sobrevive a um reload da página")
        void filaPersistida() {
            iniciarPartida(true);
            xadrezManager.definirPreLances(SALA, DONO, PARTICIPANTE, fila("e7e5", "g8f6"));

            // Releitura do banco: é o mesmo que o jogador teria depois de dar F5.
            assertEquals(fila("e7e5", "g8f6"), partida().getPreLancesPretas());
        }

        @Test
        @DisplayName("promoção enfileirada sobrevive junto com a peça escolhida")
        void filaComPromocaoPersistida() {
            iniciarPartida(true);
            xadrezManager.definirPreLances(SALA, DONO, PARTICIPANTE, fila("b2b1n"));

            PreLance guardado = partida().getPreLancesPretas().get(0);
            assertEquals("n", guardado.getPromocao());
        }

        @Test
        @DisplayName("o modo em que a partida foi jogada fica no histórico")
        void modoNoHistorico() {
            iniciarPartida(true);
            xadrezManager.desistir(SALA, DONO, DONO);

            XadrezResponse r = xadrezManager.mostrar(SALA, DONO, DONO);
            assertEquals(1, r.getHistorico().size());
            assertEquals(Boolean.TRUE, r.getHistorico().get(0).getModoVisual());
        }
    }

    // =========================================================================
    // Quem pode enfileirar
    // =========================================================================

    @Nested
    @DisplayName("Permissões")
    class Permissoes {

        @Test
        @DisplayName("quem não joga a partida não enfileira")
        void naoJogador() {
            iniciarPartida(true);
            salaMock.getUsernameParticipantes().add("intruso");

            assertThrows(ErroDeRequisicaoGeral.class,
                    () -> xadrezManager.definirPreLances(SALA, DONO, "intruso", fila("e7e5")));
        }

        @Test
        @DisplayName("sem partida em andamento não há o que enfileirar")
        void semPartida() {
            assertThrows(ErroDeRequisicaoGeral.class,
                    () -> xadrezManager.definirPreLances(SALA, DONO, DONO, fila("e2e4")));
        }

        @Test
        @DisplayName("coordenada inválida derruba a requisição inteira, não só o item ruim")
        void coordenadaInvalida() {
            iniciarPartida(true);

            assertThrows(ErroDeRequisicaoGeral.class, () -> xadrezManager.definirPreLances(
                    SALA, DONO, PARTICIPANTE, List.of(new PreLance("e7", "e5", null),
                            new PreLance("z9", "e5", null))));

            assertTrue(partida().getPreLancesPretas().isEmpty(),
                    "nada da fila suspeita entra — meia fila seria pior do que nenhuma");
        }

        @Test
        @DisplayName("fila acima do limite é recusada")
        void filaGrandeDemais() {
            iniciarPartida(true);
            List<PreLance> gigante = new ArrayList<>();
            for (int i = 0; i < PartidaXadrez.MAX_PRE_LANCES + 1; i++)
                gigante.add(new PreLance("e7", "e5", null));

            assertThrows(ErroDeRequisicaoGeral.class,
                    () -> xadrezManager.definirPreLances(SALA, DONO, PARTICIPANTE, gigante));
        }
    }

    // =========================================================================
    // Fluxo de jogo
    // =========================================================================

    @Nested
    @DisplayName("Fluxo de jogo")
    class Fluxo {

        @Test
        @DisplayName("o lance do adversário destrava o pré-lance na mesma requisição")
        void preLanceAplicadoNoLanceDoAdversario() {
            iniciarPartida(true);
            xadrezManager.definirPreLances(SALA, DONO, PARTICIPANTE, fila("e7e5"));

            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "e2", "e4", null);

            PartidaXadrez p = partida();
            assertEquals(List.of("e4", "e5"), p.getLances());
            assertTrue(p.getPreLancesPretas().isEmpty());
            assertTrue(p.vezDasBrancas(), "a vez já voltou — o pré-lance saiu junto");
        }

        @Test
        @DisplayName("a fila pinga um pré-lance por lance do adversário")
        void filaPingaUmPorVez() {
            iniciarPartida(true);
            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "e2", "e4", null);

            // Duas jogadas engatilhadas de uma vez, à moda do chess.com.
            xadrezManager.definirPreLances(SALA, DONO, DONO, fila("g1f3", "f1c4"));

            xadrezManager.jogarCoordenadas(SALA, DONO, PARTICIPANTE, "e7", "e5", null);
            assertEquals(List.of("e4", "e5", "Nf3"), partida().getLances());
            assertEquals(fila("f1c4"), partida().getPreLancesBrancas(),
                    "o segundo pré-lance espera o próximo lance do adversário");

            xadrezManager.jogarCoordenadas(SALA, DONO, PARTICIPANTE, "b8", "c6", null);
            assertEquals(List.of("e4", "e5", "Nf3", "Nc6", "Bc4"), partida().getLances());
            assertTrue(partida().getPreLancesBrancas().isEmpty());
        }

        @Test
        @DisplayName("quando dá para jogar na mão, a própria fila já foi consumida")
        void filaJaVaziaQuandoChegaAVez() {
            // A cadeia sempre para no lado cuja fila esvaziou. Logo, no instante em
            // que é a sua vez, não existe pré-lance seu pendurado — não dá para
            // jogar um lance na mão e ter um pré-lance velho disparando por cima.
            iniciarPartida(true);
            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "e2", "e4", null);
            xadrezManager.definirPreLances(SALA, DONO, DONO, fila("g1f3"));
            xadrezManager.jogarCoordenadas(SALA, DONO, PARTICIPANTE, "e7", "e5", null);

            assertEquals(List.of("e4", "e5", "Nf3"), partida().getLances());

            // As pretas jogam de novo; agora é a vez das brancas.
            xadrezManager.jogarCoordenadas(SALA, DONO, PARTICIPANTE, "d7", "d5", null);
            PartidaXadrez p = partida();
            assertTrue(p.vezDasBrancas());
            assertTrue(p.getPreLancesBrancas().isEmpty(),
                    "a fila foi consumida pela cadeia, não sobrou nada para atropelar o lance manual");

            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "d2", "d4", null);
            assertEquals(List.of("e4", "e5", "Nf3", "d5", "d4"), partida().getLances());
        }

        @Test
        @DisplayName("pré-lance que virou ilegal some sem virar lance ilegal")
        void preLanceIlegalNaoPenaliza() {
            iniciarPartida(true);
            xadrezManager.definirPreLances(SALA, DONO, PARTICIPANTE, fila("d8h4"));

            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "e2", "e4", null);

            PartidaXadrez p = partida();
            assertEquals(List.of("e4"), p.getLances());
            assertTrue(p.getPreLancesPretas().isEmpty());
            assertEquals(0, p.getLancesIlegaisPretas());
        }

        @Test
        @DisplayName("pré-lance pode dar mate e encerrar a partida")
        void preLanceDaMate() {
            iniciarPartida(true);
            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "e2", "e4", null);
            xadrezManager.jogarCoordenadas(SALA, DONO, PARTICIPANTE, "e7", "e5", null);
            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "f1", "c4", null);
            xadrezManager.jogarCoordenadas(SALA, DONO, PARTICIPANTE, "b8", "c6", null);
            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "d1", "h5", null);

            // As brancas deixam o mate do pastor engatilhado.
            xadrezManager.definirPreLances(SALA, DONO, DONO, fila("h5f7"));
            xadrezManager.jogarCoordenadas(SALA, DONO, PARTICIPANTE, "g8", "f6", null);

            SalaXadrez s = recarregar();
            assertNull(s.getPartidaAtual(), "a partida foi arquivada");
            XadrezResponse r = xadrezManager.mostrar(SALA, DONO, DONO);
            assertEquals("VITORIA_BRANCAS", r.getHistorico().get(0).getResultado());
            assertEquals("XEQUE_MATE", r.getHistorico().get(0).getMotivo());
        }

        @Test
        @DisplayName("desistir não deixa fila pendurada")
        void desistirLimpaFila() {
            iniciarPartida(true);
            xadrezManager.definirPreLances(SALA, DONO, PARTICIPANTE, fila("e7e5"));
            xadrezManager.desistir(SALA, DONO, DONO);

            XadrezResponse r = xadrezManager.mostrar(SALA, DONO, PARTICIPANTE);
            assertNull(r.getMeusPreLances(), "sem partida em andamento não há fila nenhuma");
        }

        @Test
        @DisplayName("mandar fila vazia cancela o que estava enfileirado")
        void cancelar() {
            iniciarPartida(true);
            xadrezManager.definirPreLances(SALA, DONO, PARTICIPANTE, fila("e7e5", "g8f6"));
            xadrezManager.limparPreLances(SALA, DONO, PARTICIPANTE);

            assertTrue(partida().getPreLancesPretas().isEmpty());
        }

        @Test
        @DisplayName("a fila mais nova substitui a anterior em vez de acumular")
        void substituiNaoAcumula() {
            iniciarPartida(true);
            xadrezManager.definirPreLances(SALA, DONO, PARTICIPANTE, fila("e7e5", "g8f6"));
            xadrezManager.definirPreLances(SALA, DONO, PARTICIPANTE, fila("d7d5"));

            assertEquals(fila("d7d5"), partida().getPreLancesPretas(),
                    "reenviar a fila inteira é o que torna a operação segura de repetir");
        }
    }

    // =========================================================================
    // A corrida
    // =========================================================================

    @Nested
    @DisplayName("Fila que chega quando já é a minha vez")
    class Corrida {

        @Test
        @DisplayName("é aplicada na hora, e não perdida")
        void aplicaNaHora() {
            iniciarPartida(true);
            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "e2", "e4", null);

            // O pré-lance saiu do navegador achando que ainda era vez das brancas.
            xadrezManager.definirPreLances(SALA, DONO, PARTICIPANTE, fila("e7e5"));

            assertEquals(List.of("e4", "e5"), partida().getLances());
        }

        @Test
        @DisplayName("cobra o relógio do primeiro lance — não é um lance de graça")
        void cobraORelogio() throws InterruptedException {
            iniciarPartidaComTempo(60, 0);
            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "e2", "e4", null);

            Thread.sleep(1200);
            xadrezManager.definirPreLances(SALA, DONO, PARTICIPANTE, fila("e7e5"));

            long restante = partida().getControleTempo().getTempoRestantePretas();
            assertTrue(restante < 59_500,
                    "as pretas estavam com o relógio correndo; sobrou " + restante + "ms");
        }

        @Test
        @DisplayName("pré-lance de verdade não paga o tempo do adversário")
        void preLanceRealNaoPaga() throws InterruptedException {
            iniciarPartidaComTempo(60, 0);
            xadrezManager.definirPreLances(SALA, DONO, PARTICIPANTE, fila("e7e5"));

            // As brancas pensam por um tempo antes de jogar.
            Thread.sleep(1200);
            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "e2", "e4", null);

            ControleTempoXadrez ct = partida().getControleTempo();
            assertTrue(ct.getTempoRestanteBrancas() < 59_500, "as brancas pensaram e pagaram");
            assertTrue(ct.getTempoRestantePretas() >= 59_900,
                    "as pretas já tinham decidido; sobrou " + ct.getTempoRestantePretas() + "ms");
        }
    }

    // =========================================================================
    // Lance por coordenadas
    // =========================================================================

    @Nested
    @DisplayName("Lance por coordenadas")
    class Coordenadas {

        @Test
        @DisplayName("grava a SAN canônica, e não as coordenadas")
        void gravaSan() {
            iniciarPartida(true);
            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "g1", "f3", null);

            assertEquals(List.of("Nf3"), partida().getLances());
        }

        @Test
        @DisplayName("respeita a notação portuguesa da sala")
        void notacaoPortuguesa() {
            xadrezManager.configurarEIniciar(SALA, DONO, DONO, DONO, PARTICIPANTE,
                    NotacaoXadrez.PORTUGUESA, null, null, null, null, true);

            xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "g1", "f3", null);

            assertEquals(List.of("Cf3"), partida().getLances());
        }

        @Test
        @DisplayName("lance ilegal no tabuleiro visual NÃO conta como lance ilegal")
        void ilegalNaoPenaliza() {
            // O contador de ilegais é régua do modo às cegas: errar a posição de
            // cabeça é parte daquele jogo. Quem está vendo o tabuleiro só erra se o
            // cliente estiver com estado velho — punir isso é punir a rede.
            iniciarPartida(true);

            assertThrows(ErroDeRequisicaoGeral.class,
                    () -> xadrezManager.jogarCoordenadas(SALA, DONO, DONO, "e2", "e5", null));

            assertEquals(0, partida().getLancesIlegaisBrancas());
        }

        @Test
        @DisplayName("não deixa jogar fora da vez")
        void foraDaVez() {
            iniciarPartida(true);
            assertThrows(ErroDeRequisicaoGeral.class,
                    () -> xadrezManager.jogarCoordenadas(SALA, DONO, PARTICIPANTE, "e7", "e5", null));
        }

        @Test
        @DisplayName("convive com lance por SAN na mesma partida")
        void convivemComSan() {
            iniciarPartida(false);
            xadrezManager.jogar(SALA, DONO, DONO, "e4");
            xadrezManager.jogarCoordenadas(SALA, DONO, PARTICIPANTE, "e7", "e5", null);

            assertEquals(List.of("e4", "e5"), partida().getLances());
        }
    }
}
