package com.example.roommaker.app.controllers.websocket.sala;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class SalaSenderWebsocket {
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    public SalaSenderWebsocket(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    public void enviarMensagemParaSala(String usernameDono, String salaNome,  String tipo,List<String> ouvintes, Object mensagem) {
        for (String ouvinte : ouvintes) {
            String destino = "/topic/sala/" + usernameDono + "/" + salaNome + "/" + ouvinte + "/" + tipo;
            if(mensagem!=null){
                this.messagingTemplate.convertAndSend(destino, mensagem);
            }
        }
    }
}
