package com.example.roommaker.app.categorias.examples.tictactoe.repository.entity;

import com.example.roommaker.app.categorias.examples.tictactoe.domain.models.TicTacToe;
import com.example.roommaker.app.categorias.examples.tictactoe.domain.models.TicTacToeSala;
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
@Document(collection = "tictactoes_da_sala")
public class TicTacToeDaSalaEntity {
    @Id
    private String id;

    @Field("nome_sala")
    private String nomeSala;

    @Field("username_dono")
    private String usernameDono;

    @Field("username_oponente")
    private String usernameOponente;

    @Field("jogo_atual")
    private TicTacToe jogoAtual;

    @Field("historico")
    private List<TicTacToe> historico;

    @Field("tamanho_historico")
    private Integer tamanhoHistorico;

//    @Field("vitorias_dono")
//    private Integer vitoriasDono;
//
//    @Field("vitorias_oponente")
//    private Integer vitoriasOponente;
//
    public TicTacToeDaSalaEntity(TicTacToeSala ticTacToeSala){
        this.nomeSala = ticTacToeSala.getNomeSala();
        this.usernameDono = ticTacToeSala.getUsernameDono();
        this.usernameOponente = ticTacToeSala.getUsernameOponente();
        this.jogoAtual = ticTacToeSala.getJogoAtual();
        this.historico = ticTacToeSala.getHistorico();
        this.tamanhoHistorico = ticTacToeSala.getTamanhoHistorico();
//        this.vitoriasDono = ticTacToeSala.getVitoriasDono();
//        this.vitoriasOponente = ticTacToeSala.getVitoriasOponente();
    }
    public TicTacToeSala toDomain(){
        return TicTacToeSala.builder()
                .nomeSala(this.nomeSala)
                .usernameDono(this.usernameDono)
                .usernameOponente(this.usernameOponente)
                .jogoAtual(this.jogoAtual)
                .historico(this.historico)
                .tamanhoHistorico(this.tamanhoHistorico)
//                .vitoriasDono(this.vitoriasDono)
//                .vitoriasOponente(this.vitoriasOponente)
                .build();
    }
    public void atualizar(TicTacToeSala ticTacToeSala){
        this.nomeSala = ticTacToeSala.getNomeSala();
        this.usernameDono = ticTacToeSala.getUsernameDono();
        this.usernameOponente = ticTacToeSala.getUsernameOponente();
        this.jogoAtual = ticTacToeSala.getJogoAtual();
        this.historico = ticTacToeSala.getHistorico();
        this.tamanhoHistorico = ticTacToeSala.getTamanhoHistorico();
//        this.vitoriasDono = ticTacToeSala.getVitoriasDono();
//        this.vitoriasOponente = ticTacToeSala.getVitoriasOponente();
    }
}