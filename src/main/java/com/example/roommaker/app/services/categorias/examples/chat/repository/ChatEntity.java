package com.example.roommaker.app.services.categorias.examples.chat.repository;

import com.example.roommaker.app.services.categorias.examples.chat.core.Chat;
import com.example.roommaker.app.services.categorias.examples.chat.core.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chat")
public class ChatEntity {
    @Id
    private String id;
    @Field("username_dono")
    private String usernameDono;
    @Field("nome_sala")
    private String nomeSala;
    @Field("messages")
    private List<Message> messages;
    private Long contador=0L;

    public Chat toDomain() {
        Chat chat = Chat.builder()
                .usernameDono(usernameDono)
                .nome(nomeSala)
                .messages(messages)
                .build();
        return chat;
    }

    public ChatEntity(Chat chat) {
        this.usernameDono = chat.getUsernameDono();
        this.nomeSala = chat.getNome();
        this.messages = chat.getMessages();
    }
    public void adicionarMensagem(Message message) {
        ZoneId fusoHorarioBrasilia = ZoneId.of("America/Sao_Paulo");
        LocalDateTime localDateTimeBrasilia = LocalDateTime.now(fusoHorarioBrasilia);

        message.setTimestamp(localDateTimeBrasilia);
        this.contador++;
        message.setOrdem(contador);
        this.messages.add(message);
    }
}
