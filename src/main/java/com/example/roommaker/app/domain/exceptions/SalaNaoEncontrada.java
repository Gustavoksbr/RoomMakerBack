package com.example.roommaker.app.domain.exceptions;

public class SalaNaoEncontrada extends RuntimeException {
    public SalaNaoEncontrada(String nome, String dono) {
        super("Sala com nome '" + nome + "' do usuario '"+dono+"'  não encontrada.");
    }
}
