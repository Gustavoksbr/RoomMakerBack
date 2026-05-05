package com.example.roommaker.app.categorias.examples.jokenpo.domain.model;

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
public class JokenpoSala extends Sala {
    private String nomeSala;
    private String usernameDono;
    private String usernameOponente;
    private Jokenpo jogoAtual;
    private List<Jokenpo> historico;

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
    private Map<String, List<Jokenpo>> historicoPorUsername = new HashMap<>();
}