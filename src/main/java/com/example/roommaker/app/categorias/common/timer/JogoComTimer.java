package com.example.roommaker.app.categorias.common.timer;

/**
 * Interface para jogos que suportam timer.
 * Implementada pelos Managers de jogos que querem usar o sistema de timer.
 */
public interface JogoComTimer {

    /**
     * Retorna a configuração de timer da sala.
     */
    TimerConfig obterConfigTimer(String nomeSala, String usernameDono);

    /**
     * Atualiza a configuração de timer da sala.
     * Só pode ser chamado antes de iniciar a partida.
     */
    void configurarTimer(String nomeSala, String usernameDono, String username, TimerConfig config);

    /**
     * Callback chamado quando o tempo de um jogador esgota.
     * O jogo deve processar a derrota por tempo.
     */
    void processarTimeout(String nomeSala, String usernameDono, String usernameJogadorTimeout);

    /**
     * Retorna o ID único da sala para o timer (geralmente nomeSala + usernameDono).
     */
    default String gerarSalaId(String nomeSala, String usernameDono) {
        return nomeSala + ":" + usernameDono;
    }
}
