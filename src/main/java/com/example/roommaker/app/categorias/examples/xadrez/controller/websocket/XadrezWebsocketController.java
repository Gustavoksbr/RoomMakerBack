package com.example.roommaker.app.categorias.examples.xadrez.controller.websocket;

import com.example.roommaker.app.categorias.examples.xadrez.controller.request.*;
import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezManager;
import com.example.roommaker.app.domain.thread.Contexto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

// Todos os endpoints seguem o padrão: /sala/{usernameDono}/{nomeSala}/{username}/xadrez/...
@Controller
public class XadrezWebsocketController {

    private final XadrezManager xadrezManager;

    @Autowired
    public XadrezWebsocketController(XadrezManager xadrezManager) {
        this.xadrezManager = xadrezManager;
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/configurar")
    public void configurar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username,
            @Payload XadrezConfigurarRequest request) {
        Contexto.setUsername(username);
        xadrezManager.configurar(nomeSala, usernameDono, username,
                request.getUsernameBrancas(), request.getUsernamePretas(), request.getNotacao());
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/iniciar")
    public void iniciar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username) {
        Contexto.setUsername(username);
        xadrezManager.iniciarPartida(nomeSala, usernameDono, username);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/configurar-e-iniciar")
    public void configurarEIniciar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username,
            @Payload XadrezConfigurarRequest request) {
        Contexto.setUsername(username);
        xadrezManager.configurarEIniciar(nomeSala, usernameDono, username,
                request.getUsernameBrancas(), request.getUsernamePretas(), request.getNotacao(),
                request.calcularTempoTotalBrancas(), request.getIncrementoBrancasSegundos(),
                request.calcularTempoTotalPretas(), request.getIncrementoPretasSegundos());
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/lance")
    public void lance(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username,
            @Payload XadrezLanceRequest request) {
        Contexto.setUsername(username);
        xadrezManager.jogar(nomeSala, usernameDono, username, request.getSan());
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/desistir")
    public void desistir(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username) {
        Contexto.setUsername(username);
        xadrezManager.desistir(nomeSala, usernameDono, username);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/propor-empate")
    public void proporEmpate(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username) {
        Contexto.setUsername(username);
        xadrezManager.proporEmpate(nomeSala, usernameDono, username);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/responder-empate")
    public void responderEmpate(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username,
            @Payload XadrezResponderEmpateRequest request) {
        Contexto.setUsername(username);
        xadrezManager.responderEmpate(nomeSala, usernameDono, username, Boolean.TRUE.equals(request.getAceitar()));
    }
}
