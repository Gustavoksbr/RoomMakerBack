package com.example.roommaker.app.services.categorias.examples.chat.core;

import com.example.roommaker.app.domain.models.Sala;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder


public class Chat extends Sala {
    private String usernameDono;
    private String nome;
    private List<Message> messages;

    public void adicionarMensagem(Message message) {
        this.messages.add(message);
    }
}
