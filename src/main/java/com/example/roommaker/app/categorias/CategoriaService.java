package com.example.roommaker.app.categorias;

import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.WhoIsTheImpostorManager;
import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.categorias.examples.chat.core.ChatManager;
import com.example.roommaker.app.categorias.examples.jokenpo.domain.JokenpoManager;
import com.example.roommaker.app.categorias.examples.tictactoe.domain.TicTacToeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {
    // private final SalaRepository salaRepository;
    private final TicTacToeManager ticTacToeManager;
    private final JokenpoManager jokenpoManager;
    private final ChatManager chatManager;
    private final WhoIsTheImpostorManager whoIsTheImpostorManager;
    // private final CoupManager coupManager; //futuramente sera adicionado

    @Autowired
    public CategoriaService(TicTacToeManager ticTacToeManager, JokenpoManager jokenpoManager, ChatManager chatManager,
            WhoIsTheImpostorManager whoIsTheImpostorManager) {
        // this.salaRepository = salaRepository;
        this.ticTacToeManager = ticTacToeManager;
        this.jokenpoManager = jokenpoManager;
        this.chatManager = chatManager;
        this.whoIsTheImpostorManager = whoIsTheImpostorManager;
    }

    // public void verificarSeUsuarioEstaNoJogo(String usernameParticipante, String
    // usernameDono, String nomeSala) {
    // Sala sala =
    // this.salaRepository.verificarSeUsuarioEstaNaSalaERetornarSala(nomeSala,
    // usernameDono, usernameParticipante);
    // }

    public void notificarSaidaDeUsuario(String usernameParticipante, Sala sala) {
        if (sala.getCategoria().equals("tictactoe")) {
            this.ticTacToeManager.saidaDeParticipante(usernameParticipante, sala);
        } else if (sala.getCategoria().equals("jokenpo")) {
            this.jokenpoManager.saidaDeParticipante(usernameParticipante, sala);
        } else if (sala.getCategoria().equals("whoistheimpostor")) {
            this.whoIsTheImpostorManager.saidaDeParticipante(usernameParticipante, sala);
        }

    }

    public void validarSalaParaOJogo(Sala sala) {
        if (!List.of("tictactoe", "jokenpo", "chat", "whoistheimpostor").contains(sala.getCategoria())) {
            throw new ErroDeRequisicaoGeral("Categoria inválida");
        }

        if (sala.getCategoria().equals("tictactoe")) {
            this.ticTacToeManager.validarSalaParaOJogo(sala);
        } else if (sala.getCategoria().equals("jokenpo")) {
            this.jokenpoManager.validarSalaParaOJogo(sala);
        } else if (sala.getCategoria().equals("whoistheimpostor")) {
            this.whoIsTheImpostorManager.validarSalaParaOJogo(sala);
        }
    }

    /**
     * Verifica se a categoria da sala exige uma capacidade fixa.
     * Categorias com capacidade fixa: tictactoe (2), jokenpo (2).
     * Categorias com capacidade mínima: whoistheimpostor (mínimo 3).
     */
    public void validarAlteracaoDeCapacidade(Sala sala, Long novaCapacidade) {
        String categoria = sala.getCategoria();
        if (categoria.equals("tictactoe") || categoria.equals("jokenpo")) {
            throw new ErroDeRequisicaoGeral(
                    "A categoria '" + categoria + "' exige capacidade fixa de 2 jogadores e não pode ser alterada.");
        }
        if (categoria.equals("whoistheimpostor")) {
            if (novaCapacidade != null && novaCapacidade < 3) {
                throw new ErroDeRequisicaoGeral("A categoria 'whoistheimpostor' exige no mínimo 3 jogadores.");
            }
        }
        if (novaCapacidade != null && novaCapacidade < 2) {
            throw new ErroDeRequisicaoGeral("Capacidade mínima é 2.");
        }
    }

    public void excluirJogo(Sala sala) {
        this.chatManager.deletarChat(sala);
        if (sala.getCategoria().equals("tictactoe")) {
            this.ticTacToeManager.deletarJogo(sala);
        } else if (sala.getCategoria().equals("jokenpo")) {
            this.jokenpoManager.deletarJogo(sala);
        } else if (sala.getCategoria().equals("whoistheimpostor")) {
            this.whoIsTheImpostorManager.deletarJogo(sala);
        }
    }

    public void adicionarJogador(String usernameParticipante, Sala sala) {
    }

    public void aposCriacaoDaSala(Sala sala) {
        if (sala.getCategoria().equals("whoistheimpostor")) {
            this.whoIsTheImpostorManager.criarSalaDeJogo(sala);
        }
        // if(sala.getCategoria().equals("coup")){
        // // this.coupManager.criarSalaDeJogo(sala);
        // }
    }
}
