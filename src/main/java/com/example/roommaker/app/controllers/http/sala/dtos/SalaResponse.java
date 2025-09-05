package com.example.roommaker.app.controllers.http.sala.dtos;

import com.example.roommaker.app.domain.models.Sala;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class SalaResponse {
    private String id;
    private String usernameDono;
    private String nome;
    private String categoria;
    private Long qtdCapacidade;
    private Boolean disponivel;
    private Boolean publica;
    private List<String> usernameParticipantes;

    public SalaResponse(Sala sala){
        this.id = sala.getId();
        this.usernameDono = sala.getUsernameDono();
        this.nome = sala.getNome();
        this.categoria = sala.getCategoria();
        this.qtdCapacidade = sala.getQtdCapacidade();
        this.disponivel = sala.getDisponivel();
        this.publica = (sala.getSenha() == null || sala.getSenha().isEmpty());
        this.usernameParticipantes = sala.getUsernameParticipantes();
    }
    // diferencas com o domain:
    // -nao tem senha
}
