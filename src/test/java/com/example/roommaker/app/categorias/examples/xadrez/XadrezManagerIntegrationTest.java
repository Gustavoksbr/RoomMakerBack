package com.example.roommaker.app.categorias.examples.xadrez;

import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezManager;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.*;
import com.example.roommaker.app.categorias.examples.xadrez.repository.SalaXadrezRepository;
import com.example.roommaker.app.categorias.examples.xadrez.sender.XadrezSender;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.RoommakerApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes de integração do XadrezManager contra MongoDB real (banco de testes).
 * O SalaManager e XadrezSender são mockados para isolar a lógica de xadrez.
 *
 * Usa ROOMMAKER_MONGODB_URI_TESTES do .env.
 */
@SpringBootTest(classes = RoommakerApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class XadrezManagerIntegrationTest {

    static {
        // Carrega .env antes do Spring subir
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

    private static final String DONO = "dono";
    private static final String PARTICIPANTE = "participante";
    private static final String SALA = "salaTeste";

    private Sala salaMock;

    @BeforeEach
    void setup() {
        // Limpa dados de teste
        repository.deleteByNomeSalaAndUsernameDono(SALA, DONO);

        // Sala mock com 2 jogadores
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

        // Sender não faz nada (mockado)
        doNothing().when(xadrezSender).enviarParaUsuario(anyString(), anyString(), anyString(), any());
        doNothing().when(xadrezSender).enviarParaTodos(anyString(), anyString(), any(), any());

        // Cria sala de jogo
        xadrezManager.criarSalaDeJogo(salaMock);
    }

    // =========================================================================
    // Configuração
    // =========================================================================

    @Test
    @Order(1)
    @DisplayName("configurar: define brancas, pretas e notação")
    void configurar() {
        xadrezManager.configurar(SALA, DONO, DONO, DONO, PARTICIPANTE, NotacaoXadrez.PORTUGUESA);

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertNotNull(s);
        assertEquals(DONO, s.getUsernameBrancas());
        assertEquals(PARTICIPANTE, s.getUsernamePretas());
        assertEquals(NotacaoXadrez.PORTUGUESA, s.getNotacao());
    }

    @Test
    @Order(2)
    @DisplayName("configurar: rejeita jogador que não está na sala")
    void configurarJogadorForaDaSala() {
        assertThrows(ErroDeRequisicaoGeral.class,
                () -> xadrezManager.configurar(SALA, DONO, DONO, DONO, "forasteiro", null));
    }

    @Test
    @Order(3)
    @DisplayName("configurar: rejeita brancas == pretas")
    void configurarMesmoJogador() {
        assertThrows(ErroDeRequisicaoGeral.class, () -> xadrezManager.configurar(SALA, DONO, DONO, DONO, DONO, null));
    }

    // =========================================================================
    // Iniciar partida
    // =========================================================================

    @Test
    @Order(10)
    @DisplayName("iniciarPartida: cria partida com ID 1")
    void iniciarPartida() {
        xadrezManager.configurar(SALA, DONO, DONO, DONO, PARTICIPANTE, null);
        xadrezManager.iniciarPartida(SALA, DONO, DONO);

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertNotNull(s.getPartidaAtual());
        assertEquals(1L, s.getPartidaAtual().getId());
        assertTrue(s.getPartidaAtual().emAndamento());
        assertEquals(2L, s.getProximoIdPartida());
    }

    @Test
    @Order(11)
    @DisplayName("iniciarPartida: rejeita sem configuração")
    void iniciarSemConfiguracao() {
        assertThrows(ErroDeRequisicaoGeral.class, () -> xadrezManager.iniciarPartida(SALA, DONO, DONO));
    }

    @Test
    @Order(12)
    @DisplayName("iniciarPartida: rejeita com partida já em andamento")
    void iniciarComPartidaEmAndamento() {
        xadrezManager.configurar(SALA, DONO, DONO, DONO, PARTICIPANTE, null);
        xadrezManager.iniciarPartida(SALA, DONO, DONO);

        assertThrows(ErroDeRequisicaoGeral.class, () -> xadrezManager.iniciarPartida(SALA, DONO, DONO));
    }

    // =========================================================================
    // Lance
    // =========================================================================

    private void configurarEIniciar() {
        xadrezManager.configurar(SALA, DONO, DONO, DONO, PARTICIPANTE, null);
        xadrezManager.iniciarPartida(SALA, DONO, DONO);
    }

    @Test
    @Order(20)
    @DisplayName("jogar: executa lance válido e persiste")
    void jogarLanceValido() {
        configurarEIniciar(); // DONO = brancas
        xadrezManager.jogar(SALA, DONO, DONO, "e4");

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertEquals(1, s.getPartidaAtual().getLances().size());
        assertEquals("e4", s.getPartidaAtual().getLances().get(0));
    }

    @Test
    @Order(21)
    @DisplayName("jogar: normaliza SAN — ed5 vira exd5")
    void jogarNormalizaSan() {
        configurarEIniciar();
        xadrezManager.jogar(SALA, DONO, DONO, "e4");
        xadrezManager.jogar(SALA, DONO, PARTICIPANTE, "d5");
        xadrezManager.jogar(SALA, DONO, DONO, "ed5");

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertEquals("exd5", s.getPartidaAtual().getLances().get(2));
    }

    @Test
    @Order(22)
    @DisplayName("jogar: rejeita quando não é a vez do jogador")
    void jogarForaDaVez() {
        configurarEIniciar(); // DONO = brancas, vez das brancas
        assertThrows(ErroDeRequisicaoGeral.class, () -> xadrezManager.jogar(SALA, DONO, PARTICIPANTE, "e5")); // participante
                                                                                                              // =
                                                                                                              // pretas,
                                                                                                              // mas é
                                                                                                              // vez das
                                                                                                              // brancas
    }

    @Test
    @Order(23)
    @DisplayName("jogar: notação inválida lança exceção sem incrementar contador")
    void jogarNotacaoInvalida() {
        configurarEIniciar();
        assertThrows(ErroDeRequisicaoGeral.class, () -> xadrezManager.jogar(SALA, DONO, DONO, "Ye4"));

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertEquals(0, s.getPartidaAtual().getLancesIlegaisBrancas());
    }

    @Test
    @Order(24)
    @DisplayName("jogar: lance ilegal incrementa contador das brancas")
    void jogarLanceIlegalBrancas() {
        configurarEIniciar();
        assertThrows(ErroDeRequisicaoGeral.class, () -> xadrezManager.jogar(SALA, DONO, DONO, "e5")); // ilegal na
                                                                                                      // posição inicial

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertEquals(1, s.getPartidaAtual().getLancesIlegaisBrancas());
        assertEquals(0, s.getPartidaAtual().getLancesIlegaisPretas());
    }

    @Test
    @Order(25)
    @DisplayName("jogar: lance ilegal incrementa contador das pretas")
    void jogarLanceIlegalPretas() {
        configurarEIniciar();
        xadrezManager.jogar(SALA, DONO, DONO, "e4"); // vez das pretas
        assertThrows(ErroDeRequisicaoGeral.class, () -> xadrezManager.jogar(SALA, DONO, PARTICIPANTE, "d4")); // ilegal
                                                                                                              // para
                                                                                                              // pretas

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertEquals(0, s.getPartidaAtual().getLancesIlegaisBrancas());
        assertEquals(1, s.getPartidaAtual().getLancesIlegaisPretas());
    }

    @Test
    @Order(26)
    @DisplayName("jogar: Fool's mate detecta xeque-mate e arquiva partida")
    void jogarFoolsMate() {
        configurarEIniciar(); // DONO = brancas, PARTICIPANTE = pretas
        xadrezManager.jogar(SALA, DONO, DONO, "f3");
        xadrezManager.jogar(SALA, DONO, PARTICIPANTE, "e5");
        xadrezManager.jogar(SALA, DONO, DONO, "g4");
        xadrezManager.jogar(SALA, DONO, PARTICIPANTE, "Qh4");

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertNull(s.getPartidaAtual(), "Partida deve ser arquivada após xeque-mate");
        assertFalse(s.getHistoricoPorUsername().isEmpty());

        List<PartidaXadrez> historicoDono = s.getHistoricoPorUsername().get(DONO);
        assertNotNull(historicoDono);
        assertEquals(1, historicoDono.size());
        assertEquals(ResultadoXadrez.VITORIA_PRETAS, historicoDono.get(0).getResultado());
        assertEquals(MotivoXadrez.XEQUE_MATE, historicoDono.get(0).getMotivo());
    }

    @Test
    @Order(27)
    @DisplayName("jogar: cancela proposta de empate ao jogar")
    void jogarCancelaPropostaEmpate() {
        configurarEIniciar();
        xadrezManager.proporEmpate(SALA, DONO, DONO);

        SalaXadrez antes = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertNotNull(antes.getPartidaAtual().getPropostaEmpate());

        xadrezManager.jogar(SALA, DONO, DONO, "e4");

        SalaXadrez depois = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertNull(depois.getPartidaAtual().getPropostaEmpate());
    }

    // =========================================================================
    // Desistir
    // =========================================================================

    @Test
    @Order(30)
    @DisplayName("desistir: brancas desistem, pretas vencem")
    void desistirBrancas() {
        configurarEIniciar(); // DONO = brancas
        xadrezManager.desistir(SALA, DONO, DONO);

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertNull(s.getPartidaAtual());
        List<PartidaXadrez> hist = s.getHistoricoPorUsername().get(DONO);
        assertEquals(ResultadoXadrez.VITORIA_PRETAS, hist.get(0).getResultado());
        assertEquals(MotivoXadrez.DESISTENCIA, hist.get(0).getMotivo());
    }

    @Test
    @Order(31)
    @DisplayName("desistir: pretas desistem, brancas vencem")
    void desistirPretas() {
        configurarEIniciar();
        xadrezManager.desistir(SALA, DONO, PARTICIPANTE);

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        List<PartidaXadrez> hist = s.getHistoricoPorUsername().get(PARTICIPANTE);
        assertEquals(ResultadoXadrez.VITORIA_BRANCAS, hist.get(0).getResultado());
    }

    @Test
    @Order(32)
    @DisplayName("desistir: rejeita sem partida em andamento")
    void desistirSemPartida() {
        assertThrows(ErroDeRequisicaoGeral.class, () -> xadrezManager.desistir(SALA, DONO, DONO));
    }

    // =========================================================================
    // Propor e responder empate
    // =========================================================================

    @Test
    @Order(40)
    @DisplayName("proporEmpate: registra proposta pendente")
    void proporEmpate() {
        configurarEIniciar();
        xadrezManager.proporEmpate(SALA, DONO, DONO);

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertEquals("BRANCAS", s.getPartidaAtual().getPropostaEmpate());
    }

    @Test
    @Order(41)
    @DisplayName("proporEmpate: rejeita proposta duplicada")
    void proporEmpateDuplicado() {
        configurarEIniciar();
        xadrezManager.proporEmpate(SALA, DONO, DONO);

        assertThrows(ErroDeRequisicaoGeral.class, () -> xadrezManager.proporEmpate(SALA, DONO, DONO));
    }

    @Test
    @Order(42)
    @DisplayName("responderEmpate: aceitar encerra com EMPATE / ACORDO_MUTUO")
    void responderEmpateAceitar() {
        configurarEIniciar();
        xadrezManager.proporEmpate(SALA, DONO, DONO); // brancas propõem
        xadrezManager.responderEmpate(SALA, DONO, PARTICIPANTE, true); // pretas aceitam

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertNull(s.getPartidaAtual());
        List<PartidaXadrez> hist = s.getHistoricoPorUsername().get(DONO);
        assertEquals(ResultadoXadrez.EMPATE, hist.get(0).getResultado());
        assertEquals(MotivoXadrez.ACORDO_MUTUO, hist.get(0).getMotivo());
    }

    @Test
    @Order(43)
    @DisplayName("responderEmpate: recusar mantém jogo em andamento")
    void responderEmpateRecusar() {
        configurarEIniciar();
        xadrezManager.proporEmpate(SALA, DONO, PARTICIPANTE); // pretas propõem
        xadrezManager.responderEmpate(SALA, DONO, DONO, false); // brancas recusam

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertNotNull(s.getPartidaAtual());
        assertTrue(s.getPartidaAtual().emAndamento());
        assertNull(s.getPartidaAtual().getPropostaEmpate());
    }

    @Test
    @Order(44)
    @DisplayName("responderEmpate: rejeita quando não há proposta pendente")
    void responderEmpateSePropostaPendente() {
        configurarEIniciar();
        assertThrows(ErroDeRequisicaoGeral.class, () -> xadrezManager.responderEmpate(SALA, DONO, PARTICIPANTE, true));
    }

    @Test
    @Order(45)
    @DisplayName("responderEmpate: quem propôs não pode responder")
    void responderEmpateQuemPropos() {
        configurarEIniciar();
        xadrezManager.proporEmpate(SALA, DONO, DONO); // brancas propõem
        assertThrows(ErroDeRequisicaoGeral.class, () -> xadrezManager.responderEmpate(SALA, DONO, DONO, true)); // brancas
                                                                                                                // tentam
                                                                                                                // aceitar
                                                                                                                // o
                                                                                                                // próprio
                                                                                                                // empate
    }

    // =========================================================================
    // Histórico
    // =========================================================================

    @Test
    @Order(50)
    @DisplayName("histórico: múltiplas partidas acumulam por username")
    void historicoMultiplasPartidas() {
        configurarEIniciar();
        xadrezManager.desistir(SALA, DONO, DONO); // partida 1

        xadrezManager.iniciarPartida(SALA, DONO, DONO); // partida 2
        xadrezManager.desistir(SALA, DONO, PARTICIPANTE);

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertEquals(2, s.getHistoricoPorUsername().get(DONO).size());
        assertEquals(2, s.getHistoricoPorUsername().get(PARTICIPANTE).size());
        assertEquals(1L, s.getHistoricoPorUsername().get(DONO).get(0).getId());
        assertEquals(2L, s.getHistoricoPorUsername().get(DONO).get(1).getId());
    }

    @Test
    @Order(51)
    @DisplayName("histórico: PGN formatado corretamente")
    void historicoPgn() {
        configurarEIniciar();
        xadrezManager.jogar(SALA, DONO, DONO, "e4");
        xadrezManager.jogar(SALA, DONO, PARTICIPANTE, "e5");
        xadrezManager.jogar(SALA, DONO, DONO, "Nf3");
        xadrezManager.desistir(SALA, DONO, PARTICIPANTE);

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        PartidaXadrez p = s.getHistoricoPorUsername().get(DONO).get(0);
        String pgn = p.pgn();
        assertTrue(pgn.contains("1. e4 e5"), "PGN deve conter '1. e4 e5', foi: " + pgn);
        assertTrue(pgn.contains("2. Nf3"), "PGN deve conter '2. Nf3', foi: " + pgn);
    }

    // =========================================================================
    // Fluxo completo
    // =========================================================================

    @Test
    @Order(60)
    @DisplayName("fluxo completo: Scholar's mate")
    void fluxoScholarsMate() {
        configurarEIniciar(); // DONO = brancas, PARTICIPANTE = pretas
        xadrezManager.jogar(SALA, DONO, DONO, "e4");
        xadrezManager.jogar(SALA, DONO, PARTICIPANTE, "e5");
        xadrezManager.jogar(SALA, DONO, DONO, "Bc4");
        xadrezManager.jogar(SALA, DONO, PARTICIPANTE, "Nc6");
        xadrezManager.jogar(SALA, DONO, DONO, "Qh5");
        xadrezManager.jogar(SALA, DONO, PARTICIPANTE, "Nf6");
        xadrezManager.jogar(SALA, DONO, DONO, "Qxf7");

        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertNull(s.getPartidaAtual());
        PartidaXadrez p = s.getHistoricoPorUsername().get(DONO).get(0);
        assertEquals(ResultadoXadrez.VITORIA_BRANCAS, p.getResultado());
        assertEquals(MotivoXadrez.XEQUE_MATE, p.getMotivo());
        assertEquals(7, p.getLances().size());
    }
}
