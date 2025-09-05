package com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models;

import com.example.roommaker.app.domain.models.Sala;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder

public class TicTacToeSala extends Sala {
    private String nomeSala;
    private String usernameDono;
    private String usernameOponente;
    private TicTacToe jogoAtual;
    private List<TicTacToe> historico;
    private Integer tamanhoHistorico;
//    private Integer vitoriasDono;
//    private Integer vitoriasOponente;
}
