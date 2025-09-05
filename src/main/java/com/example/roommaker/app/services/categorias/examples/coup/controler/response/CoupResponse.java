package com.example.roommaker.app.services.categorias.examples.coup.controler.response;

import com.example.roommaker.app.services.categorias.examples.coup.domain.model.JogadorCoup;
import com.example.roommaker.app.services.categorias.examples.coup.domain.model.Turno;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoupResponse {
    private Long id;
    private List<String> usernameJogadoresVivos;
    private List<String> usernameJogadoresMortos;
    private List<Turno> turnos;
    private Turno turnoAtual;
    private JogadorCoup jogador;
}
