package com.example.roommaker.app.categorias.examples.tictactoe.domain.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicTacToe {
    private Integer numero;
    private String x;
    private String o;
    private String posicao;
    private TicTacToeStatus status;
}
