package com.example.roommaker.app.categorias.examples.xadrez.repository.entity;

import com.example.roommaker.app.categorias.examples.xadrez.domain.model.NotacaoXadrez;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.SalaXadrez;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "xadrez_salas")
@CompoundIndexes({
        @CompoundIndex(name = "unique_xadrez_sala", def = "{'username_dono': 1, 'nome_sala': 1}", unique = true)
})
public class SalaXadrezEntity {

    @Id
    private String id;

    @Field("nome_sala")
    private String nomeSala;

    @Field("username_dono")
    private String usernameDono;

    @Field("username_brancas")
    private String usernameBrancas;

    @Field("username_pretas")
    private String usernamePretas;

    @Field("notacao")
    private String notacao;

    @Field("partida_atual")
    private PartidaXadrezEntity partidaAtual;

    @Field("proximo_id_partida")
    private Long proximoIdPartida;

    @Field("historico_por_username")
    private Map<String, List<PartidaXadrezEntity>> historicoPorUsername;

    public static SalaXadrezEntity fromDomain(SalaXadrez s) {
        Map<String, List<PartidaXadrezEntity>> historico = new HashMap<>();
        if (s.getHistoricoPorUsername() != null) {
            s.getHistoricoPorUsername().forEach((username, partidas) -> historico.put(username, partidas.stream()
                    .map(PartidaXadrezEntity::fromDomain)
                    .collect(Collectors.toList())));
        }
        return SalaXadrezEntity.builder()
                .nomeSala(s.getNomeSala())
                .usernameDono(s.getUsernameDono())
                .usernameBrancas(s.getUsernameBrancas())
                .usernamePretas(s.getUsernamePretas())
                .notacao(s.getNotacao() != null ? s.getNotacao().name() : NotacaoXadrez.INGLESA.name())
                .partidaAtual(s.getPartidaAtual() != null ? PartidaXadrezEntity.fromDomain(s.getPartidaAtual()) : null)
                .proximoIdPartida(s.getProximoIdPartida())
                .historicoPorUsername(historico)
                .build();
    }

    public SalaXadrez toDomain() {
        Map<String, List<com.example.roommaker.app.categorias.examples.xadrez.domain.model.PartidaXadrez>> historico = new HashMap<>();
        if (this.historicoPorUsername != null) {
            this.historicoPorUsername.forEach((username, partidas) -> historico.put(username, partidas.stream()
                    .map(PartidaXadrezEntity::toDomain)
                    .collect(Collectors.toList())));
        }
        return SalaXadrez.builder()
                .nomeSala(this.nomeSala)
                .usernameDono(this.usernameDono)
                .usernameBrancas(this.usernameBrancas)
                .usernamePretas(this.usernamePretas)
                .notacao(this.notacao != null ? NotacaoXadrez.valueOf(this.notacao) : NotacaoXadrez.INGLESA)
                .partidaAtual(this.partidaAtual != null ? this.partidaAtual.toDomain() : null)
                .proximoIdPartida(this.proximoIdPartida != null ? this.proximoIdPartida : 1L)
                .historicoPorUsername(historico)
                .build();
    }
}
