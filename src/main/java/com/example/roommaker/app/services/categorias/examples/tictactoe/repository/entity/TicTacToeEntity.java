package com.example.roommaker.app.services.categorias.examples.tictactoe.repository.entity;

import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToeStatus;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "tictactoe")
public class TicTacToeEntity {
    @Field("numero")
    private Integer numero;

    @Field("x")
    private String x;

    @Field("o")
    private String o;

    @Field("posicao")
    private String posicao;

    @Field("status")
    private TicTacToeStatus status;
}