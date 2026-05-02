package com.example.roommaker.app.categorias.examples.xadrez.sender;

import com.example.roommaker.app.categorias.examples.xadrez.domain.model.XadrezResponse;
import com.example.roommaker.app.controllers.websocket.sala.SalaSenderWebsocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class XadrezSender {

    private final SalaSenderWebsocket salaSenderWebsocket;

    @Autowired
    public XadrezSender(SalaSenderWebsocket salaSenderWebsocket) {
        this.salaSenderWebsocket = salaSenderWebsocket;
    }

    public void enviarParaUsuario(String usernameDono, String nomeSala, String usuario, XadrezResponse response) {
        salaSenderWebsocket.enviarMensagemParaSala(
                usernameDono, nomeSala, "xadrez", List.of(usuario), response);
    }

    public void enviarParaTodos(String usernameDono, String nomeSala, List<String> usuarios, XadrezResponse response) {
        salaSenderWebsocket.enviarMensagemParaSala(
                usernameDono, nomeSala, "xadrez", usuarios, response);
    }
}
