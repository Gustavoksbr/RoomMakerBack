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
        } else {
            // Sem partida em andamento — mas se uma acabou de ser arquivada
            // (arquivarPartida zera partidaAtual ANTES deste método rodar, então
            // isso vale tanto para o próprio evento FIM quanto para o reload logo
            // depois), o cliente ainda precisa do resultado e dos lances dela: é
            // o que deixa o tabuleiro na tela pra reanalisar, em vez de sumir com
            // tudo assim que a partida termina. Pré-lances ficam de fora de
            // propósito — não fazem sentido para uma partida que já acabou.
            PartidaXadrez ultimaEncerrada = ultimaPartidaEncerrada(salaXadrez);
            if (ultimaEncerrada != null) {
                builder.partidaId(ultimaEncerrada.getId())
                        .lances(new ArrayList<>(ultimaEncerrada.getLances()))
                        .resultado(ultimaEncerrada.getResultado() != null ? ultimaEncerrada.getResultado().name() : null)
                        .motivo(ultimaEncerrada.getMotivo() != null ? ultimaEncerrada.getMotivo().name() : null)
                        .lancesIlegaisBrancas(ultimaEncerrada.getLancesIlegaisBrancas())
                        .lancesIlegaisPretas(ultimaEncerrada.getLancesIlegaisPretas());
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
     * A última partida encerrada com o par brancas/pretas ATUAL da sala, ou
     * null se nenhuma das duas nunca jogou aqui.
     *
     * {@code historicoPorUsername} é por username e pode ter jogos de
     * configurações antigas da sala (outro par de jogadores); mas
     * {@code arquivarPartida} adiciona o MESMO objeto à lista de brancas e de
     * pretas, então o último item da lista de qualquer um dos dois já é,
     * necessariamente, a partida que acabou de terminar com a configuração
     * atual — não precisa comparar usernameBrancas/usernamePretas dentro da
     * partida arquivada.
     */
    private PartidaXadrez ultimaPartidaEncerrada(SalaXadrez salaXadrez) {
        PartidaXadrez daBrancas = ultimaDaLista(salaXadrez, salaXadrez.getUsernameBrancas());
        if (daBrancas != null) {
            return daBrancas;
        }
        return ultimaDaLista(salaXadrez, salaXadrez.getUsernamePretas());
    }

    private PartidaXadrez ultimaDaLista(SalaXadrez salaXadrez, String username) {
        if (username == null) {
            return null;
        }
        List<PartidaXadrez> lista = salaXadrez.getHistoricoPorUsername().get(username);
        return (lista == null || lista.isEmpty()) ? null : lista.get(lista.size() - 1);
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
