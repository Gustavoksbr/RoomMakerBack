package com.example.roommaker.app.categorias.examples.whoistheimpostor.repository.entity;

import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.Card;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostor;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Document(collection = "who_is_the_impostor")
@CompoundIndexes({
        @CompoundIndex(name = "unique_sala_user", def = "{'username_dono': 1, 'nome_sala': 1}", unique = true)
})
public class WhoIsTheImpostorEntity {
    @Id
    private String id;
    @Field("nome_sala")
    private String nomeSala;
    @Field("username_dono")
    private String usernameDono;
    private Boolean jogando;
    private List<String> jogadores;
    private String impostor;
    private Card carta;

    public WhoIsTheImpostor toDomain(){
        return WhoIsTheImpostor.builder()
                .nomeSala(this.nomeSala)
                .usernameDono(this.usernameDono)
                .jogando(this.jogando)
                .jogadores(this.jogadores)
                .impostor(this.impostor)
                .carta(this.carta)
                .build();
    }

    public WhoIsTheImpostorEntity (WhoIsTheImpostor whoIsTheImpostor){
        this.nomeSala = whoIsTheImpostor.getNomeSala();
        this.usernameDono = whoIsTheImpostor.getUsernameDono();
        this.jogando = whoIsTheImpostor.getJogando();
        this.jogadores = whoIsTheImpostor.getJogadores();
        this.impostor = whoIsTheImpostor.getImpostor();
        this.carta = whoIsTheImpostor.getCarta();
    }

    public static WhoIsTheImpostorEntity fromDomain(WhoIsTheImpostor entity){
        return WhoIsTheImpostorEntity.builder()
                .nomeSala(entity.getNomeSala())
                .usernameDono(entity.getUsernameDono())
                .jogando(entity.getJogando())
                .jogadores(entity.getJogadores())
                .impostor(entity.getImpostor())
                .carta(entity.getCarta())
                .build();
    }
}
