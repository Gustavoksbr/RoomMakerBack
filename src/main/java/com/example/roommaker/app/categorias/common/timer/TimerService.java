package com.example.roommaker.app.categorias.common.timer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Serviço que gerencia todos os timers ativos dos jogos.
 * Verifica timeouts periodicamente e notifica via callback.
 */
@Service
@Slf4j
public class TimerService {

    /**
     * Mapa de timers ativos por salaId.
     */
    private final Map<String, GameTimer> timersAtivos = new ConcurrentHashMap<>();

    /**
     * Callbacks para notificar quando o tempo esgotar.
     * Chave: salaId, Valor: callback que recebe o username do jogador que perdeu
     * por tempo.
     */
    private final Map<String, Consumer<String>> timeoutCallbacks = new ConcurrentHashMap<>();

    /**
     * Callbacks para enviar atualizações periódicas do timer via WebSocket.
     * Chave: salaId, Valor: callback que recebe o GameTimer atualizado.
     */
    private final Map<String, Consumer<GameTimer>> updateCallbacks = new ConcurrentHashMap<>();

    /**
     * Registra um novo timer.
     */
    public void registrarTimer(GameTimer timer, Consumer<String> onTimeout, Consumer<GameTimer> onUpdate) {
        timersAtivos.put(timer.getSalaId(), timer);
        if (onTimeout != null) {
            timeoutCallbacks.put(timer.getSalaId(), onTimeout);
        }
        if (onUpdate != null) {
            updateCallbacks.put(timer.getSalaId(), onUpdate);
        }
        log.info("Timer registrado para sala: {}", timer.getSalaId());
    }

    /**
     * Inicia o timer de uma sala.
     */
    public void iniciarTimer(String salaId, String primeiroJogador) {
        GameTimer timer = timersAtivos.get(salaId);
        if (timer != null) {
            timer.iniciar(primeiroJogador);
            log.info("Timer iniciado para sala: {}, jogador: {}", salaId, primeiroJogador);
        }
    }

    /**
     * Pausa o timer de uma sala.
     */
    public void pausarTimer(String salaId) {
        GameTimer timer = timersAtivos.get(salaId);
        if (timer != null) {
            timer.pausar();
            log.info("Timer pausado para sala: {}", salaId);
        }
    }

    /**
     * Troca o turno do timer.
     */
    public void trocarTurno(String salaId) {
        GameTimer timer = timersAtivos.get(salaId);
        if (timer != null) {
            timer.trocarTurno();
            log.debug("Turno trocado para sala: {}, novo jogador: {}", salaId, timer.getJogadorAtual());
        }
    }

    /**
     * Remove o timer de uma sala.
     */
    public void removerTimer(String salaId) {
        timersAtivos.remove(salaId);
        timeoutCallbacks.remove(salaId);
        updateCallbacks.remove(salaId);
        log.info("Timer removido para sala: {}", salaId);
    }

    /**
     * Obtém o timer de uma sala.
     */
    public GameTimer obterTimer(String salaId) {
        return timersAtivos.get(salaId);
    }

    /**
     * Verifica se existe timer para uma sala.
     */
    public boolean existeTimer(String salaId) {
        return timersAtivos.containsKey(salaId);
    }

    /**
     * Tarefa agendada que verifica timeouts e envia atualizações.
     * Executa a cada 100ms para precisão.
     */
    @Scheduled(fixedRate = 100)
    public void verificarTimers() {
        timersAtivos.forEach((salaId, timer) -> {
            if (timer.getPausado() || timer.getTempoEsgotado()) {
                return;
            }

            // Verifica timeout
            if (timer.verificarTimeout()) {
                log.warn("Timeout na sala: {}, jogador: {}", salaId, timer.getJogadorQuePerderPorTempo());
                Consumer<String> callback = timeoutCallbacks.get(salaId);
                if (callback != null) {
                    try {
                        callback.accept(timer.getJogadorQuePerderPorTempo());
                    } catch (Exception e) {
                        log.error("Erro ao executar callback de timeout para sala: {}", salaId, e);
                    }
                }
            }
        });
    }

    /**
     * Tarefa agendada que envia atualizações do timer via WebSocket.
     * Executa a cada 1 segundo.
     */
    @Scheduled(fixedRate = 1000)
    public void enviarAtualizacoes() {
        timersAtivos.forEach((salaId, timer) -> {
            if (timer.getPausado() || timer.getTempoEsgotado()) {
                return;
            }

            Consumer<GameTimer> callback = updateCallbacks.get(salaId);
            if (callback != null) {
                try {
                    callback.accept(timer);
                } catch (Exception e) {
                    log.error("Erro ao enviar atualização de timer para sala: {}", salaId, e);
                }
            }
        });
    }
}
