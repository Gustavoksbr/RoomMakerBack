package com.example.roommaker.app.services.categorias.examples.chat.controller.dto;

import com.example.roommaker.app.services.categorias.examples.chat.core.Chat;
import com.example.roommaker.app.services.categorias.examples.chat.core.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String usernameDono;
    private String nomeSala;
    private List<Message> messages;

    public ChatResponse(Chat chat) {
        this.usernameDono = chat.getUsernameDono();
        this.nomeSala = chat.getNome();
        this.messages = chat.getMessages();
    }
}
