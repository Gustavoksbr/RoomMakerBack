package com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models;

import lombok.*;

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

    //partida atual
    private Boolean estaNaPartida;
    private Boolean isImpostor;
    private Card carta;

}
