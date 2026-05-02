package com.example.roommaker.app.categorias.examples.xadrez.domain.model;

import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estado da sala de xadrez às cegas.
 * Mantém a partida atual e o histórico de partidas encerradas.
 * O histórico é por participante: cada username tem sua lista de partidas.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalaXadrez {

    private String nomeSala;
    private String usernameDono;

    /** Username do jogador de brancas (pode ser dono ou participante). */
    private String usernameBrancas;

    /** Username do jogador de pretas. */
    private String usernamePretas;

    /** Notação escolhida pelo dono: PORTUGUESA ou INGLESA. */
    @Builder.Default
    private NotacaoXadrez notacao = NotacaoXadrez.INGLESA;

    /** Partida em andamento. Null se não há partida ativa. */
    private PartidaXadrez partidaAtual;

    /** Próximo ID de partida (sequencial dentro da sala). */
    @Builder.Default
    private Long proximoIdPartida = 1L;

    /**
     * Histórico de partidas encerradas por username.
     * Chave: username do participante (brancas ou pretas).
     * Valor: lista de partidas encerradas em que participou.
     */
    @Builder.Default
    private Map<String, List<PartidaXadrez>> historicoPorUsername = new HashMap<>();

    public boolean partidaEmAndamento() {
        return partidaAtual != null && partidaAtual.emAndamento();
    }

    /** Adiciona a partida encerrada ao histórico dos dois jogadores. */
    public void arquivarPartida(PartidaXadrez partida) {
        adicionarAoHistorico(usernameBrancas, partida);
        adicionarAoHistorico(usernamePretas, partida);
        this.partidaAtual = null;
    }

    private void adicionarAoHistorico(String username, PartidaXadrez partida) {
        if (username == null)
            return;
        historicoPorUsername.computeIfAbsent(username, k -> new ArrayList<>()).add(partida);
    }
}
