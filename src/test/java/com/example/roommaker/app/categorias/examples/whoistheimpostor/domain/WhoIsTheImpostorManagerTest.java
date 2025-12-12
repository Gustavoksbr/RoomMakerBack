package com.example.roommaker.app.categorias.examples.whoistheimpostor.domain;

import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.Card;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostor;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostorResponse;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.repository.WhoIsTheImpostorRepository;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.sender.WhoIsTheImpostorSender;
import com.example.roommaker.app.domain.exceptions.Erro409;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.domain.models.Sala;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class WhoIsTheImpostorManagerTest {

    // Mocks para as dependências
    @Mock
    private WhoIsTheImpostorRepository repository;

    @Mock
    private WhoIsTheImpostorSender whoIsTheImpostorSender;

    @Mock
    private SalaManager salaManager;

    // A classe que queremos testar, com as dependências mockadas injetadas
    @InjectMocks
    private WhoIsTheImpostorManager manager;

    private static final String NOME_SALA = "SalaTeste";
    private static final String DONO = "donoUser";
    private static final String USUARIO_PARTICIPANTE = "part1";
    private static final String USUARIO_NAO_PARTICIPANTE = "naoPart";

    @BeforeEach
    public void setUp() {
        // Inicializa os mocks antes de cada teste
        MockitoAnnotations.openMocks(this);
    }

    // --- Métodos Auxiliares para criar objetos de teste ---

    private Sala criarSalaValida(List<String> participantes) {
        Sala sala = new Sala();
        // Os atributos são privados na Sala, vou assumir que há setters ou construtor
        // Para este teste, vamos simular o comportamento que a WhoIsTheImpostorManager espera.
        // Como não temos os setters/getters da classe Sala, assumo que os campos são acessíveis para simplificação no teste.
        // Na prática, você usaria os métodos da classe Sala para preencher.
        sala.setNome(NOME_SALA);
        sala.setUsernameDono(DONO);
        sala.setCategoria("whoistheimpostor");
        sala.setUsernameParticipantes(participantes);
        sala.setQtdCapacidade(5L); // Deve ser >= 3 para validarSalaParaOJogo
        return sala;
    }

    private WhoIsTheImpostor criarJogoEmAndamento(List<String> jogadores, String impostor, Card carta) {
        WhoIsTheImpostor jogo = new WhoIsTheImpostor();
        jogo.setNomeSala(NOME_SALA);
        jogo.setUsernameDono(DONO);
        jogo.setJogando(true);
        jogo.setJogadores(jogadores);
        jogo.setImpostor(impostor);
        jogo.setCarta(carta);
        HashMap<String, String> votos = new HashMap<>();
        for (String jogador : jogadores) {
            votos.put(jogador, ""); // Nenhum voto ainda
        }
        jogo.setVotosPorvotador(votos);
        return jogo;
    }

    private WhoIsTheImpostor criarJogoTerminado(List<String> jogadores, String impostor, Card carta, Map<String, String> votos) {
        WhoIsTheImpostor jogo = new WhoIsTheImpostor();
        jogo.setNomeSala(NOME_SALA);
        jogo.setUsernameDono(DONO);
        jogo.setJogando(false);
        jogo.setJogadores(jogadores);
        jogo.setImpostor(impostor);
        jogo.setCarta(carta);
        jogo.setVotosPorvotador(new HashMap<>(votos));
        return jogo;
    }


    // --- Testes para o método comecarPartida ---

    @Test
    public void comecarPartida_SucessoComMinimoDeJogadores() {
        // Preparação
        List<String> participantes = Arrays.asList("jogador1", "jogador2");
        Sala sala = criarSalaValida(participantes); // Total de jogadores: dono + 2 = 3
        when(salaManager.mostrarSala(eq(NOME_SALA), eq(DONO))).thenReturn(sala);
        doNothing().when(whoIsTheImpostorSender).enviarParaUsuario(any(), any(), any(), any());

        // Captura do objeto WhoIsTheImpostor salvo
        ArgumentCaptor<WhoIsTheImpostor> jogoCaptor = ArgumentCaptor.forClass(WhoIsTheImpostor.class);

        // Ação
        manager.comecarPartida(NOME_SALA, DONO, DONO);

        // Verificação
        verify(repository, times(1)).save(jogoCaptor.capture());
        WhoIsTheImpostor jogoSalvo = jogoCaptor.getValue();

        // Asserts
        assertNotNull(jogoSalvo, "O jogo não deve ser nulo");
        assertTrue(jogoSalvo.getJogando(), "O jogo deve estar em andamento");
        assertEquals(3, jogoSalvo.getJogadores().size(), "Deve haver 3 jogadores (dono + 2 participantes)");
        assertNotNull(jogoSalvo.getImpostor(), "Um impostor deve ser escolhido");
        assertNotNull(jogoSalvo.getCarta(), "Uma carta deve ser escolhida");
        assertTrue(jogoSalvo.getJogadores().contains(jogoSalvo.getImpostor()), "O impostor deve ser um dos jogadores");
        assertEquals(3, jogoSalvo.getVotosPorvotador().size(), "Deve haver uma entrada de voto para cada jogador");
        verify(whoIsTheImpostorSender, times(3)).enviarParaUsuario(any(), any(), any(), any());
    }

    @Test
    public void comecarPartida_Falha_MenosDeTresJogadores() {
        // Preparação
        List<String> participantes = Collections.singletonList("jogador1");
        Sala sala = criarSalaValida(participantes); // Total de jogadores: dono + 1 = 2
        when(salaManager.mostrarSala(eq(NOME_SALA), eq(DONO))).thenReturn(sala);

        // Ação e Verificação
        Exception exception = assertThrows(ErroDeRequisicaoGeral.class, () -> {
            manager.comecarPartida(NOME_SALA, DONO, DONO);
        });

        // Asserts
        assertTrue(exception.getMessage().contains("Quantidade mínima de 3 jogadores"), "Mensagem de erro incorreta");
        verify(repository, never()).save(any());
        verify(whoIsTheImpostorSender, never()).enviarParaUsuario(any(), any(), any(), any());
    }

    @Test
    public void comecarPartida_Falha_UsuarioNaoAutorizado() {
        // Preparação
        Sala sala = criarSalaValida(Arrays.asList("jogador1", "jogador2"));
        when(salaManager.mostrarSala(eq(NOME_SALA), eq(DONO))).thenReturn(sala);
        String usuarioNaoDono = "naoDono";

        // Ação e Verificação
        Exception exception = assertThrows(UsuarioNaoAutorizado.class, () -> {
            manager.comecarPartida(NOME_SALA, DONO, usuarioNaoDono);
        });

        // Asserts
        assertTrue(exception.getMessage().contains("Somente o dono pode executar esta ação"), "Mensagem de erro incorreta");
        verify(repository, never()).save(any());
    }

    // --- Testes para o método terminarPartida ---

    @Test
    public void terminarPartida_Sucesso() {
        // Preparação
        Sala sala = criarSalaValida(Arrays.asList(USUARIO_PARTICIPANTE));
        WhoIsTheImpostor jogo = criarJogoEmAndamento(
                Arrays.asList(DONO, USUARIO_PARTICIPANTE),
                DONO,
                Card.BATS
        );
        when(salaManager.mostrarSala(eq(NOME_SALA), eq(DONO))).thenReturn(sala);
        when(repository.findByNomeSalaAndUsernameDono(eq(NOME_SALA), eq(DONO))).thenReturn(jogo);

        ArgumentCaptor<WhoIsTheImpostor> jogoCaptor = ArgumentCaptor.forClass(WhoIsTheImpostor.class);
        ArgumentCaptor<List<String>> ouvintesCaptor = ArgumentCaptor.forClass(List.class);

        // Ação
        manager.terminarPartida(NOME_SALA, DONO, DONO);

        // Verificação
        verify(repository, times(1)).save(jogoCaptor.capture());
        verify(whoIsTheImpostorSender, times(1)).enviarParaTodos(eq(DONO), eq(NOME_SALA), ouvintesCaptor.capture(), any(WhoIsTheImpostorResponse.class));

        // Asserts
        assertFalse(jogoCaptor.getValue().getJogando(), "O jogo deve estar marcado como terminado (jogando=false)");
        List<String> ouvintes = ouvintesCaptor.getValue();
        assertEquals(2, ouvintes.size(), "Deve enviar a mensagem para o dono e 1 participante");
        assertTrue(ouvintes.contains(DONO) && ouvintes.contains(USUARIO_PARTICIPANTE), "A lista de ouvintes está incorreta");
    }

    // --- Testes para o método votar ---

    @Test
    public void votar_Sucesso_VotoSalvo() {
        // Preparação
        List<String> jogadores = Arrays.asList(DONO, "j2", "j3");
        Sala sala = criarSalaValida(Arrays.asList("j2", "j3"));
        WhoIsTheImpostor jogo = criarJogoEmAndamento(jogadores, "j3", Card.LOG);

        when(salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(eq(NOME_SALA), eq(DONO), eq(DONO))).thenReturn(sala);
        when(repository.findByNomeSalaAndUsernameDono(eq(NOME_SALA), eq(DONO))).thenReturn(jogo);

        ArgumentCaptor<WhoIsTheImpostor> jogoCaptor = ArgumentCaptor.forClass(WhoIsTheImpostor.class);

        // Ação: DONO vota em j2
        manager.votar(NOME_SALA, DONO, DONO, "j2");

        // Verificação
        verify(repository, times(1)).save(jogoCaptor.capture());
        // Deve notificar todos os jogadores com o estado atualizado
        verify(whoIsTheImpostorSender, times(jogadores.size())).enviarParaUsuario(any(), any(), any(), any());

        // Asserts
        WhoIsTheImpostor jogoSalvo = jogoCaptor.getValue();
        assertEquals("j2", jogoSalvo.getVotosPorvotador().get(DONO), "O voto deve ser registrado");
        assertTrue(jogoSalvo.getJogando(), "O jogo deve continuar em andamento");
        verify(whoIsTheImpostorSender, never()).enviarParaTodos(any(), any(), any(), any()); // Não termina a partida
    }

    @Test
    public void votar_Sucesso_PartidaTerminadaAposUltimoVoto() {
        // Preparação
        List<String> jogadores = Arrays.asList(DONO, "j2", "j3");
        Sala sala = criarSalaValida(Arrays.asList("j2", "j3"));
        // Jogo com 2 votos preenchidos (j2 votou em j3, j3 votou em dono)
        WhoIsTheImpostor jogo = criarJogoEmAndamento(jogadores, "j3", Card.LOG);
        jogo.getVotosPorvotador().put("j2", "j3");
        jogo.getVotosPorvotador().put("j3", DONO); // Votos iniciais

        when(salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(eq(NOME_SALA), eq(DONO), eq(DONO))).thenReturn(sala);
        when(repository.findByNomeSalaAndUsernameDono(eq(NOME_SALA), eq(DONO))).thenReturn(jogo);

        ArgumentCaptor<WhoIsTheImpostor> jogoCaptor = ArgumentCaptor.forClass(WhoIsTheImpostor.class);
        ArgumentCaptor<List<String>> ouvintesCaptor = ArgumentCaptor.forClass(List.class);

        // Ação: DONO vota em j2 (último voto)
        manager.votar(NOME_SALA, DONO, DONO, "j2");

        // Verificação
        verify(repository, times(1)).save(jogoCaptor.capture());

        // Deve notificar a todos que a partida terminou
        verify(whoIsTheImpostorSender, times(1)).enviarParaTodos(eq(DONO), eq(NOME_SALA), ouvintesCaptor.capture(), any(WhoIsTheImpostorResponse.class));
        verify(whoIsTheImpostorSender, never()).enviarParaUsuario(any(), any(), any(), any()); // Não deve notificar um por um

        // Asserts
        WhoIsTheImpostor jogoSalvo = jogoCaptor.getValue();
        assertEquals("j2", jogoSalvo.getVotosPorvotador().get(DONO), "O voto deve ser registrado");
        assertFalse(jogoSalvo.getJogando(), "O jogo deve estar terminado (todos votaram)");
        assertEquals(3, ouvintesCaptor.getValue().size(), "Deve enviar a mensagem para todos os 3 jogadores");
    }

    @Test
    public void votar_Falha_PartidaNaoEmAndamento() {
        // Preparação
        List<String> jogadores = Arrays.asList(DONO, "j2", "j3");
        Sala sala = criarSalaValida(Arrays.asList("j2", "j3"));
        WhoIsTheImpostor jogoTerminado = criarJogoTerminado(jogadores, "j3", Card.LOG, Map.of("j2", "j3", "j3", DONO, DONO, "j2"));

        when(salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(eq(NOME_SALA), eq(DONO), eq(DONO))).thenReturn(sala);
        when(repository.findByNomeSalaAndUsernameDono(eq(NOME_SALA), eq(DONO))).thenReturn(jogoTerminado);

        // Ação e Verificação
        Exception exception = assertThrows(ErroDeRequisicaoGeral.class, () -> {
            manager.votar(NOME_SALA, DONO, DONO, "j2");
        });

        // Asserts
        assertTrue(exception.getMessage().contains("Partida não está sendo jogada"), "Mensagem de erro incorreta");
        verify(repository, never()).save(any());
        verify(whoIsTheImpostorSender, never()).enviarParaUsuario(any(), any(), any(), any());
    }

    @Test
    public void votar_Falha_VotarEmSiMesmo() {
        // Preparação
        List<String> jogadores = Arrays.asList(DONO, "j2", "j3");
        Sala sala = criarSalaValida(Arrays.asList("j2", "j3"));
        WhoIsTheImpostor jogo = criarJogoEmAndamento(jogadores, "j3", Card.LOG);

        when(salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(eq(NOME_SALA), eq(DONO), eq(DONO))).thenReturn(sala);
        when(repository.findByNomeSalaAndUsernameDono(eq(NOME_SALA), eq(DONO))).thenReturn(jogo);

        // Ação e Verificação
        Exception exception = assertThrows(Erro409.class, () -> {
            manager.votar(NOME_SALA, DONO, DONO, DONO); // Votando em si mesmo
        });

        // Asserts
        assertTrue(exception.getMessage().contains("Não é possível votar em si mesmo"), "Mensagem de erro incorreta");
        verify(repository, never()).save(any());
    }

    // --- Testes para o método cancelarVoto ---

    @Test
    public void cancelarVoto_Sucesso() {
        // Preparação
        List<String> jogadores = Arrays.asList(DONO, "j2", "j3");
        Sala sala = criarSalaValida(Arrays.asList("j2", "j3"));
        WhoIsTheImpostor jogo = criarJogoEmAndamento(jogadores, "j3", Card.LOG);
        jogo.getVotosPorvotador().put(DONO, "j2"); // DONO já votou

        when(salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(eq(NOME_SALA), eq(DONO), eq(DONO))).thenReturn(sala);
        when(repository.findByNomeSalaAndUsernameDono(eq(NOME_SALA), eq(DONO))).thenReturn(jogo);

        ArgumentCaptor<WhoIsTheImpostor> jogoCaptor = ArgumentCaptor.forClass(WhoIsTheImpostor.class);

        // Ação: DONO cancela voto
        manager.cancelarVoto(NOME_SALA, DONO, DONO);

        // Verificação
        verify(repository, times(1)).save(jogoCaptor.capture());
        // Deve notificar todos os jogadores
        verify(whoIsTheImpostorSender, times(jogadores.size())).enviarParaUsuario(any(), any(), any(), any());

        // Asserts
        WhoIsTheImpostor jogoSalvo = jogoCaptor.getValue();
        assertEquals("", jogoSalvo.getVotosPorvotador().get(DONO), "O voto deve ser removido (string vazia)");
        assertTrue(jogoSalvo.getJogando(), "O jogo deve continuar em andamento");
    }

    // --- Testes para o método validarSalaParaOJogo ---

    @Test
    public void validarSalaParaOJogo_Sucesso_Minimo3() {
        // Preparação
        Sala sala = criarSalaValida(Arrays.asList("p1", "p2")); // Capacidade 5, 3 jogadores

        // Ação: Não deve lançar exceção
        manager.validarSalaParaOJogo(sala);

        // Verificação
        // Se chegou aqui, não lançou exceção, o que é o sucesso esperado.
    }

    @Test
    public void validarSalaParaOJogo_Falha_MenosDe3() {
        // Preparação
        Sala sala = criarSalaValida(Arrays.asList("p1"));
        sala.setQtdCapacidade(2L); // Capacidade 2, 2 jogadores

        // Ação e Verificação
        Exception exception = assertThrows(ErroDeRequisicaoGeral.class, () -> {
            manager.validarSalaParaOJogo(sala);
        });

        // Asserts
        assertTrue(exception.getMessage().contains("Quantidade de participantes deve ser no mínimo 3"), "Mensagem de erro incorreta");
    }

    // --- Testes para o método saidaDeParticipante ---

    @Test
    public void saidaDeParticipante_PartidaEmAndamento_ParticipanteNoJogo() {
        // Preparação
        String participanteSaindo = "j2";
        Sala sala = criarSalaValida(Arrays.asList(participanteSaindo, "j3"));
        WhoIsTheImpostor jogo = criarJogoEmAndamento(
                Arrays.asList(DONO, participanteSaindo, "j3"),
                "j3",
                Card.FIRESPIRIT
        );

        // Configuramos o comportamento do mock de repository para o `saidaDeParticipante`
        when(repository.findByNomeSalaAndUsernameDono(eq(NOME_SALA), eq(DONO))).thenReturn(jogo);

        // Configuramos o comportamento do mock de repository/salaManager para o `terminarPartida` que é chamado
        // O terminarPartida será chamado pelo *dono* (último parâmetro), então ele buscará a sala e o jogo novamente.
        when(salaManager.mostrarSala(eq(NOME_SALA), eq(DONO))).thenReturn(sala);
        // Mockamos o retorno do jogo após a primeira chamada para que `terminarPartida` possa "zerar" o jogo
        when(repository.findByNomeSalaAndUsernameDono(eq(NOME_SALA), eq(DONO))).thenReturn(jogo);

        ArgumentCaptor<WhoIsTheImpostor> jogoCaptor = ArgumentCaptor.forClass(WhoIsTheImpostor.class);

        // Ação
        manager.saidaDeParticipante(participanteSaindo, sala);

        // Verificação
        // Deve chamar terminarPartida, que por sua vez chama save e enviarParaTodos
        verify(repository, times(1)).save(jogoCaptor.capture());
        verify(whoIsTheImpostorSender, times(1)).enviarParaTodos(any(), any(), any(), any());

        // Asserts
        assertFalse(jogoCaptor.getValue().getJogando(), "O jogo deve ser terminado após a saída de um jogador");
    }

    @Test
    public void saidaDeParticipante_PartidaNaoEmAndamento() {
        // Preparação
        String participanteSaindo = "j2";
        Sala sala = criarSalaValida(Arrays.asList(participanteSaindo, "j3"));
        WhoIsTheImpostor jogoTerminado = criarJogoTerminado(
                Arrays.asList(DONO, participanteSaindo, "j3"),
                "j3",
                Card.FIRESPIRIT,
                Map.of()
        );

        when(repository.findByNomeSalaAndUsernameDono(eq(NOME_SALA), eq(DONO))).thenReturn(jogoTerminado);

        // Ação
        manager.saidaDeParticipante(participanteSaindo, sala);

        // Verificação
        // Não deve chamar terminarPartida, então não deve haver save ou envio de mensagem
        verify(repository, never()).save(any());
        verify(whoIsTheImpostorSender, never()).enviarParaTodos(any(), any(), any(), any());
    }

    // --- Testes para o método deletarJogo ---

    @Test
    public void deletarJogo_Sucesso() {
        // Preparação
        Sala sala = criarSalaValida(Arrays.asList(USUARIO_PARTICIPANTE));
        WhoIsTheImpostor jogo = criarJogoEmAndamento(
                Arrays.asList(DONO, USUARIO_PARTICIPANTE),
                DONO,
                Card.BATS
        );
        when(repository.findByNomeSalaAndUsernameDono(eq(NOME_SALA), eq(DONO))).thenReturn(jogo);

        ArgumentCaptor<WhoIsTheImpostor> jogoCaptor = ArgumentCaptor.forClass(WhoIsTheImpostor.class);
        ArgumentCaptor<List<String>> ouvintesCaptor = ArgumentCaptor.forClass(List.class);

        // Ação
        manager.deletarJogo(sala);

        // Verificação
        // Deve zerar o jogo antes de deletar (chama save)
        verify(repository, times(1)).save(jogoCaptor.capture());
        // Deve notificar a todos
        verify(whoIsTheImpostorSender, times(1)).enviarParaTodos(eq(DONO), eq(NOME_SALA), ouvintesCaptor.capture(), any(WhoIsTheImpostorResponse.class));
        // Deve deletar o jogo
        verify(repository, times(1)).deleteByNomeSalaAndUsernameDono(eq(NOME_SALA), eq(DONO));

        // Asserts
        assertFalse(jogoCaptor.getValue().getJogando(), "O jogo deve ser zerado (jogando=false) antes de ser deletado");
        List<String> ouvintes = ouvintesCaptor.getValue();
        assertEquals(2, ouvintes.size(), "Deve enviar a mensagem para o dono e 1 participante");
    }
}