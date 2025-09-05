package com.example.roommaker.app.domain.exceptions;

public class SalaJaExiste extends RuntimeException {
    public SalaJaExiste(String nome) {
        super("Você já possui uma sala com o nome: '" + nome + ".");
    }
}