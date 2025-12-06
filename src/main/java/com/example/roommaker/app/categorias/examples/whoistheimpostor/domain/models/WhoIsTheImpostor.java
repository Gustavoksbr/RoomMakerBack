package com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WhoIsTheImpostor {
    private String nomeSala;
    private String usernameDono;
    private Boolean jogando;
    private List<String> jogadores;
    private String impostor;
    private Card carta;
}
