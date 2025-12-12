package com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WhoIsTheImpostorResponse {
    private Boolean partidaSendoJogada;

    //partida passada
    private String impostorDaPartidaPassada;
    private Card cartaDaPartidaPassada;
    private Map<String, Set<String>> votosPorVotadosDaPartidaPassada;
    private List<String> jogadoresDaPartidaPassada;

    //partida atual
    private Boolean estaNaPartida;
    private Boolean isImpostor;
    private Card carta;
    private List<String> jogadoresNaPartida;
    private Long quantidadeVotos;
    private String votado;
}
