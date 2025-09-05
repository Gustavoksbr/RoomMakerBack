package com.example.roommaker.app.controllers.websocket.filters;


import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.domain.exceptions.ErroDeAutenticacaoGeral;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;
import com.example.roommaker.app.domain.thread.Contexto;
import com.example.roommaker.app.controllers.websocket.sala.SalaSenderWebsocket;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.domain.managers.usuario.UsuarioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class JwtWebsocketInterceptor implements ChannelInterceptor {
    private final UsuarioManager usuarioManager;
    private final SalaManager salaManager;
    private final SalaSenderWebsocket salaSenderWebsocket;


    @Autowired
    public JwtWebsocketInterceptor(UsuarioManager usuarioManager, SalaManager salaManager, @Lazy SalaSenderWebsocket salaSenderWebsocket) {
        this.usuarioManager = usuarioManager;
        this.salaManager = salaManager;
        this.salaSenderWebsocket = salaSenderWebsocket;
    }

    /**************************/

    @Override
public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    assert accessor != null;
//     accessor.getSessionAttributes().put("myEntity", "entity");
//        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.UNSUBSCRIBE.equals(accessor.getCommand()) || StompCommand.CONNECT.equals(accessor.getCommand()) || StompCommand.DISCONNECT.equals(accessor.getCommand()))
//        if (EnumSet.of(StompCommand.SUBSCRIBE, StompCommand.SEND, StompCommand.UNSUBSCRIBE, StompCommand.CONNECT, StompCommand.DISCONNECT).contains(accessor.getCommand())) {
        if (EnumSet.of(StompCommand.SUBSCRIBE, StompCommand.SEND).contains(accessor.getCommand())) {
        String authToken = accessor.getFirstNativeHeader("Authorization");
       // System.out.println("Token: " + authToken);
        if (authToken != null && authToken.startsWith("Bearer ")) {
            String jwtToken = authToken.substring(7);
            String username = usuarioManager.capturarUsernameDoToken(jwtToken);
            Contexto.setUsername(username);
            //System.out.println("Filtro descobriu que esse usuário é: " + username);
        } else {
            throw new ErroDeAutenticacaoGeral("Usuário não autenticado");
        }
        String destination = accessor.getDestination();
        System.out.println("Destination with"+ accessor.getCommand()+": " + destination);
        if (destination != null) {
            if (EnumSet.of(StompCommand.SUBSCRIBE,StompCommand.UNSUBSCRIBE).contains(accessor.getCommand()) && (destination.matches("/topic/sala/[^/]+/[^/]+/[^/]+/[^/]+") )) {
                processDestination(destination, accessor);
            }
//            else if (StompCommand.SEND.equals(accessor.getCommand()) && (destination.matches("/app/sala/[^/]+/[^/]+/[^/]+/[^/]+"))) {
//                processDestination(destination);
//            }
        }
    }
        if(accessor.getCommand()==StompCommand.CONNECT){
            System.out.println("Conexão estabelecida");
        }
        if(accessor.getCommand()==StompCommand.DISCONNECT){
            System.out.println("Conexão encerrada");
        }
   // return message;
    return   MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
}

private void processDestination(String destination, StompHeaderAccessor accessor) {

    String[] parts = destination.split("/");
    String usernameDono = parts[3];
    String nomeSala = parts[4];
    String username = parts[5];
    String tipo = parts[6];
    if (!Contexto.getUsername().matches(username)) {
        throw new UsuarioNaoAutorizado("Usuário decodificado do token não é o mesmo da url de destino");
    }
    Sala sala = this.salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(nomeSala, usernameDono, Contexto.getUsername());
    Contexto.setSala(sala);
//   if (accessor.getCommand()==StompCommand.SUBSCRIBE){
//       System.out.println(username + " fez SUBSCRIBE na sala: " + nomeSala + " do dono: " + usernameDono);
//       record UsuarioOnline(String username, Boolean online){}
//       UsuarioOnline usuarioOnline = new UsuarioOnline(username, true);
//       this.salaSenderWebsocket.enviarMensagemParaSala(usernameDono, nomeSala, null, sala.getUsernameParticipantes(), usuarioOnline);
//       if(tipo==null){
//
//       }
//   }
//    else if (accessor.getCommand()==StompCommand.UNSUBSCRIBE){
//         System.out.println(username + " fez UNSUBSCRIBE da sala: " + nomeSala + " do dono: " + usernameDono);
//        record UsuarioOffline(String username, Boolean online){}
//        UsuarioOffline usuarioOffline = new UsuarioOffline(username, false);
//        this.salaSenderWebsocket.enviarMensagemParaSala(usernameDono, nomeSala, null, sala.getUsernameParticipantes(), usuarioOffline);
//    }
}
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        Contexto.clear();
    }
}