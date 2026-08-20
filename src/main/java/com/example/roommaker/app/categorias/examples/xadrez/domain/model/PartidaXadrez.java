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

    /**
     * Modo visual: true = tabuleiro com peças, false = às cegas (apenas notação)
     */
    private Boolean modoVisual;

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

    /**
     * Fila de pré-lances das brancas, na ordem em que serão tentados.
     * Vive no servidor (e não só no navegador) para que o relógio não cobre o
     * ida-e-volta da rede e para que ninguém possa forjar "esse lance foi
     * pré-lance" só para ganhar tempo.
     */
    @Builder.Default
    private List<PreLance> preLancesBrancas = new ArrayList<>();

    /** Fila de pré-lances das pretas. Ver {@link #preLancesBrancas}. */
    @Builder.Default
    private List<PreLance> preLancesPretas = new ArrayList<>();

    /** Quantos pré-lances um jogador pode deixar enfileirados. */
    public static final int MAX_PRE_LANCES = 8;

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
        limparTodosPreLances();
    }

    // -------------------------------------------------------------------------
    // Pré-lances
    // -------------------------------------------------------------------------

    /** A fila do lado pedido — a lista real, não uma cópia. */
    public List<PreLance> preLancesDe(boolean brancas) {
        if (brancas) {
            if (preLancesBrancas == null)
                preLancesBrancas = new ArrayList<>();
            return preLancesBrancas;
        }
        if (preLancesPretas == null)
            preLancesPretas = new ArrayList<>();
        return preLancesPretas;
    }

    /** Substitui a fila de um lado inteira. O cliente sempre manda a fila toda. */
    public void definirPreLances(boolean brancas, List<PreLance> fila) {
        List<PreLance> nova = fila == null ? new ArrayList<>() : new ArrayList<>(fila);
        if (nova.size() > MAX_PRE_LANCES) {
            nova = new ArrayList<>(nova.subList(0, MAX_PRE_LANCES));
        }
        if (brancas)
            this.preLancesBrancas = nova;
        else
            this.preLancesPretas = nova;
    }

    public void limparPreLances(boolean brancas) {
        preLancesDe(brancas).clear();
    }

    public void limparTodosPreLances() {
        limparPreLances(true);
        limparPreLances(false);
    }

    public boolean temPreLances(boolean brancas) {
        return !preLancesDe(brancas).isEmpty();
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
