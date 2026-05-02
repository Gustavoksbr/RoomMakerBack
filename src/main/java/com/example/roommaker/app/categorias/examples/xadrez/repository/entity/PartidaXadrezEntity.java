package com.example.roommaker.app.categorias.examples.xadrez.repository.entity;

import com.example.roommaker.app.categorias.examples.xadrez.domain.model.MotivoXadrez;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.PartidaXadrez;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.ResultadoXadrez;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartidaXadrezEntity {

    private Long id;
    @Builder.Default
    private List<String> lances = new ArrayList<>();
    @Builder.Default
    private String resultado = "EM_ANDAMENTO";
    private String motivo;
    private String propostaEmpate;
    private int lancesIlegaisBrancas;
    private int lancesIlegaisPretas;
    private String usernameBrancas;
    private String usernamePretas;

    public static PartidaXadrezEntity fromDomain(PartidaXadrez p) {
        return PartidaXadrezEntity.builder()
                .id(p.getId())
                .lances(new ArrayList<>(p.getLances()))
                .resultado(p.getResultado() != null ? p.getResultado().name() : "EM_ANDAMENTO")
                .motivo(p.getMotivo() != null ? p.getMotivo().name() : null)
                .propostaEmpate(p.getPropostaEmpate())
                .lancesIlegaisBrancas(p.getLancesIlegaisBrancas())
                .lancesIlegaisPretas(p.getLancesIlegaisPretas())
                .usernameBrancas(p.getUsernameBrancas())
                .usernamePretas(p.getUsernamePretas())
                .build();
    }

    public PartidaXadrez toDomain() {
        return PartidaXadrez.builder()
                .id(this.id)
                .lances(new ArrayList<>(this.lances))
                .resultado(
                        this.resultado != null ? ResultadoXadrez.valueOf(this.resultado) : ResultadoXadrez.EM_ANDAMENTO)
                .motivo(this.motivo != null ? MotivoXadrez.valueOf(this.motivo) : null)
                .propostaEmpate(this.propostaEmpate)
                .lancesIlegaisBrancas(this.lancesIlegaisBrancas)
                .lancesIlegaisPretas(this.lancesIlegaisPretas)
                .usernameBrancas(this.usernameBrancas)
                .usernamePretas(this.usernamePretas)
                .build();
    }
}
