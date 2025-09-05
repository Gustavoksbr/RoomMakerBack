package com.example.roommaker.app.services.categorias.examples.tictactoe.controller.dto;

import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToe;
import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicTacToeLanceResponseWs {
    private String x;
    private String o;
    private String posicao;
    private TicTacToeStatus status;
    public TicTacToe toDomain() {
        return TicTacToe.builder().x(x).o(o).posicao(posicao).status(status).build();
    }
    public TicTacToeLanceResponseWs(TicTacToe ticTacToe) {
        this.x = ticTacToe.getX();
        this.o = ticTacToe.getO();
        this.posicao = ticTacToe.getPosicao();
        this.status = ticTacToe.getStatus();
    }
}
