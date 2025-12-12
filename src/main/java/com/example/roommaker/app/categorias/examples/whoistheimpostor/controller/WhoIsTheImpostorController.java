package com.example.roommaker.app.categorias.examples.whoistheimpostor.controller;

import com.example.roommaker.app.categorias.examples.whoistheimpostor.controller.requests.VotoRequest;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.WhoIsTheImpostorManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

// obs: TODOS os controladores websocket devem comecar com @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/...")
@Controller
public class WhoIsTheImpostorController {
    private final WhoIsTheImpostorManager whoIsTheImpostorManager;

    @Autowired
    public WhoIsTheImpostorController(
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
//        Sala sala = salaManager.mostrarSala(nomeSala, usernameDono);
        whoIsTheImpostorManager.comecarPartida(nomeSala, usernameDono , username);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/whoistheimpostor/terminar")
    public void terminar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username
    ) {
//        Sala sala = salaManager.mostrarSala(nomeSala, usernameDono);
        whoIsTheImpostorManager.terminarPartida(nomeSala, usernameDono, username);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/whoistheimpostor/mostrar")
    public void mostrar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username
    ) {
//        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(
//                nomeSala, usernameDono, username
//        );
        whoIsTheImpostorManager.mostrarJogoAtual(nomeSala, usernameDono, username);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/whoistheimpostor/votar")
    public void votar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username,
            @Payload VotoRequest votoRequest
    ) {
//        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(
//                nomeSala, usernameDono, username
//        );
        whoIsTheImpostorManager.votar(nomeSala, usernameDono, username, votoRequest.getVoto());
    }
    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/whoistheimpostor/cancelarVoto")
    public void cancelarVoto(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username
    ) {
//        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(
//                nomeSala, usernameDono, username
//        );
        whoIsTheImpostorManager.cancelarVoto(nomeSala, usernameDono, username);
    }
}
