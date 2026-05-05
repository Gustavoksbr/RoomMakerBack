package com.example.roommaker.app.categorias.common.historico;

import java.util.List;

/**
 * Interface para jogos que mantêm histórico completo de partidas.
 * Implementada pelos Managers de jogos que querem usar o sistema de histórico.
 * 
 * @param <T> Tipo da partida (ex: TicTacToe, Jokenpo, PartidaXadrez)
 */
public interface JogoComHistorico<T> {

    /**
     * Retorna o histórico completo de partidas de um jogador específico.
     * 
     * @param nomeSala     Nome da sala
     * @param usernameDono Dono da sala
     * @param username     Username do jogador
     * @return Lista de partidas em que o jogador participou (ordem decrescente -
     *         mais recente primeiro)
     */
    List<T> obterHistorico(String nomeSala, String usernameDono, String username);

    /**
     * Retorna o histórico completo de todas as partidas da sala.
     * 
     * @param nomeSala     Nome da sala
     * @param usernameDono Dono da sala
     * @return Lista de todas as partidas (ordem decrescente - mais recente
     *         primeiro)
     */
    List<T> obterHistoricoCompleto(String nomeSala, String usernameDono);

    /**
     * Limpa o histórico de partidas da sala.
     * Apenas o dono pode executar esta ação.
     * 
     * @param nomeSala     Nome da sala
     * @param usernameDono Dono da sala
     * @param username     Username do solicitante (deve ser o dono)
     */
    void limparHistorico(String nomeSala, String usernameDono, String username);
}
