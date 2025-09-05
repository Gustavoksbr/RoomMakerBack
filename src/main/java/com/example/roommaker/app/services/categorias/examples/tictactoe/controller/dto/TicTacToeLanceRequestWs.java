package com.example.roommaker.app.services.categorias.examples.tictactoe.controller.dto;

import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToeLance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicTacToeLanceRequestWs {
    private Integer lance;
    public TicTacToeLanceRequestWs (TicTacToeLance ticTacToeLance) {
        this.lance = ticTacToeLance.getLance();
    }
    public TicTacToeLance toDomain() {
        return TicTacToeLance.builder().lance(lance).build();
    }
}