package com.example.roommaker.app.categorias.examples.xadrez.repository.entity;

import com.example.roommaker.app.categorias.examples.xadrez.domain.model.ControleTempoXadrez;
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
        private String notacao; // PORTUGUESA ou INGLESA

        // Campos de controle de tempo (em milissegundos)
        private Long tempoInicialBrancas;
        private Long tempoInicialPretas;
        private Long incrementoBrancas;
        private Long incrementoPretas;
        private Long tempoRestanteBrancas;
        private Long tempoRestantePretas;
        private Long timestampUltimoLance;

        public static PartidaXadrezEntity fromDomain(PartidaXadrez p) {
                PartidaXadrezEntityBuilder builder = PartidaXadrezEntity.builder()
                                .id(p.getId())
                                .lances(new ArrayList<>(p.getLances()))
                                .resultado(p.getResultado() != null ? p.getResultado().name() : "EM_ANDAMENTO")
                                .motivo(p.getMotivo() != null ? p.getMotivo().name() : null)
                                .propostaEmpate(p.getPropostaEmpate())
                                .lancesIlegaisBrancas(p.getLancesIlegaisBrancas())
                                .lancesIlegaisPretas(p.getLancesIlegaisPretas())
                                .usernameBrancas(p.getUsernameBrancas())
                                .usernamePretas(p.getUsernamePretas())
                                .notacao(p.getNotacao() != null ? p.getNotacao().name() : null);

                // Adiciona controle de tempo se existir
                if (p.getControleTempo() != null) {
                        ControleTempoXadrez ct = p.getControleTempo();
                        builder.tempoInicialBrancas(ct.getTempoInicialBrancas())
                                        .tempoInicialPretas(ct.getTempoInicialPretas())
                                        .incrementoBrancas(ct.getIncrementoBrancas())
                                        .incrementoPretas(ct.getIncrementoPretas())
                                        .tempoRestanteBrancas(ct.getTempoRestanteBrancas())
                                        .tempoRestantePretas(ct.getTempoRestantePretas())
                                        .timestampUltimoLance(ct.getTimestampUltimoLance());
                }

                return builder.build();
        }

        public PartidaXadrez toDomain() {
                PartidaXadrez.PartidaXadrezBuilder builder = PartidaXadrez.builder()
                                .id(this.id)
                                .lances(new ArrayList<>(this.lances))
                                .resultado(
                                                this.resultado != null ? ResultadoXadrez.valueOf(this.resultado)
                                                                : ResultadoXadrez.EM_ANDAMENTO)
                                .motivo(this.motivo != null ? MotivoXadrez.valueOf(this.motivo) : null)
                                .propostaEmpate(this.propostaEmpate)
                                .lancesIlegaisBrancas(this.lancesIlegaisBrancas)
                                .lancesIlegaisPretas(this.lancesIlegaisPretas)
                                .usernameBrancas(this.usernameBrancas)
                                .usernamePretas(this.usernamePretas)
                                .notacao(this.notacao != null
                                                ? com.example.roommaker.app.categorias.examples.xadrez.domain.model.NotacaoXadrez
                                                                .valueOf(this.notacao)
                                                : null);

                // Reconstrói controle de tempo se existir
                if (this.tempoInicialBrancas != null || this.tempoInicialPretas != null) {
                        ControleTempoXadrez ct = ControleTempoXadrez.builder()
                                        .tempoInicialBrancas(this.tempoInicialBrancas)
                                        .tempoInicialPretas(this.tempoInicialPretas)
                                        .incrementoBrancas(this.incrementoBrancas != null ? this.incrementoBrancas : 0L)
                                        .incrementoPretas(this.incrementoPretas != null ? this.incrementoPretas : 0L)
                                        .tempoRestanteBrancas(this.tempoRestanteBrancas)
                                        .tempoRestantePretas(this.tempoRestantePretas)
                                        .timestampUltimoLance(this.timestampUltimoLance)
                                        .build();
                        builder.controleTempo(ct);
                }

                return builder.build();
        }
}
