package com.example.roommaker.app.services.categorias.examples.tictactoe.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicTacToeComecarRequest {
    private String jogador1;
    public String jogador2;
}
