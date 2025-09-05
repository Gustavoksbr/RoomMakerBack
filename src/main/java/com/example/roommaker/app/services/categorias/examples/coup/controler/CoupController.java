package com.example.roommaker.app.services.categorias.examples.coup.controler;

import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;
import com.example.roommaker.app.controllers.websocket.sala.SalaSenderWebsocket;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.services.categorias.examples.coup.controler.response.CoupResponse;
import com.example.roommaker.app.services.categorias.examples.coup.domain.CoupManager;
import com.example.roommaker.app.services.categorias.examples.coup.domain.model.Coup;
import com.example.roommaker.app.services.categorias.examples.coup.domain.model.JogadorCoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CoupController {
    private final SalaSenderWebsocket salaSenderWebsocket;
    private final SalaManager salaManager;
    private final CoupManager coupManager;
    @Autowired
    public CoupController(SalaSenderWebsocket salaSenderWebsocket, SalaManager salaManager, CoupManager coupManager) {
        this.salaSenderWebsocket = salaSenderWebsocket;
        this.salaManager = salaManager;
        this.coupManager = coupManager;
    }
    @MessageMapping("/sala/{usernameDono}/{salaNome}/{username}/coup/iniciar")
    public void iniciarJogo(@DestinationVariable String usernameDono,@DestinationVariable String salaNome,@DestinationVariable String username) {
        Sala sala = this.salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(salaNome, usernameDono, username);
        if(!sala.getUsernameDono().equals(username)){
            throw new UsuarioNaoAutorizado("Apenas o dono da sala pode iniciar o jogo");
        }
       Coup coup = this.coupManager.iniciarJogo(sala);
        for (JogadorCoup jogador : coup.getJogadores()){
            String usernameParaEnviar = jogador.getUsername();
            List<String> ouvintes = List.of(usernameParaEnviar);
            CoupResponse coupResponse = CoupResponse.builder()
                    .id(coup.getId())
                    .usernameJogadoresVivos(coup.getUsernameJogadoresVivos())
                    .usernameJogadoresMortos(coup.getUsernameJogadoresMortos())
                    .turnos(coup.getTurnos())
                    .turnoAtual(coup.getTurnoAtual())
                    .jogador(jogador)
                    .build();
            this.salaSenderWebsocket.enviarMensagemParaSala(sala.getUsernameDono(), sala.getNome(), "coup", ouvintes , coupResponse);
        }
    }
}