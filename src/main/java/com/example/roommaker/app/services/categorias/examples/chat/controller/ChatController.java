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
//    @MessageMapping("/sala/{usernameDono}/{salaNome}/{username}/chat")
//    @SendTo("/topic/greetings")
//    public MessageResponseWs greetingTest1(String message, @DestinationVariable String username){
//        System.out.println("variavel username: " + username);
//        return new MessageResponseWs(message);
//    }

//    @MessageMapping("/sala/{usernameDono}/{salaNome}/{username}/chat")
//    public void greetingTest2(@Payload MessageRequestWs message, @DestinationVariable String usernameDono, @DestinationVariable String salaNome, @DestinationVariable String username){
//        System.out.println("variavel username: " + username);
//        System.out.println("mensagem: " + message.getMessage());
//        List<String> ouvintes = List.of("a", "b", "c", "d");
//        this.salaSenderWebsocket.enviarMensagemParaSala("a", "b", "c", ouvintes, new MessageResponse(message.getMessage()," o mano c"));
//    }
@MessageMapping("/sala/{usernameDono}/{salaNome}/{username}/chat")
public void chat(@DestinationVariable String usernameDono, @DestinationVariable String salaNome, @DestinationVariable String username) {
    Sala salad = this.salaManager.verificarSeUsuarioEstaNaSalaERetornarSala( salaNome, usernameDono,username);
    Chat chat = this.chatManager.devolverChat(salad);
//    List<String> ouvintes = new ArrayList<>(salad.getUsernameParticipantes());
//    ouvintes.add(salad.getUsernameDono());
    List<String> ouvintes = new ArrayList<>();
    ouvintes.add(username);
    this.salaSenderWebsocket.enviarMensagemParaSala(salad.getUsernameDono(), salad.getNome(),"chat",ouvintes, chat);
}

    @MessageMapping("/sala/{usernameDono}/{salaNome}/{username}/chat/message")
    public void setMessage(@Payload MessageRequestWs message, @DestinationVariable String usernameDono, @DestinationVariable String salaNome, @DestinationVariable String username) { //,@Header("simpSessionAttributes") Map<String, Object> sessionAttributes
//        if (!username.equals(Contexto.getUsername())) {
//            System.out.println("Contexto username:"+Contexto.getUsername());
//            throw new ErroDeRequisicaoGeral("Usuário não autorizado");
//        }
//        Sala sala = Contexto.getSala();
//        if (sala.getNome().equals(salaNome)) {
//            System.out.println("Nome da sala: " + sala.getNome());
//        }
//        if (sala.getUsernameDono().equals(usernameDono)) {
//            System.out.println("Dono da sala: " + sala.getUsernameDono());
//        }
       // System.out.println("printou:"+sessionAttributes.get("myEntity"));

        if(message.getMessage().isEmpty() || message.getMessage().isBlank()){
            throw new ErroDeRequisicaoGeral("Mensagem não pode ser vazia");
        }
//        if(message.getMessage().equals("abc")){
//            throw new ErroDeRequisicaoGeral("Mensagem não pode ser abc");
//        }
//        System.out.println("Contexto username:"+Contexto.getUsername());
//        System.out.println("usernameDono: " + usernameDono+ " salaNome: " + salaNome + " username: " + username);
        Sala salad = this.salaManager.verificarSeUsuarioEstaNaSalaERetornarSala( salaNome, usernameDono,username);

        Message messageDomain = message.toDomain();
        messageDomain.setFrom(username);
        this.chatManager.enviarMensagem(messageDomain, salad);


//        System.out.println("chegou ate aqui");
         // Sala verificarSeUsuarioEstaNaSalaERetornarSala(String nomeSala, String usernameDono, String usernameParticipante);
        List<String> ouvintes = new ArrayList<>(salad.getUsernameParticipantes());
        ouvintes.add(salad.getUsernameDono());
        salaSenderWebsocket.enviarMensagemParaSala(salad.getUsernameDono(), salad.getNome(),"chat",ouvintes, messageDomain);
    }

//    @MessageMapping("/testes/teste1")
//    @SendTo("/topic/listener1")
//    public MessageResponse greetingTest1(@Payload MessageRequestWs message){
//        System.out.println("1 variavel username: " + message.getMessage());
//        return new MessageResponse(message.getMessage(), " 1blableblibloblu");
//    }
//    @MessageMapping("/testes/teste2")
//    @SendTo("/topic/listener2")
//    public MessageResponse greetingTest2(@Payload MessageRequestWs message){
//        System.out.println("2 variavel username: " + message.getMessage());
//        return new MessageResponse(message.getMessage(), " 2blableblibloblu");
//    }
}
