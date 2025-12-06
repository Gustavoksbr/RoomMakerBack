package com.example.roommaker.app.categorias.examples.coup.domain.model;

import com.example.roommaker.app.categorias.examples.coup.domain.model.main.Personagem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JogadorCoup {
    private Long Ordem;
    private String username;
    private Integer moedas;
    private List<Personagem> influencias;
    private Boolean vivo;


}
