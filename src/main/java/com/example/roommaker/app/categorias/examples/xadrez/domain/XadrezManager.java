package com.example.roommaker.app.categorias.examples.xadrez.domain;

import com.example.roommaker.app.categorias.examples.JogoPort;
import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezLogica.*;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.*;
import com.example.roommaker.app.categorias.examples.xadrez.domain.service.XadrezTempoService;
import com.example.roommaker.app.categorias.examples.xadrez.repository.SalaXadrezRepository;
import com.example.roommaker.app.categorias.examples.xadrez.sender.XadrezSender;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.domain.models.Sala;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class XadrezManager implements JogoPort {

    private final SalaXadrezRepository repository;
    private final XadrezSender sender;
    private final SalaManager salaManager;
    private final XadrezTempoService tempoService;

    public XadrezManager(SalaXadrezRepository repository, XadrezSender sender,
            @Lazy SalaManager salaManager, XadrezTempoService tempoService) {
        this.repository = repository;
        this.sender = sender;
        this.salaManager = salaManager;
        this.tempoService = tempoService;
    }

    // -------------------------------------------------------------------------
    // Criação da sala (chamado pelo CategoriaService após criar sala)
    // -------------------------------------------------------------------------

    public void criarSalaDeJogo(Sala sala) {
        SalaXadrez salaXadrez = SalaXadrez.builder()
                .nomeSala(sala.getNome())
                .usernameDono(sala.getUsernameDono())
                .notacao(NotacaoXadrez.INGLESA)
                .build();
        repository.save(salaXadrez);
    }

    // -------------------------------------------------------------------------
    // Configuração (dono define brancas/pretas e notação)
    // -------------------------------------------------------------------------

    public void configurar(String nomeSala, String usernameDono, String username,
            String usernameBrancas, String usernamePretas, NotacaoXadrez notacao) {
        Sala sala = salaManager.mostrarSala(nomeSala, usernameDono);
        validarDono(sala, username);

        SalaXadrez salaXadrez = obterSala(nomeSala, usernameDono);
        if (salaXadrez.partidaEmAndamento()) {
            throw new ErroDeRequisicaoGeral("Não é possível alterar configurações com partida em andamento.");
        }

        // Valida que brancas e pretas são jogadores da sala
        List<String> jogadores = jogadoresDaSala(sala);
        if (!jogadores.contains(usernameBrancas)) {
            throw new ErroDeRequisicaoGeral("'" + usernameBrancas + "' não está na sala.");
        }
        if (!jogadores.contains(usernamePretas)) {
            throw new ErroDeRequisicaoGeral("'" + usernamePretas + "' não está na sala.");
        }
        if (usernameBrancas.equals(usernamePretas)) {
            throw new ErroDeRequisicaoGeral("Brancas e pretas devem ser jogadores diferentes.");
        }

        salaXadrez.setUsernameBrancas(usernameBrancas);
        salaXadrez.setUsernamePretas(usernamePretas);
        if (notacao != null)
            salaXadrez.setNotacao(notacao);
        repository.save(salaXadrez);

        enviarParaTodos(sala, salaXadrez, "CONFIGURACAO_ALTERADA");
    }

    // -------------------------------------------------------------------------
    // Iniciar partida
    // -------------------------------------------------------------------------

    public void iniciarPartida(String nomeSala, String usernameDono, String username) {
        Sala sala = salaManager.mostrarSala(nomeSala, usernameDono);
        validarDono(sala, username);

        SalaXadrez salaXadrez = obterSala(nomeSala, usernameDono);
        if (salaXadrez.partidaEmAndamento()) {
            throw new ErroDeRequisicaoGeral("Já há uma partida em andamento.");
        }
        if (salaXadrez.getUsernameBrancas() == null || salaXadrez.getUsernamePretas() == null) {
            throw new ErroDeRequisicaoGeral("Configure quem joga de brancas e de pretas antes de iniciar.");
        }

        PartidaXadrez partida = PartidaXadrez.builder()
                .id(salaXadrez.getProximoIdPartida())
                .usernameBrancas(salaXadrez.getUsernameBrancas())
                .usernamePretas(salaXadrez.getUsernamePretas())
                .notacao(salaXadrez.getNotacao())
                .build();
        salaXadrez.setProximoIdPartida(salaXadrez.getProximoIdPartida() + 1);
        salaXadrez.setPartidaAtual(partida);
        repository.save(salaXadrez);

        enviarParaTodos(sala, salaXadrez, "PARTIDA_INICIADA");
    }

    // -------------------------------------------------------------------------
    // Configurar e iniciar em um único passo
    // -------------------------------------------------------------------------

    @Transactional
    public void configurarEIniciar(String nomeSala, String usernameDono, String username,
            String usernameBrancas, String usernamePretas, NotacaoXadrez notacao,
            Integer tempoInicialBrancas, Integer incrementoBrancas,
            Integer tempoInicialPretas, Integer incrementoPretas) {
        Sala sala = salaManager.mostrarSala(nomeSala, usernameDono);
        validarDono(sala, username);

        SalaXadrez salaXadrez = obterSala(nomeSala, usernameDono);
        if (salaXadrez.partidaEmAndamento()) {
            throw new ErroDeRequisicaoGeral("Já há uma partida em andamento.");
        }

        List<String> jogadores = jogadoresDaSala(sala);
        if (!jogadores.contains(usernameBrancas)) {
            throw new ErroDeRequisicaoGeral("'" + usernameBrancas + "' não está na sala.");
        }
        if (!jogadores.contains(usernamePretas)) {
            throw new ErroDeRequisicaoGeral("'" + usernamePretas + "' não está na sala.");
        }
        if (usernameBrancas.equals(usernamePretas)) {
            throw new ErroDeRequisicaoGeral("Brancas e pretas devem ser jogadores diferentes.");
        }

        salaXadrez.setUsernameBrancas(usernameBrancas);
        salaXadrez.setUsernamePretas(usernamePretas);
        if (notacao != null)
            salaXadrez.setNotacao(notacao);

        // Cria controle de tempo usando o serviço (converte segundos para
        // milissegundos)
        ControleTempoXadrez controleTempo = tempoService.criarControleTempo(
                tempoInicialBrancas, incrementoBrancas,
                tempoInicialPretas, incrementoPretas);

        if (controleTempo != null) {
            controleTempo.inicializar();
        }

        PartidaXadrez partida = PartidaXadrez.builder()
                .id(salaXadrez.getProximoIdPartida())
                .usernameBrancas(usernameBrancas)
                .usernamePretas(usernamePretas)
                .notacao(notacao != null ? notacao : salaXadrez.getNotacao())
                .controleTempo(controleTempo)
                .build();
        salaXadrez.setProximoIdPartida(salaXadrez.getProximoIdPartida() + 1);
        salaXadrez.setPartidaAtual(partida);
        repository.save(salaXadrez);

        log.info("Partida iniciada na sala {}/{} - Brancas: {}, Pretas: {}, Tempo: {}",
                usernameDono, nomeSala, usernameBrancas, usernamePretas,
                controleTempo != null ? "configurado" : "infinito");

        enviarParaTodos(sala, salaXadrez, "PARTIDA_INICIADA");
    }

    // -------------------------------------------------------------------------
    // Lance
    // -------------------------------------------------------------------------

    @Transactional(noRollbackFor = ErroDeRequisicaoGeral.class)
    public void jogar(String nomeSala, String usernameDono, String username, String san) {
        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(nomeSala, usernameDono, username);
        SalaXadrez salaXadrez = obterSala(nomeSala, usernameDono);
        PartidaXadrez partida = exigirPartidaEmAndamento(salaXadrez);

        // Valida que é a vez do jogador
        boolean vezBrancas = partida.vezDasBrancas();
        String jogadorDaVez = vezBrancas ? salaXadrez.getUsernameBrancas() : salaXadrez.getUsernamePretas();
        if (!username.equals(jogadorDaVez)) {
            throw new ErroDeRequisicaoGeral("Não é a sua vez de jogar.");
        }

        // EVENTO: Verifica timeout ANTES de processar o lance
        XadrezTempoService.ResultadoTimeout timeout = tempoService.verificarTimeout(partida, salaXadrez);
        if (timeout != null) {
            partida.encerrar(timeout.resultado(), timeout.motivo());
            tempoService.congelarTempo(partida);
            salaXadrez.arquivarPartida(partida);
            repository.save(salaXadrez);
            enviarParaTodos(sala, salaXadrez, "FIM");
            throw new ErroDeRequisicaoGeral("Tempo esgotado!");
        }

        // Valida que o lance está na notação correta
        if (!NotacaoValidator.validarNotacao(san, salaXadrez.getNotacao())) {
            String erroNotacao = NotacaoValidator.obterErroNotacao(san, salaXadrez.getNotacao());
            if (erroNotacao != null) {
                throw new ErroDeRequisicaoGeral(erroNotacao);
            }
            throw new ErroDeRequisicaoGeral("Lance contém caracteres inválidos para a notação " +
                    salaXadrez.getNotacao().name().toLowerCase() + ".");
        }

        // Converte da notação configurada para inglês (SAN padrão)
        String sanIngles = NotacaoConverter.paraIngles(san, salaXadrez.getNotacao());

        Board board = XadrezLogica.reconstruirBoard(partida.getLances(), salaXadrez.getNotacao());
        Classificacao classif = XadrezLogica.classificarEntrada(board, sanIngles);

        switch (classif.tipo()) {
            case NOTACAO_INVALIDA -> {
                // Não penaliza, apenas avisa
                enviarParaUsuario(salaXadrez.getUsernameDono(), salaXadrez.getNomeSala(),
                        username, salaXadrez, "NOTACAO_INVALIDA");
                throw new ErroDeRequisicaoGeral("Notação inválida: '" + san + "'. Use SAN (ex: e4, Nf3, O-O).");
            }
            case LANCE_AMBIGUO -> {
                // Lance ambíguo é tratado como ilegal e penaliza
                partida.incrementarIlegais(vezBrancas);
                repository.save(salaXadrez);
                enviarParaTodos(sala, salaXadrez, "LANCE_ILEGAL");
                throw new ErroDeRequisicaoGeral(
                        "Lance ambíguo: '" + san + "'. Especifique qual peça mover.");
            }
            case LANCE_ILEGAL -> {
                partida.incrementarIlegais(vezBrancas);
                repository.save(salaXadrez);
                enviarParaTodos(sala, salaXadrez, "LANCE_ILEGAL");
                throw new ErroDeRequisicaoGeral("Lance ilegal na posição atual: '" + san + "'.");
            }
            case VALIDO -> {
                Move move = classif.move();
                String sanCanonica = XadrezLogica.sanCanonica(board, move, partida.getLances(),
                        salaXadrez.getNotacao());
                board.doMove(move);

                // Converte a SAN canônica (inglês) para a notação configurada antes de
                // armazenar
                String sanArmazenada = NotacaoConverter.deIngles(sanCanonica, salaXadrez.getNotacao());
                partida.getLances().add(sanArmazenada);
                partida.setPropostaEmpate(null); // jogar cancela proposta de empate

                // EVENTO: Processa tempo após o lance (adiciona incremento e atualiza
                // timestamp)
                tempoService.processarAposLance(partida, vezBrancas);

                ResultadoFim fim = XadrezLogica.verificarFim(board);
                if (fim != null) {
                    partida.encerrar(fim.resultado(), fim.motivo());
                    tempoService.congelarTempo(partida);
                    salaXadrez.arquivarPartida(partida);
                    repository.save(salaXadrez);
                    enviarParaTodos(sala, salaXadrez, "FIM");
                } else {
                    repository.save(salaXadrez);
                    enviarParaTodos(sala, salaXadrez, "LANCE");
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Desistir
    // -------------------------------------------------------------------------

    public void desistir(String nomeSala, String usernameDono, String username) {
        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(nomeSala, usernameDono, username);
        SalaXadrez salaXadrez = obterSala(nomeSala, usernameDono);
        PartidaXadrez partida = exigirPartidaEmAndamento(salaXadrez);

        boolean ehBrancas = username.equals(salaXadrez.getUsernameBrancas());
        ResultadoXadrez resultado = ehBrancas ? ResultadoXadrez.VITORIA_PRETAS : ResultadoXadrez.VITORIA_BRANCAS;
        partida.encerrar(resultado, MotivoXadrez.DESISTENCIA);
        salaXadrez.arquivarPartida(partida);
        repository.save(salaXadrez);

        enviarParaTodos(sala, salaXadrez, "FIM");
    }

    // -------------------------------------------------------------------------
    // Propor empate
    // -------------------------------------------------------------------------

    public void proporEmpate(String nomeSala, String usernameDono, String username) {
        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(nomeSala, usernameDono, username);
        SalaXadrez salaXadrez = obterSala(nomeSala, usernameDono);
        PartidaXadrez partida = exigirPartidaEmAndamento(salaXadrez);

        String lado = username.equals(salaXadrez.getUsernameBrancas()) ? "BRANCAS" : "PRETAS";
        if (lado.equals(partida.getPropostaEmpate())) {
            throw new ErroDeRequisicaoGeral("Você já propôs empate. Aguardando resposta.");
        }
        partida.setPropostaEmpate(lado);
        repository.save(salaXadrez);

        enviarParaTodos(sala, salaXadrez, "EMPATE_PROPOSTO");
    }

    // -------------------------------------------------------------------------
    // Responder empate
    // -------------------------------------------------------------------------

    public void responderEmpate(String nomeSala, String usernameDono, String username, boolean aceitar) {
        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(nomeSala, usernameDono, username);
        SalaXadrez salaXadrez = obterSala(nomeSala, usernameDono);
        PartidaXadrez partida = exigirPartidaEmAndamento(salaXadrez);

        if (!partida.temPropostaEmpate()) {
            throw new ErroDeRequisicaoGeral("Não há proposta de empate pendente.");
        }
        // Quem propôs não pode responder (seria aceitar o próprio empate)
        String ladoUsername = username.equals(salaXadrez.getUsernameBrancas()) ? "BRANCAS" : "PRETAS";
        if (ladoUsername.equals(partida.getPropostaEmpate())) {
            throw new ErroDeRequisicaoGeral("Você não pode responder à sua própria proposta de empate.");
        }

        if (aceitar) {
            partida.encerrar(ResultadoXadrez.EMPATE, MotivoXadrez.ACORDO_MUTUO);
            salaXadrez.arquivarPartida(partida);
            repository.save(salaXadrez);
            enviarParaTodos(sala, salaXadrez, "FIM");
        } else {
            partida.setPropostaEmpate(null);
            repository.save(salaXadrez);
            enviarParaTodos(sala, salaXadrez, "EMPATE_RECUSADO");
        }
    }

    // -------------------------------------------------------------------------
    // Mostrar estado atual (HTTP GET)
    // -------------------------------------------------------------------------

    public XadrezResponse mostrar(String nomeSala, String usernameDono, String username) {
        salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(nomeSala, usernameDono, username);
        SalaXadrez salaXadrez = obterSala(nomeSala, usernameDono);
        return construirResponse(salaXadrez, username, null);
    }

    // -------------------------------------------------------------------------
    // JogoPort
    // -------------------------------------------------------------------------

    @Override
    public void validarSalaParaOJogo(Sala sala) {
        if (sala.getQtdCapacidade() != null && sala.getQtdCapacidade() != 2) {
            throw new ErroDeRequisicaoGeral("Sala de xadrez deve ter capacidade de 2.");
        }
    }

    @Override
    public void saidaDeParticipante(String usernameParticipante, Sala sala) {
        SalaXadrez salaXadrez = repository.findByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
        if (salaXadrez == null)
            return;
        if (salaXadrez.partidaEmAndamento()) {
            // Quem saiu perde por desistência
            PartidaXadrez partida = salaXadrez.getPartidaAtual();
            boolean ehBrancas = usernameParticipante.equals(salaXadrez.getUsernameBrancas());
            ResultadoXadrez resultado = ehBrancas ? ResultadoXadrez.VITORIA_PRETAS : ResultadoXadrez.VITORIA_BRANCAS;
            partida.encerrar(resultado, MotivoXadrez.DESISTENCIA);
            salaXadrez.arquivarPartida(partida);
            repository.save(salaXadrez);

            List<String> ouvintes = jogadoresDaSala(sala);
            ouvintes.add(usernameParticipante);
            sender.enviarParaTodos(sala.getUsernameDono(), sala.getNome(), ouvintes,
                    construirResponseParaTodos(salaXadrez, "FIM"));
        }
        // Limpa configuração de brancas/pretas se o jogador que saiu estava configurado
        if (usernameParticipante.equals(salaXadrez.getUsernameBrancas())) {
            salaXadrez.setUsernameBrancas(null);
            repository.save(salaXadrez);
        } else if (usernameParticipante.equals(salaXadrez.getUsernamePretas())) {
            salaXadrez.setUsernamePretas(null);
            repository.save(salaXadrez);
        }
    }

    @Override
    public void deletarJogo(Sala sala) {
        SalaXadrez salaXadrez = repository.findByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
        if (salaXadrez == null)
            return;
        List<String> ouvintes = jogadoresDaSala(sala);
        sender.enviarParaTodos(sala.getUsernameDono(), sala.getNome(), ouvintes,
                construirResponseParaTodos(salaXadrez, "SALA_DELETADA"));
        repository.deleteByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private SalaXadrez obterSala(String nomeSala, String usernameDono) {
        SalaXadrez s = repository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
        if (s == null)
            throw new ErroDeRequisicaoGeral("Sala de xadrez não encontrada.");
        return s;
    }

    private PartidaXadrez exigirPartidaEmAndamento(SalaXadrez salaXadrez) {
        if (!salaXadrez.partidaEmAndamento()) {
            throw new ErroDeRequisicaoGeral("Não há partida em andamento.");
        }
        return salaXadrez.getPartidaAtual();
    }

    private void validarDono(Sala sala, String username) {
        if (!sala.getUsernameDono().equals(username)) {
            throw new UsuarioNaoAutorizado("Apenas o dono pode executar esta ação.");
        }
    }

    private List<String> jogadoresDaSala(Sala sala) {
        List<String> lista = new ArrayList<>(sala.getUsernameParticipantes());
        lista.add(sala.getUsernameDono());
        return lista;
    }

    private void enviarParaTodos(Sala sala, SalaXadrez salaXadrez, String evento) {
        List<String> ouvintes = jogadoresDaSala(sala);
        // Envia resposta personalizada (com histórico) para cada jogador
        for (String ouvinte : ouvintes) {
            XadrezResponse r = construirResponse(salaXadrez, ouvinte, evento);
            sender.enviarParaUsuario(salaXadrez.getUsernameDono(), salaXadrez.getNomeSala(), ouvinte, r);
        }
    }

    private void enviarParaUsuario(String usernameDono, String nomeSala, String username,
            SalaXadrez salaXadrez, String evento) {
        XadrezResponse r = construirResponse(salaXadrez, username, evento);
        sender.enviarParaUsuario(usernameDono, nomeSala, username, r);
    }

    private XadrezResponse construirResponseParaTodos(SalaXadrez salaXadrez, String evento) {
        return construirResponse(salaXadrez, null, evento);
    }

    private XadrezResponse construirResponse(SalaXadrez salaXadrez, String username, String evento) {
        PartidaXadrez partida = salaXadrez.getPartidaAtual();

        XadrezResponse.XadrezResponseBuilder builder = XadrezResponse.builder()
                .usernameBrancas(salaXadrez.getUsernameBrancas())
                .usernamePretas(salaXadrez.getUsernamePretas())
                .notacao(salaXadrez.getNotacao())
                .evento(evento)
                .partidaEmAndamento(salaXadrez.partidaEmAndamento());

        if (partida != null) {
            builder.partidaId(partida.getId())
                    .lances(new ArrayList<>(partida.getLances()))
                    .resultado(partida.getResultado() != null ? partida.getResultado().name() : null)
                    .motivo(partida.getMotivo() != null ? partida.getMotivo().name() : null)
                    .propostaEmpate(partida.getPropostaEmpate())
                    .lancesIlegaisBrancas(partida.getLancesIlegaisBrancas())
                    .lancesIlegaisPretas(partida.getLancesIlegaisPretas())
                    .vezDasBrancas(partida.vezDasBrancas());

            // Adiciona informações de tempo se existir controle de tempo
            if (partida.getControleTempo() != null) {
                ControleTempoXadrez ct = partida.getControleTempo();
                builder.tempoInicialBrancas(ct.getTempoInicialBrancasSegundos())
                        .tempoInicialPretas(ct.getTempoInicialPretasSegundos())
                        .incrementoBrancas(ct.getIncrementoBrancasSegundos())
                        .incrementoPretas(ct.getIncrementoPretasSegundos())
                        .tempoRestanteBrancas(ct.getTempoRestanteBrancasSegundos())
                        .tempoRestantePretas(ct.getTempoRestantePretasSegundos())
                        .timestampUltimoLance(ct.getTimestampUltimoLance());
            }
        }

        // Histórico personalizado por username (ordem decrescente — última partida
        // primeiro)
        if (username != null && salaXadrez.getHistoricoPorUsername().containsKey(username)) {
            List<XadrezResponse.PartidaXadrezResumo> historico = salaXadrez.getHistoricoPorUsername()
                    .get(username).stream()
                    .map(p -> {
                        XadrezResponse.PartidaXadrezResumo.PartidaXadrezResumoBuilder resumoBuilder = XadrezResponse.PartidaXadrezResumo
                                .builder()
                                .id(p.getId())
                                .pgn(p.pgn())
                                .lances(new ArrayList<>(p.getLances()))
                                .resultado(p.getResultado() != null ? p.getResultado().name() : null)
                                .motivo(p.getMotivo() != null ? p.getMotivo().name() : null)
                                .lancesIlegaisBrancas(p.getLancesIlegaisBrancas())
                                .lancesIlegaisPretas(p.getLancesIlegaisPretas())
                                .usernameBrancas(p.getUsernameBrancas())
                                .usernamePretas(p.getUsernamePretas())
                                .notacao(p.getNotacao());

                        // Adiciona informações de tempo no histórico
                        if (p.getControleTempo() != null) {
                            ControleTempoXadrez ct = p.getControleTempo();
                            resumoBuilder.tempoInicialBrancas(ct.getTempoInicialBrancasSegundos())
                                    .tempoInicialPretas(ct.getTempoInicialPretasSegundos())
                                    .incrementoBrancas(ct.getIncrementoBrancasSegundos())
                                    .incrementoPretas(ct.getIncrementoPretasSegundos());
                        }

                        return resumoBuilder.build();
                    })
                    .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                    .collect(Collectors.toList());
            builder.historico(historico);
        }

        return builder.build();
    }
}
