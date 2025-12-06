package com.example.roommaker.app.categorias.examples.jokenpo.repository.entity;

import com.example.roommaker.app.categorias.examples.jokenpo.domain.model.Jokenpo;
import com.example.roommaker.app.categorias.examples.jokenpo.domain.model.JokenpoSala;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Document(collection = "jokenpo_da_sala")
public class JokenpoDaSalaEntity {
    @Id
    private String id;
    @Field("nome_sala")
    private String nomeSala;
    @Field("username_dono")
    private String usernameDono;
    @Field("username_oponente")
    private String usernameOponente;
    @Field("jogo_atual")
    private Jokenpo jogoAtual;
    @Field("historico")
    private List<Jokenpo> historico;
    public JokenpoDaSalaEntity(JokenpoSala jokenpoDaSala){
        this.nomeSala = jokenpoDaSala.getNomeSala();
        this.usernameDono = jokenpoDaSala.getUsernameDono();
        this.usernameOponente = jokenpoDaSala.getUsernameOponente();
        this.jogoAtual = jokenpoDaSala.getJogoAtual();
        this.historico = jokenpoDaSala.getHistorico();
    }
    public JokenpoSala toDomain(){
        return JokenpoSala.builder()
                .nomeSala(this.nomeSala)
                .usernameDono(this.usernameDono)
                .usernameOponente(this.usernameOponente)
                .jogoAtual(this.jogoAtual)
                .historico(this.historico)
                .build();
    }
    public void atualizar(JokenpoSala jokenpoDaSala){
        this.nomeSala = jokenpoDaSala.getNomeSala();
        this.usernameDono = jokenpoDaSala.getUsernameDono();
        this.usernameOponente = jokenpoDaSala.getUsernameOponente();
        this.jogoAtual = jokenpoDaSala.getJogoAtual();
        this.historico = jokenpoDaSala.getHistorico();
    }
}
