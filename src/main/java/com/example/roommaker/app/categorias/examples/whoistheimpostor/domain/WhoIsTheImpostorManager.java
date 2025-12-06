package com.example.roommaker.app.categorias.examples.whoistheimpostor.domain;

import com.example.roommaker.app.categorias.examples.JogoPort;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostorResponse;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.sender.WhoIsTheImpostorNotifyPort;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.sender.WhoIsTheImpostorSender;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;
import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.Card;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostor;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.repository.WhoIsTheImpostorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class WhoIsTheImpostorManager implements JogoPort {

    private final WhoIsTheImpostorRepository repository;
    private final WhoIsTheImpostorNotifyPort notifyPort;

    public WhoIsTheImpostorManager(
            WhoIsTheImpostorRepository repository,
            WhoIsTheImpostorNotifyPort notifyPort
    ) {
        this.repository = repository;
        this.notifyPort = notifyPort;
    }

    // =====================================================
    // ✅ COMEÇAR (AGORA ENVIA AQUI)
    // =====================================================
    public void comecarPartida(Sala sala, String dono) {

        validarSala(sala, dono);

        WhoIsTheImpostor whoIsTheImpostor = new WhoIsTheImpostor();
        whoIsTheImpostor.setNomeSala(sala.getNome());
        whoIsTheImpostor.setUsernameDono(sala.getUsernameDono());
        whoIsTheImpostor.setJogando(true);

        List<String> jogadores = new ArrayList<>(sala.getUsernameParticipantes());
        jogadores.add(sala.getUsernameDono());

        if (jogadores.size() < 3) {
            throw new ErroDeRequisicaoGeral("Quantidade mínima de 3 jogadores");
        }

        whoIsTheImpostor.setJogadores(jogadores);

        Random random = new Random();
        whoIsTheImpostor.setImpostor(jogadores.get(random.nextInt(jogadores.size())));

        Card[] cartas = Card.values();
        whoIsTheImpostor.setCarta(cartas[random.nextInt(cartas.length)]);

        repository.save(whoIsTheImpostor);

        // ✅ ENVIO PARA CADA JOGADOR
        jogadores.forEach(jogador -> {
            WhoIsTheImpostorResponse response =
                    notifyPort.criarResponse(whoIsTheImpostor, jogador);

            notifyPort.enviarParaUsuario(
                    sala.getUsernameDono(),
                    sala.getNome(),
                    jogador,
                    response
            );
        });
    }

    // =====================================================
    // ✅ TERMINAR (AGORA ENVIA AQUI)
    // =====================================================
    public void terminarPartida(Sala sala, String dono) {

        validarSala(sala, dono);

        WhoIsTheImpostor jogo = zerarJogo(sala);

        List<String> ouvintes = new ArrayList<>(sala.getUsernameParticipantes());
        ouvintes.add(sala.getUsernameDono());

        WhoIsTheImpostorResponse response =
                notifyPort.criarResponsePartidaFinalizada(jogo);

        notifyPort.enviarParaTodos(
                sala.getUsernameDono(),
                sala.getNome(),
                ouvintes,
                response
        );
    }

    // =====================================================
    // ✅ MOSTRAR (AGORA ENVIA AQUI)
    // =====================================================
    public void mostrarJogoAtual(Sala sala, String username) {

        if (!sala.getCategoria().equals("whoistheimpostor")) {
            throw new ErroDeRequisicaoGeral("Categoria inválida");
        }

        WhoIsTheImpostor jogo =
                repository.findByNomeSalaAndUsernameDono(
                        sala.getNome(), sala.getUsernameDono()
                );

        WhoIsTheImpostorResponse response =
                notifyPort.criarResponse(jogo, username);

        notifyPort.enviarParaUsuario(
                sala.getUsernameDono(),
                sala.getNome(),
                username,
                response
        );
    }

    // =====================================================
    // ✅ SAÍDA DE PARTICIPANTE (AGORA ENVIA)
    // =====================================================
    @Override
    public void saidaDeParticipante(String usernameParticipante, Sala sala) {
        terminarPartida(sala, sala.getUsernameDono());
    }

    @Override public void validarSalaParaOJogo(Sala sala) {
        if(sala.getQtdCapacidade() < 3){
            throw new ErroDeRequisicaoGeral("Quantidade de participantes deve ser no mínimo 3 para jogar Who Is The Impostor");
        }
    }

    // =====================================================
    // ✅ DELETAR JOGO (AGORA ENVIA)
    // =====================================================
    @Override
    public void deletarJogo(Sala sala) {
        WhoIsTheImpostor jogo = zerarJogo(sala);

        List<String> ouvintes = new ArrayList<>(sala.getUsernameParticipantes());
        ouvintes.add(sala.getUsernameDono());

        WhoIsTheImpostorResponse response =
                notifyPort.criarResponsePartidaFinalizada(jogo);

        notifyPort.enviarParaTodos(
                sala.getUsernameDono(),
                sala.getNome(),
                ouvintes,
                response
        );

        repository.deleteByNomeSalaAndUsernameDono(
                sala.getNome(), sala.getUsernameDono()
        );
    }

    // =====================================================

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
}