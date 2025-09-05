package com.example.roommaker.app.domain.exceptions;

public class UsuarioNaoAutorizado extends RuntimeException {
    public UsuarioNaoAutorizado(String message) {
        super("Usuário não autorizado:"+message);
    }
}
