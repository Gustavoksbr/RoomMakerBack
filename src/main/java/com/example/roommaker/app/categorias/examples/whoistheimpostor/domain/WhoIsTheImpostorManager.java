package com.example.roommaker.app.categorias.examples.whoistheimpostor.domain;

import com.example.roommaker.app.categorias.examples.JogoPort;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostorResponse;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.sender.WhoIsTheImpostorSender;
import com.example.roommaker.app.domain.exceptions.Erro409;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.Card;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostor;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.repository.WhoIsTheImpostorRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WhoIsTheImpostorManager implements JogoPort {

    private final WhoIsTheImpostorRepository repository;
    private final WhoIsTheImpostorSender whoIsTheImpostorSender;
    private final SalaManager salaManager;

    public WhoIsTheImpostorManager(
            WhoIsTheImpostorRepository repository,
            WhoIsTheImpostorSender whoIsTheImpostorSender,
           @Lazy SalaManager salaManager
    ) {
        this.repository = repository;
        this.whoIsTheImpostorSender = whoIsTheImpostorSender;
        this.salaManager = salaManager;
    }

    public void comecarPartida(String nomeSala, String usernameDono, String username) {

        Sala sala = salaManager.mostrarSala(nomeSala, usernameDono);
        validarSala(sala, username);

        WhoIsTheImpostor whoIsTheImpostorExistente =
                repository.findByNomeSalaAndUsernameDono(
                        sala.getNome(), sala.getUsernameDono()
                );
        if (whoIsTheImpostorExistente != null && whoIsTheImpostorExistente.getJogando()) {
            throw new ErroDeRequisicaoGeral("Partida já está em andamento");
        }

        WhoIsTheImpostor jogo = new WhoIsTheImpostor();
        jogo.setNomeSala(sala.getNome());
        jogo.setUsernameDono(sala.getUsernameDono());
        jogo.setJogando(true);

        // adiciona jogadores (dono + participantes)
        List<String> jogadores = new ArrayList<>(sala.getUsernameParticipantes());
        jogadores.add(sala.getUsernameDono());
        if (jogadores.size() < 3) {
            throw new ErroDeRequisicaoGeral("Quantidade mínima de 3 jogadores");
        }
        jogo.setJogadores(jogadores);

        // escolhe impostor aleatoriamente
        Random random = new Random();
        jogo.setImpostor(jogadores.get(random.nextInt(jogadores.size())));

        // escolhe carta aleatoriamente
        Card[] cartas = Card.values();
        jogo.setCarta(cartas[random.nextInt(cartas.length)]);

        // inicializa votos com sets vazios
        jogo.setVotosPorvotador(new HashMap<>());
        for (String jogador : jogadores) {
            jogo.getVotosPorvotador().put(jogador, "");
        }

        repository.save(jogo);

        jogadores.forEach(jogador -> {
            WhoIsTheImpostorResponse response =
                    criarResponse(jogo, jogador,
                            jogo.getVotosPorvotador().getOrDefault(jogador, "")
                    );
            whoIsTheImpostorSender.enviarParaUsuario(
                    sala.getUsernameDono(),
                    sala.getNome(),
                    jogador,
                    response
            );
        });
    }

    public void terminarPartida(String nomeSala, String usernameDono, String username) {

        Sala sala = salaManager.mostrarSala(nomeSala, usernameDono);
        validarSala(sala, username);

        WhoIsTheImpostor jogo = zerarJogo(sala);

        List<String> ouvintes = new ArrayList<>(sala.getUsernameParticipantes());
        ouvintes.add(sala.getUsernameDono());

        WhoIsTheImpostorResponse response =
                criarResponse(jogo,"","");

        whoIsTheImpostorSender.enviarParaTodos(
                sala.getUsernameDono(),
                sala.getNome(),
                ouvintes,
                response
        );
    }


    public WhoIsTheImpostorResponse mostrarJogoAtual(String nomeSala, String usernameDono, String username) {

        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(
                nomeSala, usernameDono, username
        );

        if (!sala.getCategoria().equals("whoistheimpostor")) {
            throw new ErroDeRequisicaoGeral("Categoria inválida");
        }

        WhoIsTheImpostor jogo =
                repository.findByNomeSalaAndUsernameDono(
                        sala.getNome(), sala.getUsernameDono()
                );

        WhoIsTheImpostorResponse response =
                criarResponse(jogo, username,
                        jogo.getVotosPorvotador().getOrDefault(username, "")
                );

//        whoIsTheImpostorSender.enviarParaUsuario(
//                sala.getUsernameDono(),
//                sala.getNome(),
//                username,
//                response
//        );
        return response;
    }

    public void votar(String nomeSala, String usernameDono, String username, String usernameVotado) {
        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(
                nomeSala, usernameDono, username
        );
        WhoIsTheImpostor jogo =
                repository.findByNomeSalaAndUsernameDono(
                        sala.getNome(), sala.getUsernameDono()
                );
        if (!jogo.getJogando()) {
            throw new ErroDeRequisicaoGeral("Partida não está sendo jogada");
        }
        if(!jogo.getJogadores().contains(username)){
            throw new ErroDeRequisicaoGeral("Você não está na partida");
        }
        if (username.equals(usernameVotado)) {
            throw new Erro409("Não é possível votar em si mesmo");
        }
        if (!jogo.getJogadores().contains(usernameVotado)) {
            throw new ErroDeRequisicaoGeral("Usuário votado não está na partida");
        }
        jogo.getVotosPorvotador().put(username, usernameVotado);

        // verifica se todos votaram (dai termina a partida)
        long quantidadeVotos = jogo.getVotosPorvotador().values().stream()
                .filter(voto -> !voto.isEmpty())
                .count();
        if (jogo.getJogadores().size() == quantidadeVotos) {
            jogo.setJogando(false);
            repository.save(jogo);
            List<String> ouvintes = new ArrayList<>(sala.getUsernameParticipantes());
            ouvintes.add(sala.getUsernameDono());

            WhoIsTheImpostorResponse response =
                    criarResponse(jogo,username,jogo.getVotosPorvotador().getOrDefault(username, ""));

            whoIsTheImpostorSender.enviarParaTodos(
                    sala.getUsernameDono(),
                    sala.getNome(),
                    ouvintes,
                    response
            );
            return;
        }

        // se nem todos votaram, salva o voto, notifica todos e continua a partida
        repository.save(jogo);

        List<String> jogadores = jogo.getJogadores();
        jogadores.forEach(jogador -> {
            WhoIsTheImpostorResponse response =
                    criarResponse(jogo, jogador,
                            jogo.getVotosPorvotador().getOrDefault(jogador, "")
                    );
            whoIsTheImpostorSender.enviarParaUsuario(
                    sala.getUsernameDono(),
                    sala.getNome(),
                    jogador,
                    response
            );
        });
    }

    public void cancelarVoto(String nomeSala, String usernameDono, String username) {
        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(
                nomeSala, usernameDono, username
        );
        WhoIsTheImpostor jogo =
                repository.findByNomeSalaAndUsernameDono(
                        sala.getNome(), sala.getUsernameDono()
                );
        if (!jogo.getJogando()) {
            throw new ErroDeRequisicaoGeral("Partida não está sendo jogada");
        }
        if(!jogo.getJogadores().contains(username)){
            throw new ErroDeRequisicaoGeral("Você não está na partida");
        }
        jogo.getVotosPorvotador().put(username, "");

        repository.save(jogo);

        List<String> jogadores = jogo.getJogadores();
        jogadores.forEach(jogador -> {
            WhoIsTheImpostorResponse response =
                    criarResponse(jogo, jogador,
                            jogo.getVotosPorvotador().getOrDefault(jogador, "")
                    );
            whoIsTheImpostorSender.enviarParaUsuario(
                    sala.getUsernameDono(),
                    sala.getNome(),
                    jogador,
                    response
            );
        });

    }

    @Override
    public void saidaDeParticipante(String usernameParticipante, Sala sala) {
        WhoIsTheImpostor jogo =
                repository.findByNomeSalaAndUsernameDono(
                        sala.getNome(), sala.getUsernameDono()
                );
        if (jogo.getJogando() && jogo.getJogadores().contains(usernameParticipante)) {
            terminarPartida(sala.getNome(), sala.getUsernameDono() , sala.getUsernameDono());
        }
    }

    @Override public void validarSalaParaOJogo(Sala sala) {
        if(sala.getQtdCapacidade() < 3){
            throw new ErroDeRequisicaoGeral("Quantidade de participantes deve ser no mínimo 3 para jogar Who Is The Impostor");
        }
    }


    @Override
    public void deletarJogo(Sala sala) {
        WhoIsTheImpostor jogo = zerarJogo(sala);

        List<String> ouvintes = new ArrayList<>(sala.getUsernameParticipantes());
        ouvintes.add(sala.getUsernameDono());

        WhoIsTheImpostorResponse response =
                criarResponse(jogo,"","");

        whoIsTheImpostorSender.enviarParaTodos(
                sala.getUsernameDono(),
                sala.getNome(),
                ouvintes,
                response
        );

        repository.deleteByNomeSalaAndUsernameDono(
                sala.getNome(), sala.getUsernameDono()
        );
    }


    private void validarSala(Sala sala, String dono) {
        if (!sala.getCategoria().equals("whoistheimpostor")) {
            throw new ErroDeRequisicaoGeral("Categoria inválida");
        }
        if (!dono.equals(sala.getUsernameDono())) {
            throw new UsuarioNaoAutorizado("Somente o dono pode executar esta ação");
        }
    }

    private WhoIsTheImpostor zerarJogo(Sala sala) {
        WhoIsTheImpostor jogo =
                repository.findByNomeSalaAndUsernameDono(
                        sala.getNome(), sala.getUsernameDono()
                );

        jogo.setJogando(false);
        repository.save(jogo);
        return jogo;
    }

    public void criarSalaDeJogo(Sala sala) {
        WhoIsTheImpostor jogo = new WhoIsTheImpostor();
        jogo.setNomeSala(sala.getNome());
        jogo.setUsernameDono(sala.getUsernameDono());
        jogo.setJogando(false);
        jogo.setJogadores(new ArrayList<>());
        jogo.setImpostor(null);
        jogo.setCarta(null);
        repository.save(jogo);
    }



    private WhoIsTheImpostorResponse criarResponse(WhoIsTheImpostor jogo, String usuario, String votado) {

        WhoIsTheImpostorResponse r = new WhoIsTheImpostorResponse();

        //nao ta em jogo
        if (!jogo.getJogando()) {
            r.setPartidaSendoJogada(false);
            if(jogo.getImpostor() == null) {
                return r;
            }
            r.setJogadoresDaPartidaPassada(jogo.getJogadores());
            r.setImpostorDaPartidaPassada(jogo.getImpostor());
            r.setCartaDaPartidaPassada(jogo.getCarta());
            Map<String, Set<String>> votosInvertidos = new HashMap<>();
            for (Map.Entry<String, String> entry : jogo.getVotosPorvotador().entrySet()) {
                String votador = entry.getKey();
                String votadoPassada = entry.getValue();
                if (!votadoPassada.isEmpty()) {
                    votosInvertidos.putIfAbsent(votadoPassada, new HashSet<>());
                    votosInvertidos.get(votadoPassada).add(votador);
                }
            }
            r.setVotosPorVotadosDaPartidaPassada(votosInvertidos);
            return r;
        }

        //em jogo
        r.setPartidaSendoJogada(true);
        r.setJogadoresNaPartida(jogo.getJogadores());
        long quantidadeVotos = jogo.getVotosPorvotador().values().stream()
                .filter(voto -> !voto.isEmpty())
                .count();
        r.setQuantidadeVotos(quantidadeVotos);

        boolean impostor = jogo.getImpostor().equals(usuario);
        r.setIsImpostor(impostor);
        r.setCarta(impostor ? null : jogo.getCarta());

        if (!jogo.getJogadores().contains(usuario)) {
            r.setEstaNaPartida(false);
            return r;
        }
        r.setVotado(votado);
        r.setEstaNaPartida(true);
        return r;
    }
}