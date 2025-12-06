package com.example.roommaker.app.categorias.examples.jokenpo.controller;

import com.example.roommaker.app.categorias.examples.jokenpo.controller.dto.JokenpoResponse;
import com.example.roommaker.app.categorias.examples.jokenpo.domain.model.JokenpoLance;
import com.example.roommaker.app.categorias.examples.jokenpo.domain.model.JokenpoStatus;
import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;

import com.example.roommaker.app.controllers.websocket.sala.SalaSenderWebsocket;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.categorias.examples.jokenpo.controller.dto.JokenpoLanceRequest;
import com.example.roommaker.app.categorias.examples.jokenpo.domain.JokenpoManager;
import com.example.roommaker.app.categorias.examples.jokenpo.domain.model.Jokenpo;
import com.example.roommaker.app.categorias.examples.jokenpo.domain.model.JokenpoSala;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class JokenpoController {
    private final SalaSenderWebsocket salaSenderWebsocket;
    private final SalaManager salaManager;
    private final JokenpoManager jokenpoManager;
    @Autowired
    public JokenpoController(SalaManager salaManager, JokenpoManager jokenpoManager, SalaSenderWebsocket salaSenderWebsocket) { //, TicTacToeManager ticTacToeManager, SalaSenderWebsocket salaSenderWebsocket, SimpMessagingTemplate simpMessagingTemplate
        this.salaSenderWebsocket = salaSenderWebsocket;
        this.salaManager = salaManager;
        this.jokenpoManager = jokenpoManager;
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/jokenpo")
    public void retornarJokenpoSala(@DestinationVariable String usernameDono, @DestinationVariable String nomeSala, @DestinationVariable String username) {
        Sala salad = this.salaManager.verificarSeUsuarioEstaNaSalaERetornarSala( nomeSala, usernameDono,username);
        JokenpoSala jokenpo = this.jokenpoManager.retornarJokenpoSala(nomeSala, usernameDono);
        List<String> ouvintes = new ArrayList<>(salad.getUsernameParticipantes()) ;
        ouvintes.add(username);
        this.salaSenderWebsocket.enviarMensagemParaSala(usernameDono, nomeSala, "jokenpo", ouvintes, jokenpo);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/jokenpo/lance")
    public void lance(@Payload JokenpoLanceRequest request, @DestinationVariable String usernameDono, @DestinationVariable String nomeSala, @DestinationVariable String username) {
        Sala salad = this.salaManager.verificarSeUsuarioEstaNaSalaERetornarSala( nomeSala, usernameDono,username);
        if(!salad.getCategoria().equals("jokenpo")){
            throw new UsuarioNaoAutorizado("Esta sala não é de jokenpo");
        }
        List<String> ouvintes = salad.getUsernameParticipantes().isEmpty() ? new ArrayList<>() : salad.getUsernameParticipantes();
        ouvintes.add(usernameDono);

        Jokenpo jokenpo = this.jokenpoManager.lance(request.getLance(),username, salad);

        JokenpoResponse response= new JokenpoResponse(jokenpo);
        if(jokenpo.getStatus().equals(JokenpoStatus.WAITING)){
            if(jokenpo.getLanceOponente() != JokenpoLance.ESPERANDO) {
                response.setLanceOponente(JokenpoLance.SEGREDO);
            }
            if(jokenpo.getLanceDono() != JokenpoLance.ESPERANDO) {
                response.setLanceDono(JokenpoLance.SEGREDO);
            }
        }
        this.salaSenderWebsocket.enviarMensagemParaSala(usernameDono, nomeSala, "jokenpo", ouvintes, response);
    }
}
