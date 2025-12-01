package com.example.roommaker.app.services.categorias.examples.chat.controller;

import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.controllers.websocket.sala.SalaSenderWebsocket;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.services.categorias.examples.chat.controller.dto.MessageRequestWs;
import com.example.roommaker.app.services.categorias.examples.chat.core.Chat;
import com.example.roommaker.app.services.categorias.examples.chat.core.ChatManager;
import com.example.roommaker.app.services.categorias.examples.chat.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ChatController {
    private final SalaSenderWebsocket salaSenderWebsocket;
    private final SalaManager salaManager;
    private final ChatManager chatManager;
    @Autowired
   public ChatController(SalaSenderWebsocket salaSenderWebsocket, SalaManager salaManager, ChatManager chatManager) {
        this.salaSenderWebsocket = salaSenderWebsocket;
        this.salaManager = salaManager;
        this.chatManager = chatManager;
    }

@MessageMapping("/sala/{usernameDono}/{salaNome}/{username}/chat")
public void chat(@DestinationVariable String usernameDono, @DestinationVariable String salaNome, @DestinationVariable String username) {
    Sala salad = this.salaManager.verificarSeUsuarioEstaNaSalaERetornarSala( salaNome, usernameDono,username);
    Chat chat = this.chatManager.devolverChat(salad);

    List<String> ouvintes = new ArrayList<>();
    ouvintes.add(username);
    this.salaSenderWebsocket.enviarMensagemParaSala(salad.getUsernameDono(), salad.getNome(),"chat",ouvintes, chat);
}

    @MessageMapping("/sala/{usernameDono}/{salaNome}/{username}/chat/message")
    public void setMessage(@Payload MessageRequestWs message, @DestinationVariable String usernameDono, @DestinationVariable String salaNome, @DestinationVariable String username) { //,@Header("simpSessionAttributes") Map<String, Object> sessionAttributes


        if(message.getMessage().isEmpty() || message.getMessage().isBlank()){
            throw new ErroDeRequisicaoGeral("Mensagem não pode ser vazia");
        }

        Sala salad = this.salaManager.verificarSeUsuarioEstaNaSalaERetornarSala( salaNome, usernameDono,username);

        Message messageDomain = message.toDomain();
        messageDomain.setFrom(username);
        this.chatManager.enviarMensagem(messageDomain, salad);

        List<String> ouvintes = new ArrayList<>(salad.getUsernameParticipantes());
        ouvintes.add(salad.getUsernameDono());
        salaSenderWebsocket.enviarMensagemParaSala(salad.getUsernameDono(), salad.getNome(),"chat",ouvintes, messageDomain);
    }

}
