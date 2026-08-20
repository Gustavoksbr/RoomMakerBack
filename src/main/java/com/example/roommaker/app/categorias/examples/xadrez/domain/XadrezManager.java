package com.example.roommaker.app.categorias.examples.xadrez.domain;

import com.example.roommaker.app.categorias.examples.JogoPort;
import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezLogica.*;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.*;
import com.example.roommaker.app.categorias.examples.xadrez.domain.service.XadrezPreLanceService;
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

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class XadrezManager implements JogoPort {

    private final SalaXadrezRepository repository;
    private final XadrezSender sender;
    private final SalaManager salaManager;
    private final XadrezTempoService tempoService;
    private final XadrezPreLanceService preLanceService;
    private final XadrezResponseFactory responseFactory;

    public XadrezManager(SalaXadrezRepository repository, XadrezSender sender,
            @Lazy SalaManager salaManager, XadrezTempoService tempoService,
            XadrezPreLanceService preLanceService, XadrezResponseFactory responseFactory) {
        this.repository = repository;
        this.sender = sender;
        this.salaManager = salaManager;
        this.tempoService = tempoService;
        this.preLanceService = preLanceService;
        this.responseFactory = responseFactory;
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
    // Configuração (dono define brancas/pretas, notação e modo)
    // -------------------------------------------------------------------------

    public void configurar(String nomeSala, String usernameDono, String username,
            String usernameBrancas, String usernamePretas, NotacaoXadrez notacao, Boolean modoVisual) {
        Sala sala = salaManager.mostrarSala(nomeSala, usernameDono);
        validarDono(sala, username);

        SalaXadrez salaXadrez = obterSala(nomeSala, usernameDono);
        if (salaXadrez.partidaEmAndamento()) {
            throw new ErroDeRequisicaoGeral("Não é possível alterar configurações com partida em andamento.");
        }

        validarJogadores(sala, usernameBrancas, usernamePretas);

        salaXadrez.setUsernameBrancas(usernameBrancas);
        salaXadrez.setUsernamePretas(usernamePretas);
        if (notacao != null)
            salaXadrez.setNotacao(notacao);
        if (modoVisual != null)
            salaXadrez.setModoVisual(modoVisual);
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
                .modoVisual(salaXadrez.getModoVisual())
                .build();
        salaXadrez.setProximoIdPartida(salaXadrez.getProximoIdPartida() + 1);
        salaXadrez.setPartidaAtual(partida);
        repository.save(salaXadrez);

        enviarParaTodos(sala, salaXadrez, "PARTIDA_INICIADA");
    }

    // -------------------------------------------------------------------------
    // Configurar e iniciar em um único passo
    // -------------------------------------------------------------------------

    /**
     * SEM @Transactional de propósito: este método (como jogar, jogarCoordenadas
     * e definirPreLances) sempre escreve em exatamente UM documento
     * (repository.save(salaXadrez) roda uma única vez, em cada caminho do
     * método) — e o Mongo já garante atomicidade de documento único sozinho,
     * sem precisar de uma sessão transacional. A anotação não protegia nada
     * aqui; só pagava o custo de negociar uma transação (round-trips extras de
     * start/commit) em toda partida iniciada, todo lance, toda fila de
     * pré-lance — e contenção de sessão foi a causa observada de WriteConflict
     * entre este código e o scheduler de timeout rodando em paralelo.
     */
    public void configurarEIniciar(String nomeSala, String usernameDono, String username,
            String usernameBrancas, String usernamePretas, NotacaoXadrez notacao,
            Integer tempoInicialBrancas, Integer incrementoBrancas,
            Integer tempoInicialPretas, Integer incrementoPretas, Boolean modoVisual) {
        Sala sala = salaManager.mostrarSala(nomeSala, usernameDono);
        validarDono(sala, username);

        SalaXadrez salaXadrez = obterSala(nomeSala, usernameDono);
        if (salaXadrez.partidaEmAndamento()) {
            throw new ErroDeRequisicaoGeral("Já há uma partida em andamento.");
        }

        validarJogadores(sala, usernameBrancas, usernamePretas);

        salaXadrez.setUsernameBrancas(usernameBrancas);
        salaXadrez.setUsernamePretas(usernamePretas);
        if (notacao != null)
            salaXadrez.setNotacao(notacao);
        if (modoVisual != null)
            salaXadrez.setModoVisual(modoVisual);

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
                .modoVisual(salaXadrez.getModoVisual())
                .build();
        salaXadrez.setProximoIdPartida(salaXadrez.getProximoIdPartida() + 1);
        salaXadrez.setPartidaAtual(partida);
        repository.save(salaXadrez);

        log.info("Partida iniciada na sala {}/{} - Brancas: {}, Pretas: {}, Modo: {}, Tempo: {}",
                usernameDono, nomeSala, usernameBrancas, usernamePretas,
                Boolean.TRUE.equals(salaXadrez.getModoVisual()) ? "visual" : "às cegas",
                controleTempo != null ? "configurado" : "infinito");

        enviarParaTodos(sala, salaXadrez, "PARTIDA_INICIADA");
    }

    // -------------------------------------------------------------------------
    // Lance por notação (modo às cegas)
    // -------------------------------------------------------------------------

    // Ver o comentário em configurarEIniciar: um save() por caminho, um documento
    // só — @Transactional não protegia nada extra e só custava round-trips.
    public void jogar(String nomeSala, String usernameDono, String username, String san) {
        ContextoLance ctx = prepararLance(nomeSala, usernameDono, username);
        SalaXadrez salaXadrez = ctx.salaXadrez();

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
        Classificacao classif = XadrezLogica.classificarEntrada(ctx.board(), sanIngles);

        switch (classif.tipo()) {
            case NOTACAO_INVALIDA -> {
                // Não penaliza, apenas avisa
                enviarParaUsuario(salaXadrez.getUsernameDono(), salaXadrez.getNomeSala(),
                        username, salaXadrez, "NOTACAO_INVALIDA");
                throw new ErroDeRequisicaoGeral("Notação inválida: '" + san + "'. Use SAN (ex: e4, Nf3, O-O).");
            }
            case LANCE_AMBIGUO -> {
                // Lance ambíguo é tratado como ilegal e penaliza
                penalizarLanceIlegal(ctx);
                throw new ErroDeRequisicaoGeral(
                        "Lance ambíguo: '" + san + "'. Especifique qual peça mover.");
            }
            case LANCE_ILEGAL -> {
                penalizarLanceIlegal(ctx);
                throw new ErroDeRequisicaoGeral("Lance ilegal na posição atual: '" + san + "'.");
            }
            case VALIDO -> aplicarLanceERepercutir(ctx, classif.move());
        }
    }

    // -------------------------------------------------------------------------
    // Lance por coordenadas (modo visual)
    // -------------------------------------------------------------------------

    /**
     * Joga a partir de duas casas do tabuleiro, e não de uma SAN.
     *
     * Note que aqui NÃO existe "lance ilegal contabilizado": o contador de ilegais
     * é uma régua do modo às cegas, onde errar a posição de cabeça é parte do
     * jogo. Quem está olhando o tabuleiro só consegue mandar um lance ilegal se o
     * cliente estiver com estado velho — punir isso seria punir a rede.
     */
    // Ver o comentário em configurarEIniciar.
    public void jogarCoordenadas(String nomeSala, String usernameDono, String username,
            String from, String to, String promocao) {
        PreLance lance = PreLance.normalizar(from, to, promocao);
        if (lance == null) {
            throw new ErroDeRequisicaoGeral("Coordenadas inválidas: '" + from + "' -> '" + to + "'.");
        }

        ContextoLance ctx = prepararLance(nomeSala, usernameDono, username);
        Move move = XadrezLogica.resolverPorCoordenadas(ctx.board(),
                lance.getFrom(), lance.getTo(), lance.getPromocao());

        if (move == null) {
            // O marcador "(tabuleiro visual)" é o que deixa o frontend calar essa
            // mensagem específica sem calar a irmã dela do modo às cegas (que usa
            // o mesmo texto-base "Lance ilegal na posição atual", mas ali é um erro
            // de verdade do jogador — aqui só acontece por desync de rede, e o
            // cliente já reverte a peça sozinho, sem precisar de aviso.
            throw new ErroDeRequisicaoGeral(
                    "Lance ilegal na posição atual (tabuleiro visual): "
                            + lance.getFrom() + "-" + lance.getTo() + ".");
        }

        aplicarLanceERepercutir(ctx, move);
    }

    // -------------------------------------------------------------------------
    // Pré-lances
    // -------------------------------------------------------------------------

    /**
     * Substitui a fila de pré-lances do jogador pela fila inteira que o cliente
     * mandou.
     *
     * Substituir (em vez de acrescentar) é o que torna a operação idempotente: um
     * pacote repetido ou fora de ordem não duplica nem embaralha a fila, e o
     * cliente nunca precisa reconciliar diferenças com o servidor.
     *
     * Se, quando a fila chega, JÁ for a vez do jogador, a fila é aplicada na hora
     * — é a corrida normal entre "o adversário jogou" e "meu pré-lance saiu". Nesse
     * caso o primeiro lance é cobrado no relógio normalmente, porque o jogador de
     * fato estava com o relógio correndo: é isso que impede usar este endpoint
     * como um "lance de graça".
     */
    // Ver o comentário em configurarEIniciar.
    public void definirPreLances(String nomeSala, String usernameDono, String username, List<PreLance> fila) {
        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(nomeSala, usernameDono, username);
        SalaXadrez salaXadrez = obterSala(nomeSala, usernameDono);
        PartidaXadrez partida = exigirPartidaEmAndamento(salaXadrez);

        Boolean souBrancas = ladoDe(salaXadrez, username);
        if (souBrancas == null) {
            throw new ErroDeRequisicaoGeral("Você não está jogando esta partida.");
        }

        List<PreLance> normalizados = normalizarFila(fila);
        partida.definirPreLances(souBrancas, normalizados);

        // Chegou tarde: já é a vez de quem mandou. Aplica agora, cobrando o tempo
        // do primeiro lance — ele estava no relógio.
        if (partida.vezDasBrancas() == souBrancas && !normalizados.isEmpty()) {
            XadrezTempoService.ResultadoTimeout timeout = tempoService.verificarTimeout(partida, salaXadrez);
            if (timeout != null) {
                encerrarEArquivar(sala, salaXadrez, partida, timeout.resultado(), timeout.motivo());
                throw new ErroDeRequisicaoGeral("Tempo esgotado!");
            }

            Board board = XadrezLogica.reconstruirBoard(partida.getLances(), salaXadrez.getNotacao());
            String cancelado = usernameDoLado(salaXadrez,
                    preLanceService.aplicarCadeia(partida, board, salaXadrez.getNotacao(), true)
                            .ladoCanceladoBrancas());

            if (!partida.emAndamento()) {
                salaXadrez.arquivarPartida(partida);
                repository.save(salaXadrez);
                enviarParaTodos(sala, salaXadrez, "FIM", cancelado);
            } else {
                repository.save(salaXadrez);
                enviarParaTodos(sala, salaXadrez, "LANCE", cancelado);
            }
            return;
        }

        repository.save(salaXadrez);
        // Só quem enfileirou precisa saber. Avisar a sala inteira entregaria de
        // graça a informação de que o adversário está pré-lançando.
        enviarParaUsuario(usernameDono, nomeSala, username, salaXadrez, "PRE_LANCES_ATUALIZADOS");
    }

    /** Descarta a fila do jogador. Atalho para {@link #definirPreLances} com fila vazia. */
    public void limparPreLances(String nomeSala, String usernameDono, String username) {
        definirPreLances(nomeSala, usernameDono, username, List.of());
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
        encerrarEArquivar(sala, salaXadrez, partida, resultado, MotivoXadrez.DESISTENCIA);
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
            encerrarEArquivar(sala, salaXadrez, partida, ResultadoXadrez.EMPATE, MotivoXadrez.ACORDO_MUTUO);
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
        return responseFactory.construir(salaXadrez, username, null);
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
                    responseFactory.construir(salaXadrez, null, "FIM"));
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
                responseFactory.construir(salaXadrez, null, "SALA_DELETADA"));
        repository.deleteByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
    }

    // -------------------------------------------------------------------------
    // Fluxo compartilhado entre lance por SAN e lance por coordenadas
    // -------------------------------------------------------------------------

    /** Estado já validado e pronto para receber um lance. */
    private record ContextoLance(Sala sala, SalaXadrez salaXadrez, PartidaXadrez partida,
            Board board, boolean vezBrancas) {
    }

    /**
     * Valida vez, tempo e monta o tabuleiro da posição atual.
     * Lança {@link ErroDeRequisicaoGeral} — e pode encerrar a partida por tempo.
     */
    private ContextoLance prepararLance(String nomeSala, String usernameDono, String username) {
        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(nomeSala, usernameDono, username);
        SalaXadrez salaXadrez = obterSala(nomeSala, usernameDono);
        PartidaXadrez partida = exigirPartidaEmAndamento(salaXadrez);

        boolean vezBrancas = partida.vezDasBrancas();
        String jogadorDaVez = vezBrancas ? salaXadrez.getUsernameBrancas() : salaXadrez.getUsernamePretas();
        if (!username.equals(jogadorDaVez)) {
            throw new ErroDeRequisicaoGeral("Não é a sua vez de jogar.");
        }

        // EVENTO: Verifica timeout ANTES de processar o lance
        XadrezTempoService.ResultadoTimeout timeout = tempoService.verificarTimeout(partida, salaXadrez);
        if (timeout != null) {
            encerrarEArquivar(sala, salaXadrez, partida, timeout.resultado(), timeout.motivo());
            throw new ErroDeRequisicaoGeral("Tempo esgotado!");
        }

        Board board = XadrezLogica.reconstruirBoard(partida.getLances(), salaXadrez.getNotacao());
        return new ContextoLance(sala, salaXadrez, partida, board, vezBrancas);
    }

    /**
     * Aplica o lance do jogador e, em seguida, tudo o que ele destrava: o fim da
     * partida, ou a cadeia de pré-lances que agora ficou jogável.
     */
    private void aplicarLanceERepercutir(ContextoLance ctx, Move move) {
        PartidaXadrez partida = ctx.partida();
        SalaXadrez salaXadrez = ctx.salaXadrez();

        registrarLance(partida, ctx.board(), move, salaXadrez.getNotacao());

        // Guarda, não mecanismo: a cadeia sempre para no lado cuja fila esvaziou
        // (ou acabou de ser descartada por ilegal), então quem consegue jogar na
        // mão já está com a fila vazia. A linha existe para que uma mudança
        // futura na cadeia não faça um pré-lance velho disparar depois deste
        // lance, que mudou a posição para a qual ele tinha sido pensado.
        partida.limparPreLances(ctx.vezBrancas());
        tempoService.processarAposLance(partida, ctx.vezBrancas());

        ResultadoFim fim = XadrezLogica.verificarFim(ctx.board());
        if (fim != null) {
            encerrarEArquivar(ctx.sala(), salaXadrez, partida, fim.resultado(), fim.motivo());
            return;
        }

        String filaCancelada = usernameDoLado(salaXadrez,
                preLanceService.aplicarCadeia(partida, ctx.board(), salaXadrez.getNotacao(), false)
                        .ladoCanceladoBrancas());

        if (!partida.emAndamento()) {
            // A cadeia de pré-lances terminou a partida (mate, afogamento, ...).
            salaXadrez.arquivarPartida(partida);
            repository.save(salaXadrez);
            enviarParaTodos(ctx.sala(), salaXadrez, "FIM", filaCancelada);
            return;
        }

        repository.save(salaXadrez);
        enviarParaTodos(ctx.sala(), salaXadrez, "LANCE", filaCancelada);
    }

    /** Anota o lance em SAN na notação da sala e o executa no tabuleiro. */
    private void registrarLance(PartidaXadrez partida, Board board, Move move, NotacaoXadrez notacao) {
        String sanCanonica = XadrezLogica.sanCanonica(board, move, partida.getLances(), notacao);
        board.doMove(move);
        partida.getLances().add(NotacaoConverter.deIngles(sanCanonica, notacao));
        partida.setPropostaEmpate(null); // jogar cancela proposta de empate
    }

    private void penalizarLanceIlegal(ContextoLance ctx) {
        ctx.partida().incrementarIlegais(ctx.vezBrancas());
        repository.save(ctx.salaXadrez());
        enviarParaTodos(ctx.sala(), ctx.salaXadrez(), "LANCE_ILEGAL");
    }

    private void encerrarEArquivar(Sala sala, SalaXadrez salaXadrez, PartidaXadrez partida,
            ResultadoXadrez resultado, MotivoXadrez motivo) {
        partida.encerrar(resultado, motivo);
        tempoService.congelarTempo(partida);
        salaXadrez.arquivarPartida(partida);
        repository.save(salaXadrez);
        enviarParaTodos(sala, salaXadrez, "FIM");
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

    private void validarJogadores(Sala sala, String usernameBrancas, String usernamePretas) {
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
    }

    /** Traduz "o lado que teve a fila descartada" no username correspondente. */
    private String usernameDoLado(SalaXadrez salaXadrez, Boolean ladoBrancas) {
        if (ladoBrancas == null)
            return null;
        return ladoBrancas ? salaXadrez.getUsernameBrancas() : salaXadrez.getUsernamePretas();
    }

    /** true = joga de brancas, false = de pretas, null = não é jogador da partida. */
    private Boolean ladoDe(SalaXadrez salaXadrez, String username) {
        if (username == null)
            return null;
        if (username.equals(salaXadrez.getUsernameBrancas()))
            return Boolean.TRUE;
        if (username.equals(salaXadrez.getUsernamePretas()))
            return Boolean.FALSE;
        return null;
    }

    private List<PreLance> normalizarFila(List<PreLance> fila) {
        if (fila == null || fila.isEmpty())
            return List.of();

        if (fila.size() > PartidaXadrez.MAX_PRE_LANCES) {
            throw new ErroDeRequisicaoGeral(
                    "Máximo de " + PartidaXadrez.MAX_PRE_LANCES + " pré-lances enfileirados.");
        }

        List<PreLance> normalizados = new ArrayList<>(fila.size());
        for (PreLance bruto : fila) {
            PreLance ok = bruto == null ? null
                    : PreLance.normalizar(bruto.getFrom(), bruto.getTo(), bruto.getPromocao());
            if (ok == null) {
                throw new ErroDeRequisicaoGeral("Pré-lance com coordenadas inválidas.");
            }
            normalizados.add(ok);
        }
        return normalizados;
    }

    private List<String> jogadoresDaSala(Sala sala) {
        List<String> lista = new ArrayList<>(sala.getUsernameParticipantes());
        lista.add(sala.getUsernameDono());
        return lista;
    }

    private void enviarParaTodos(Sala sala, SalaXadrez salaXadrez, String evento) {
        enviarParaTodos(sala, salaXadrez, evento, null);
    }

    /**
     * @param usernameFilaCancelada quem deve receber o aviso de que a própria fila
     *                              de pré-lances foi descartada. Só ele recebe.
     */
    private void enviarParaTodos(Sala sala, SalaXadrez salaXadrez, String evento, String usernameFilaCancelada) {
        // Cada jogador recebe uma resposta própria: o histórico e a fila de
        // pré-lances são dados privados de cada um.
        for (String ouvinte : jogadoresDaSala(sala)) {
            XadrezResponse r = responseFactory.construir(salaXadrez, ouvinte, evento,
                    ouvinte.equals(usernameFilaCancelada));
            sender.enviarParaUsuario(salaXadrez.getUsernameDono(), salaXadrez.getNomeSala(), ouvinte, r);
        }
    }

    private void enviarParaUsuario(String usernameDono, String nomeSala, String username,
            SalaXadrez salaXadrez, String evento) {
        XadrezResponse r = responseFactory.construir(salaXadrez, username, evento);
        sender.enviarParaUsuario(usernameDono, nomeSala, username, r);
    }
}
