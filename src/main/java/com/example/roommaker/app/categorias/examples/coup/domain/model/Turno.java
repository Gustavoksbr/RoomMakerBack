package com.example.roommaker.app.categorias.examples.coup.domain.model;

import com.example.roommaker.app.categorias.examples.coup.domain.model.main.Acao;
import com.example.roommaker.app.categorias.examples.coup.domain.model.main.Personagem;
import com.example.roommaker.app.categorias.examples.coup.domain.model.main.StatusCoup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class Turno {
    private Long ordem;
    private JogadorCoup jogador;
    private Acao acao;

    private JogadorCoup alvo; // assassino, golpe de estado ou extorquir
   private List<Personagem> influenciasTrocadas; //embaixador

    private JogadorCoup duvidadoPor;

    private JogadorCoup bloqueadoPor;
    private JogadorCoup bloqueioDuvidadoPor;

    private JogadorCoup vencedor;
    private JogadorCoup perdedor;

    private StatusCoup status;

    private List<String> skip;
}