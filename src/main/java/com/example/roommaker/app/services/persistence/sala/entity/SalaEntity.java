package com.example.roommaker.app.services.persistence.sala.entity;

import com.example.roommaker.app.domain.models.Sala;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "salas")
@AllArgsConstructor
@Builder
@CompoundIndexes({
        @CompoundIndex(name = "unique_sala_user", def = "{'username_dono': 1, 'nome': 1}", unique = true)
})
public class SalaEntity {
    @Id
    private String id;

    @Field("username_dono")
    private String usernameDono;

    @Field("nome")
    private String nome;

    @Field("categoria")
    private String categoria;

    @Field("senha")
    private String senha;

    @Field("qtd_capacidade")
    private Long qtdCapacidade;

    @Field("disponivel")
    private Boolean disponivel;

    @Field("username_participantes")
    private List<String> usernameParticipantes;

    public SalaEntity(Sala sala) {;
        this.usernameDono = sala.getUsernameDono();
        this.nome = sala.getNome();
        this.categoria = sala.getCategoria();
        this.senha = sala.getSenha();
        this.qtdCapacidade = sala.getQtdCapacidade();
        this.disponivel = true;
        this.usernameParticipantes = new ArrayList<>();
    }

    public SalaEntity() {
    }
    public void addParticipante(String username){
        this.usernameParticipantes.add(username);
    }
    public void removeParticipante(String username){
        this.usernameParticipantes.remove(username);
    }

    public Sala toSala() {
        return new Sala(this.id, this.usernameDono, this.nome, this.categoria, this.senha, this.qtdCapacidade, this.disponivel,this.usernameParticipantes);
    }
}