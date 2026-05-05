package com.example.roommaker.app.categorias.examples.tictactoe.domain.models;

import com.example.roommaker.app.categorias.common.timer.TimerConfig;
import com.example.roommaker.app.domain.models.Sala;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder

public class TicTacToeSala extends Sala {
    private String nomeSala;
    private String usernameDono;
    private String usernameOponente;
    private TicTacToe jogoAtual;
    private List<TicTacToe> historico;
    private Integer tamanhoHistorico;

    /**
     * Configuração de timer da sala (personalizável pelo dono).
     */
    @Builder.Default
    private TimerConfig timerConfig = TimerConfig.semTimer();

    /**
     * Histórico de partidas por jogador (para suporte ao histórico completo).
     * Chave: username, Valor: lista de partidas.
     */
    @Builder.Default
    private Map<String, List<TicTacToe>> historicoPorUsername = new HashMap<>();

    // private Integer vitoriasDono;
    // private Integer vitoriasOponente;
}
