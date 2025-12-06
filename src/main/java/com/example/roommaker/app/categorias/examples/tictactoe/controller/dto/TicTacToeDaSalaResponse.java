package com.example.roommaker.app.categorias.examples.tictactoe.controller.dto;


import com.example.roommaker.app.categorias.examples.tictactoe.domain.models.TicTacToe;
import com.example.roommaker.app.categorias.examples.tictactoe.domain.models.TicTacToeSala;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicTacToeDaSalaResponse {
    private String nomeSala;
    private String usernameDono;
    private String usernameOponente;
    private TicTacToe jogoAtual;
    private List<TicTacToe> historico;
    private Integer tamanhoHistorico;

    public  TicTacToeDaSalaResponse(TicTacToeSala ticTacToeSala) {
        this.nomeSala = ticTacToeSala.getNomeSala();
        this.usernameDono = ticTacToeSala.getUsernameDono();
        this.usernameOponente = ticTacToeSala.getUsernameOponente();
        this.jogoAtual = ticTacToeSala.getJogoAtual();
        this.historico = ticTacToeSala.getHistorico();
        this.tamanhoHistorico = ticTacToeSala.getTamanhoHistorico();
    }
}
