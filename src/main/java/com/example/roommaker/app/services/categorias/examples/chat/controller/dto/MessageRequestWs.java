package com.example.roommaker.app.services.categorias.examples.chat.controller.dto;

import com.example.roommaker.app.services.categorias.examples.chat.core.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestWs  {
//@NotBlank
    private String message;
    private Long to;
    public Message toDomain() {
        return Message.builder().message(message).to(to).build();
    }
}


//    @NotBlank