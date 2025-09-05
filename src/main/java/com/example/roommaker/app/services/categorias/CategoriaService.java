package com.example.roommaker.app.services.categorias;

import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.ports.repository.SalaRepository;
import com.example.roommaker.app.services.categorias.examples.chat.core.ChatManager;
import com.example.roommaker.app.services.categorias.examples.coup.domain.CoupManager;
import com.example.roommaker.app.services.categorias.examples.jokenpo.domain.JokenpoManager;
import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.TicTacToeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {
    private final SalaRepository salaRepository;
    private final TicTacToeManager ticTacToeManager;
    private final JokenpoManager jokenpoManager;
    private final ChatManager chatManager;
    private final CoupManager coupManager;

    @Autowired
    public CategoriaService(SalaRepository salaRepository, TicTacToeManager ticTacToeManager, JokenpoManager jokenpoManager, ChatManager chatManager, CoupManager coupManager) {

        this.salaRepository = salaRepository;
        this.ticTacToeManager = ticTacToeManager;
        this.jokenpoManager = jokenpoManager;
        this.chatManager = chatManager;
        this.coupManager = coupManager;
    }

    public void verificarSeUsuarioEstaNoJogo(String usernameParticipante, String usernameDono, String nomeSala) {
        Sala sala = this.salaRepository.verificarSeUsuarioEstaNaSalaERetornarSala(nomeSala, usernameDono, usernameParticipante);
    }

    public void notificarSaidaDeUsuario(String usernameParticipante, Sala sala) {
        if(sala.getCategoria().equals("tictactoe")){
            this.ticTacToeManager.saidaDeParticipante(usernameParticipante, sala);
        } else if (sala.getCategoria().equals("jokenpo")) {
            this.jokenpoManager.saidaDeParticipante(usernameParticipante, sala);
        }

    }

    public void validarSalaParaOJogo(Sala sala) {
        if (!List.of("tictactoe", "jokenpo", "chat").contains(sala.getCategoria())) {
            throw new ErroDeRequisicaoGeral("Categoria inválida");
        }

        if(sala.getCategoria().equals("tictactoe")){
            this.ticTacToeManager.validarSalaParaOJogo(sala);
        } else if (sala.getCategoria().equals("jokenpo")) {
            this.jokenpoManager.validarSalaParaOJogo(sala);
        }
        else if(sala.getQtdCapacidade()<2 || sala.getQtdCapacidade()>1000){
            throw new ErroDeRequisicaoGeral("Quantidade de participantes deve ser entre 2 a 1000");
        }
    }

    public void aposCriacaoDaSala(Sala sala) {
        if(sala.getCategoria().equals("coup")){
            // this.coupManager.criarSalaDeJogo(sala);
        }
    }

    public void excluirJogo(Sala sala) {
        this.chatManager.deletarChat(sala);
        if(sala.getCategoria().equals("tictactoe")){
            this.ticTacToeManager.deletarJogo(sala);
        } else if (sala.getCategoria().equals("jokenpo")) {
            this.jokenpoManager.deletarJogo(sala);
        }
    }

    public void adicionarJogador(String usernameParticipante, Sala sala) {
    }
}
