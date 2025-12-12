package com.example.roommaker.app.categorias.examples.whoistheimpostor.sender;

import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostorResponse;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostor;
import com.example.roommaker.app.controllers.websocket.sala.SalaSenderWebsocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WhoIsTheImpostorSender {

    private final SalaSenderWebsocket salaSenderWebsocket;

    @Autowired
    public WhoIsTheImpostorSender(SalaSenderWebsocket salaSenderWebsocket) {
        this.salaSenderWebsocket = salaSenderWebsocket;
    }

    public void enviarParaUsuario(
            String usernameDono,
            String nomeSala,
            String usuario,
            WhoIsTheImpostorResponse response
    ) {
        salaSenderWebsocket.enviarMensagemParaSala(
                usernameDono,
                nomeSala,
                "whoistheimpostor",
                List.of(usuario),
                response
        );
    }
    public void enviarParaTodos(
            String usernameDono,
            String nomeSala,
            List<String> usuarios,
            WhoIsTheImpostorResponse response
    ) {
        salaSenderWebsocket.enviarMensagemParaSala(
                usernameDono,
                nomeSala,
                "whoistheimpostor",
                usuarios,
                response
        );
    }
}
