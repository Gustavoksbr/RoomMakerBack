package com.example.roommaker.app.controllers.http.usuario.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EsqueciSenhaRequest {
    private String email;
}
