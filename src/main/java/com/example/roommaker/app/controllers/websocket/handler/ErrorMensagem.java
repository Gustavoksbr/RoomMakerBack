package com.example.roommaker.app.controllers.websocket.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMensagem {
    private String error;
    private String status;
}
