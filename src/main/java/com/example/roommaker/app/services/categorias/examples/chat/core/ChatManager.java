package com.example.roommaker.app.services.categorias.examples.chat.core;

import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.services.categorias.examples.chat.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class ChatManager {
    private final ChatRepository chatRepository;
    @Autowired
    public ChatManager(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }
    public Chat devolverChat(Sala sala){
        Chat chat = this.chatRepository.findByUsernameDonoAndNomeSala(sala.getUsernameDono(), sala.getNome());
        if(chat == null){

            chat = Chat.builder()
                    .usernameDono(sala.getUsernameDono())
                    .nome(sala.getNome())
                    .messages(new ArrayList<>())
                    .build();

            this.chatRepository.criar(chat);
        }
        return chat;
    }

    public Message enviarMensagem(Message mensagem, Sala sala){
        mensagem.setTimestamp(LocalDateTime.now());
        this.chatRepository.adicionarMensagem(sala.getUsernameDono(), sala.getNome(), mensagem);
        return mensagem;
    }
    public void deletarChat(Sala sala){
        this.chatRepository.deleteByUsernameDonoAndNomeSala(sala.getUsernameDono(), sala.getNome());
    }
}
