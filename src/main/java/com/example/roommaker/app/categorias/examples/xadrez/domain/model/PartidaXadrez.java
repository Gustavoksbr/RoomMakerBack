package com.example.roommaker.app.categorias.examples.xadrez.domain.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma partida de xadrez às cegas dentro de uma sala.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartidaXadrez {

    private Long id; // sequencial dentro da sala

    private String usernameBrancas;
    private String usernamePretas;

    /** Notação usada na partida (PORTUGUESA ou INGLESA) */
    private NotacaoXadrez notacao;

    /** Controle de tempo da partida (null = sem controle de tempo) */
    private ControleTempoXadrez controleTempo;

    @Builder.Default
    private List<String> lances = new ArrayList<>();

    @Builder.Default
    private ResultadoXadrez resultado = ResultadoXadrez.EM_ANDAMENTO;

    private MotivoXadrez motivo;

    /** Lado que propôs empate e aguarda resposta. Null se não há proposta. */
    private String propostaEmpate; // "BRANCAS" ou "PRETAS"

    @Builder.Default
    private int lancesIlegaisBrancas = 0;

    @Builder.Default
    private int lancesIlegaisPretas = 0;

    public boolean emAndamento() {
        return ResultadoXadrez.EM_ANDAMENTO.equals(resultado);
    }

    public boolean temPropostaEmpate() {
        return propostaEmpate != null;
    }

    public boolean vezDasBrancas() {
        return lances.size() % 2 == 0;
    }

    public void incrementarIlegais(boolean vezBrancas) {
        if (vezBrancas)
            lancesIlegaisBrancas++;
        else
            lancesIlegaisPretas++;
    }

    public void encerrar(ResultadoXadrez res, MotivoXadrez mot) {
        this.resultado = res;
        this.motivo = mot;
        this.propostaEmpate = null;
    }

    /** Formata os lances em PGN: "1. e4 e5 2. Nf3 Nc6" */
    public String pgn() {
        if (lances.isEmpty())
            return "(sem lances)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lances.size(); i++) {
            if (i % 2 == 0) {
                if (i > 0)
                    sb.append("  ");
                sb.append(i / 2 + 1).append(". ");
            } else {
                sb.append(" ");
            }
            sb.append(lances.get(i));
        }
        return sb.toString();
    }
}
