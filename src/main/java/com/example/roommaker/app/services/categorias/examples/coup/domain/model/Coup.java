package com.example.roommaker.app.services.categorias.examples.coup.domain.model;

import com.example.roommaker.app.services.categorias.examples.coup.domain.model.main.Personagem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Stack;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Coup {
    private Long id;
    private List<String> usernameJogadoresVivos;
    private List<String> usernameJogadoresMortos;
    private List<JogadorCoup> jogadores;
    private List<Turno> turnos;
    private Turno turnoAtual;
//    private StatusCoup status;
    private Stack<Personagem> baralho;
}
