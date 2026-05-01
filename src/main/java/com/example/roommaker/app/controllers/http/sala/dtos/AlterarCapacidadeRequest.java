package com.example.roommaker.app.controllers.http.sala.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlterarCapacidadeRequest {
    // null = sem limite (infinito), max = 99999
    @Max(value = 99999, message = "Capacidade máxima permitida é 99999")
    @Min(value = 2, message = "Capacidade mínima é 2")
    private Long qtdCapacidade;
}
