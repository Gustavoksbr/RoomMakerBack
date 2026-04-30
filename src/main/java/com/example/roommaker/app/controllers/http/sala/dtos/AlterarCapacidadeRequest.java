package com.example.roommaker.app.controllers.http.sala.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlterarCapacidadeRequest {
    // null = sem limite (infinito)
    private Long qtdCapacidade;
}
