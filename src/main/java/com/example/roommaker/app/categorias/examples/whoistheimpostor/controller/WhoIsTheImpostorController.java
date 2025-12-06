package com.example.roommaker.app.categorias.examples.whoistheimpostor.controller;

import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.WhoIsTheImpostorManager;
import com.example.roommaker.app.domain.managers.sala.SalaManager;


import com.example.roommaker.app.domain.models.Sala;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

// obs: TODOS os controladores websocket devem comecar com @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/...")
@Controller
public class WhoIsTheImpostorController {
    private final SalaManager salaManager;
    private final WhoIsTheImpostorManager whoIsTheImpostorManager;

    @Autowired
    public WhoIsTheImpostorController(
            SalaManager salaManager,
            WhoIsTheImpostorManager whoIsTheImpostorManager
    ) {
        this.salaManager = salaManager;
        this.whoIsTheImpostorManager = whoIsTheImpostorManager;
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/whoistheimpostor/comecar")
    public void comecar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username
    ) {
        Sala sala = salaManager.mostrarSala(nomeSala, usernameDono);
        whoIsTheImpostorManager.comecarPartida(sala, username);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/whoistheimpostor/terminar")
    public void terminar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username
    ) {
        Sala sala = salaManager.mostrarSala(nomeSala, usernameDono);
        whoIsTheImpostorManager.terminarPartida(sala, username);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/whoistheimpostor/mostrar")
    public void mostrar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username
    ) {
        Sala sala = salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(
                nomeSala, usernameDono, username
        );
        whoIsTheImpostorManager.mostrarJogoAtual(sala, username);
    }
}
