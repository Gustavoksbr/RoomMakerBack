package com.example.roommaker.app.categorias.common.historico;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe utilitária para gerenciar histórico de partidas por jogador.
 * Mantém um mapa de username -> lista de partidas.
 * 
 * @param <T> Tipo da partida
 */
public class GerenciadorHistorico<T> {

    private final Map<String, List<T>> historicoPorUsername;

    public GerenciadorHistorico() {
        this.historicoPorUsername = new HashMap<>();
    }

    public GerenciadorHistorico(Map<String, List<T>> historicoExistente) {
        this.historicoPorUsername = historicoExistente != null ? historicoExistente : new HashMap<>();
    }

    /**
     * Adiciona uma partida ao histórico de um jogador.
     */
    public void adicionarPartida(String username, T partida) {
        if (username == null || partida == null) {
            return;
        }
        historicoPorUsername.computeIfAbsent(username, k -> new ArrayList<>()).add(partida);
    }

    /**
     * Adiciona uma partida ao histórico de múltiplos jogadores.
     */
    public void adicionarPartidaParaJogadores(T partida, String... usernames) {
        for (String username : usernames) {
            adicionarPartida(username, partida);
        }
    }

    /**
     * Retorna o histórico de um jogador específico (ordem decrescente).
     */
    public List<T> obterHistorico(String username) {
        List<T> historico = historicoPorUsername.getOrDefault(username, new ArrayList<>());
        // Retorna uma cópia invertida (mais recente primeiro)
        List<T> copia = new ArrayList<>(historico);
        Collections.reverse(copia);
        return copia;
    }

    /**
     * Retorna o histórico completo de todos os jogadores (sem duplicatas).
     */
    public List<T> obterHistoricoCompleto() {
        // Usa LinkedHashSet para manter ordem e evitar duplicatas
        Set<T> todasPartidas = new LinkedHashSet<>();
        historicoPorUsername.values().forEach(todasPartidas::addAll);

        List<T> lista = new ArrayList<>(todasPartidas);
        Collections.reverse(lista);
        return lista;
    }

    /**
     * Limpa todo o histórico.
     */
    public void limpar() {
        historicoPorUsername.clear();
    }

    /**
     * Limpa o histórico de um jogador específico.
     */
    public void limparHistoricoJogador(String username) {
        historicoPorUsername.remove(username);
    }

    /**
     * Retorna o mapa interno (para persistência).
     */
    public Map<String, List<T>> obterMapa() {
        return historicoPorUsername;
    }

    /**
     * Retorna a quantidade de partidas no histórico de um jogador.
     */
    public int tamanhoHistorico(String username) {
        return historicoPorUsername.getOrDefault(username, new ArrayList<>()).size();
    }

    /**
     * Verifica se existe histórico para um jogador.
     */
    public boolean temHistorico(String username) {
        return historicoPorUsername.containsKey(username) &&
                !historicoPorUsername.get(username).isEmpty();
    }
}
