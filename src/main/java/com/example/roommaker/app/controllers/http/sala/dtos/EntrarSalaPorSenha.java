package com.example.roommaker.app.controllers.http.sala.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EntrarSalaPorSenha {
    private String senha;
}
