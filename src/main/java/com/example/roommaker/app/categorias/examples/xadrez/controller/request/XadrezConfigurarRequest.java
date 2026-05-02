package com.example.roommaker.app.categorias.examples.xadrez.controller.request;

import com.example.roommaker.app.categorias.examples.xadrez.domain.model.NotacaoXadrez;
import lombok.Data;

@Data
public class XadrezConfigurarRequest {
    private String usernameBrancas;
    private String usernamePretas;
    private NotacaoXadrez notacao; // opcional
}
