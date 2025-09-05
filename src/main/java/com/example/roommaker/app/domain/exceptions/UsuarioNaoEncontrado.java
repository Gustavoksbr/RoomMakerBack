package com.example.roommaker.app.domain.exceptions;

public class UsuarioNaoEncontrado  extends RuntimeException {
    public UsuarioNaoEncontrado(String username) {
        super("Usuário "+username+" não encontrado");
    }
}
