package com.example.roommaker.app.categorias.examples.xadrez.domain;

import com.example.roommaker.app.categorias.examples.xadrez.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Monta o {@link XadrezResponse} enviado aos jogadores.
 *
 * Existe porque a montagem estava duplicada entre o XadrezManager e o
 * XadrezTimeoutScheduler, e as duas cópias já tinham divergido (a do scheduler
 * não mandava o modoVisual). Como a resposta agora também carrega dados
 * privados de cada jogador — a fila de pré-lances —, ter uma cópia só deixou de
 * ser questão de gosto: é onde a regra de "não vazar a fila do adversário" pode
 * ser garantida em um lugar.
 */
@Component
public class XadrezResponseFactory {

    /**
     * @param username           destinatário da resposta. Determina qual histórico
     *                           e qual fila de pré-lances vão junto. Null monta uma
     *                           resposta genérica, sem nenhum dado privado.
     * @param preLancesCancelados se a fila DESTE destinatário acabou de ser
     *                           descartada por pré-lance ilegal.
     */
    public XadrezResponse construir(SalaXadrez salaXadrez, String username, String evento,
            boolean preLancesCancelados) {
        PartidaXadrez partida = salaXadrez.getPartidaAtual();

        XadrezResponse.XadrezResponseBuilder builder = XadrezResponse.builder()
                .usernameBrancas(salaXadrez.getUsernameBrancas())
                .usernamePretas(salaXadrez.getUsernamePretas())
                .notacao(salaXadrez.getNotacao())
                .modoVisual(salaXadrez.getModoVisual())
                .evento(evento)
                .partidaEmAndamento(salaXadrez.partidaEmAndamento());

        if (partida != null) {
            builder.partidaId(partida.getId())
                    .lances(new ArrayList<>(partida.getLances()))
                    .resultado(partida.getResultado() != null ? partida.getResultado().name() : null)
                    .motivo(partida.getMotivo() != null ? partida.getMotivo().name() : null)
                    .propostaEmpate(partida.getPropostaEmpate())
                    .lancesIlegaisBrancas(partida.getLancesIlegaisBrancas())
                    .lancesIlegaisPretas(partida.getLancesIlegaisPretas())
                    .vezDasBrancas(partida.vezDasBrancas());

            if (partida.getControleTempo() != null) {
                ControleTempoXadrez ct = partida.getControleTempo();
                builder.tempoInicialBrancas(ct.getTempoInicialBrancasSegundos())
                        .tempoInicialPretas(ct.getTempoInicialPretasSegundos())
                        .incrementoBrancas(ct.getIncrementoBrancasSegundos())
                        .incrementoPretas(ct.getIncrementoPretasSegundos())
                        .tempoRestanteBrancas(ct.getTempoRestanteBrancasSegundos())
                        .tempoRestantePretas(ct.getTempoRestantePretasSegundos())
                        .timestampUltimoLance(ct.getTimestampUltimoLance());
            }

            // Só a própria fila. Um jogador que descobrisse o pré-lance do
            // adversário saberia a resposta dele antes de escolher o próprio lance.
            Boolean meuLadoBrancas = ladoDe(salaXadrez, username);
            if (meuLadoBrancas != null) {
                builder.meusPreLances(copiar(partida.preLancesDe(meuLadoBrancas)))
                        .preLancesCancelados(preLancesCancelados);
            }
        }

        if (username != null && salaXadrez.getHistoricoPorUsername().containsKey(username)) {
            builder.historico(historicoDe(salaXadrez, username));
        }

        return builder.build();
    }

    public XadrezResponse construir(SalaXadrez salaXadrez, String username, String evento) {
        return construir(salaXadrez, username, evento, false);
    }

    /**
     * true = joga de brancas, false = de pretas, null = não é jogador da partida
     * (espectador, ou uma resposta genérica sem destinatário).
     */
    private Boolean ladoDe(SalaXadrez salaXadrez, String username) {
        if (username == null)
            return null;
        if (username.equals(salaXadrez.getUsernameBrancas()))
            return Boolean.TRUE;
        if (username.equals(salaXadrez.getUsernamePretas()))
            return Boolean.FALSE;
        return null;
    }

    private List<PreLance> copiar(List<PreLance> fila) {
        List<PreLance> copia = new ArrayList<>(fila.size());
        for (PreLance p : fila) {
            copia.add(new PreLance(p.getFrom(), p.getTo(), p.getPromocao()));
        }
        return copia;
    }

    private List<XadrezResponse.PartidaXadrezResumo> historicoDe(SalaXadrez salaXadrez, String username) {
        return salaXadrez.getHistoricoPorUsername().get(username).stream()
                .map(p -> {
                    XadrezResponse.PartidaXadrezResumo.PartidaXadrezResumoBuilder resumo = XadrezResponse.PartidaXadrezResumo
                            .builder()
                            .id(p.getId())
                            .pgn(p.pgn())
                            .lances(new ArrayList<>(p.getLances()))
                            .resultado(p.getResultado() != null ? p.getResultado().name() : null)
                            .motivo(p.getMotivo() != null ? p.getMotivo().name() : null)
                            .lancesIlegaisBrancas(p.getLancesIlegaisBrancas())
                            .lancesIlegaisPretas(p.getLancesIlegaisPretas())
                            .usernameBrancas(p.getUsernameBrancas())
                            .usernamePretas(p.getUsernamePretas())
                            .notacao(p.getNotacao())
                            .modoVisual(p.getModoVisual());

                    if (p.getControleTempo() != null) {
                        ControleTempoXadrez ct = p.getControleTempo();
                        resumo.tempoInicialBrancas(ct.getTempoInicialBrancasSegundos())
                                .tempoInicialPretas(ct.getTempoInicialPretasSegundos())
                                .incrementoBrancas(ct.getIncrementoBrancasSegundos())
                                .incrementoPretas(ct.getIncrementoPretasSegundos());
                    }

                    return resumo.build();
                })
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .toList();
    }
}
