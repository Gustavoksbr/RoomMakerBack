package com.example.roommaker.app.categorias.examples.coup.repository.entity;

import com.example.roommaker.app.categorias.examples.coup.domain.model.Coup;
import com.example.roommaker.app.categorias.examples.coup.domain.model.SalaDosCoups;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "salas_dos_coups")
public class SalaDosCoupsEntity {
    @Id
    private String id;
    @Field("nome_sala")
    private String nomeSala;
    @Field("username_dono")
    private String usernameDono;
    @Field("coup_atual")
    private Coup coupAtual;
    @Field("historico_coups")
    private List<Coup> historicoCoups;
    @Field("convidados")
    private List<String> convidados;
    @Field("jogadores")
    private List<String> jogadores;

    public SalaDosCoupsEntity(SalaDosCoups salaDosCoups){
        this.nomeSala = salaDosCoups.getNomeSala();
        this.usernameDono = salaDosCoups.getUsernameDono();
        this.coupAtual = salaDosCoups.getCoupAtual();
        this.historicoCoups = salaDosCoups.getHistoricoCoups();
        this.convidados = salaDosCoups.getConvidados();
        this.jogadores = salaDosCoups.getJogadores();
    }

    public SalaDosCoups toModel(){
        return SalaDosCoups.builder()
                .nomeSala(this.nomeSala)
                .usernameDono(this.usernameDono)
                .coupAtual(this.coupAtual)
                .historicoCoups(this.historicoCoups)
                .convidados(this.convidados)
                .jogadores(this.jogadores)
                .build();
    }
    public void atualizar(SalaDosCoups salaDosCoups){
        this.nomeSala = salaDosCoups.getNomeSala();
        this.usernameDono = salaDosCoups.getUsernameDono();
        this.coupAtual = salaDosCoups.getCoupAtual();
        this.historicoCoups = salaDosCoups.getHistoricoCoups();
        this.convidados = salaDosCoups.getConvidados();
        this.jogadores = salaDosCoups.getJogadores();
    }
}
