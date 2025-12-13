package com.example.roommaker.app.categorias.examples.whoistheimpostor.controller.websocket;

import com.example.roommaker.app.categorias.examples.whoistheimpostor.controller.requests.VotoRequest;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.WhoIsTheImpostorManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

// obs: TODOS os controladores websocket devem comecar com @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/...")
@Controller
public class WhoIsTheImpostorWebsocketController {
    private final WhoIsTheImpostorManager whoIsTheImpostorManager;

    @Autowired
    public WhoIsTheImpostorWebsocketController(
            WhoIsTheImpostorManager whoIsTheImpostorManager
    ) {
        this.whoIsTheImpostorManager = whoIsTheImpostorManager;
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/whoistheimpostor/comecar")
    public void comecar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username
    ) {
        whoIsTheImpostorManager.comecarPartida(nomeSala, usernameDono , username);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/whoistheimpostor/terminar")
    public void terminar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username
    ) {
        whoIsTheImpostorManager.terminarPartida(nomeSala, usernameDono, username);
    }

//    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/whoistheimpostor/mostrar")
//    public void mostrar(
//            @DestinationVariable String usernameDono,
//            @DestinationVariable String nomeSala,
//            @DestinationVariable String username
//    ) {
//        whoIsTheImpostorManager.mostrarJogoAtual(nomeSala, usernameDono, username);
//    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/whoistheimpostor/votar")
    public void votar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username,
            @Payload VotoRequest votoRequest
    ) {
        whoIsTheImpostorManager.votar(nomeSala, usernameDono, username, votoRequest.getVoto());
    }
    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/whoistheimpostor/cancelarVoto")
    public void cancelarVoto(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username
    ) {
        whoIsTheImpostorManager.cancelarVoto(nomeSala, usernameDono, username);
    }
}
