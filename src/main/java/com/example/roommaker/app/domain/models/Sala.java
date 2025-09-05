package com.example.roommaker.app.domain.models;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder

public class Sala {
    private String id;
    private String usernameDono;
    private String nome;
    private String categoria;
    private String senha;
    private Long qtdCapacidade;
    private Boolean disponivel;
    private List<String> usernameParticipantes;


    public Sala(String donoDaSala, String nomeSala) {
        this.usernameDono = donoDaSala;
        this.nome = nomeSala;
    }
}
