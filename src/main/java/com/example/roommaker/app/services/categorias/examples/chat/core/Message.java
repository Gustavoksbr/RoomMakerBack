package com.example.roommaker.app.services.categorias.examples.chat.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    private Long ordem;
    private String message;
    private String from;
    private Long to;
    private LocalDateTime timestamp;
}
