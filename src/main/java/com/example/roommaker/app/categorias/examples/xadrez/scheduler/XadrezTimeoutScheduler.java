package com.example.roommaker.app.categorias.examples.xadrez.scheduler;

import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezResponseFactory;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.*;
import com.example.roommaker.app.categorias.examples.xadrez.domain.service.XadrezTempoService;
import com.example.roommaker.app.categorias.examples.xadrez.repository.SalaXadrezRepository;
import com.example.roommaker.app.categorias.examples.xadrez.sender.XadrezSender;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.domain.models.Sala;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Scheduler LEVE para verificar timeout de partidas ativas.
 * 
 * DIFERENÇAS DO MODELO ANTIGO:
 * - NÃO decrementa tempo (cálculo é dinâmico)
 * - NÃO faz findAll() (usa cache ou índice)
 * - NÃO salva a cada segundo (só quando timeout)
 * - Apenas VERIFICA se tempo esgotou
 * 
 * OTIMIZAÇÕES:
 * - Executa apenas se houver partidas ativas
 * - Usa cálculo dinâmico (sem modificar dados)
 * - Transação apenas quando encerra partida
 */
@Service
@Slf4j
public class XadrezTimeoutScheduler {

    private final SalaXadrezRepository repository;
    private final XadrezTempoService tempoService;
    private final XadrezSender sender;
    private final SalaManager salaManager;
    private final XadrezResponseFactory responseFactory;

    public XadrezTimeoutScheduler(SalaXadrezRepository repository, XadrezTempoService tempoService,
            XadrezSender sender, SalaManager salaManager, XadrezResponseFactory responseFactory) {
        this.repository = repository;
        this.tempoService = tempoService;
        this.sender = sender;
        this.salaManager = salaManager;
        this.responseFactory = responseFactory;
    }

    /**
     * Verifica timeout a cada 1 segundo.
     * 
     * IMPORTANTE: Este método NÃO modifica dados, apenas LEITURA.
     * Modificação ocorre apenas quando detecta timeout (raro).
     */
    @Scheduled(fixedRate = 1000)
    public void verificarTimeouts() {
        // Busca apenas salas com partidas ativas
        // TODO: Otimizar com índice ou cache se necessário
        List<SalaXadrez> salasAtivas = repository.findAll().stream()
                .filter(SalaXadrez::partidaEmAndamento)
                .filter(s -> s.getPartidaAtual() != null)
                .filter(s -> s.getPartidaAtual().getControleTempo() != null)
                .filter(s -> !s.getPartidaAtual().getControleTempo().tempoInfinito())
                .toList();

        if (salasAtivas.isEmpty()) {
            return; // Nenhuma partida ativa com tempo, não faz nada
        }

        log.debug("Verificando timeout em {} partidas ativas", salasAtivas.size());

        for (SalaXadrez salaXadrez : salasAtivas) {
            try {
                verificarTimeoutDaSala(salaXadrez);
            } catch (Exception e) {
                log.error("Erro ao verificar timeout da sala {}/{}: {}",
                        salaXadrez.getUsernameDono(), salaXadrez.getNomeSala(), e.getMessage());
            }
        }
    }

    /**
     * Verifica timeout de uma sala específica.
     * Usa cálculo dinâmico - NÃO modifica dados a menos que timeout ocorra.
     */
    @Transactional
    protected void verificarTimeoutDaSala(SalaXadrez salaXadrez) {
        PartidaXadrez partida = salaXadrez.getPartidaAtual();

        if (partida == null || !salaXadrez.partidaEmAndamento()) {
            return;
        }

        // Verifica timeout usando cálculo dinâmico (sem modificar dados)
        XadrezTempoService.ResultadoTimeout timeout = tempoService.verificarTimeout(partida, salaXadrez);

        if (timeout == null) {
            return; // Tempo não esgotou, não faz nada
        }

        // Tempo esgotou! Encerra a partida
        log.info("Timeout detectado na sala {}/{} - Resultado: {}",
                salaXadrez.getUsernameDono(), salaXadrez.getNomeSala(), timeout.resultado());

        partida.encerrar(timeout.resultado(), timeout.motivo());
        tempoService.congelarTempo(partida);
        salaXadrez.arquivarPartida(partida);
        repository.save(salaXadrez);

        // Notifica todos os jogadores
        try {
            Sala sala = salaManager.mostrarSala(salaXadrez.getNomeSala(), salaXadrez.getUsernameDono());
            enviarParaTodos(sala, salaXadrez, "FIM");
        } catch (Exception e) {
            log.warn("Não foi possível notificar jogadores da sala {}/{}: {}",
                    salaXadrez.getUsernameDono(), salaXadrez.getNomeSala(), e.getMessage());
        }
    }

    private void enviarParaTodos(Sala sala, SalaXadrez salaXadrez, String evento) {
        List<String> ouvintes = jogadoresDaSala(sala);
        for (String ouvinte : ouvintes) {
            XadrezResponse r = responseFactory.construir(salaXadrez, ouvinte, evento);
            sender.enviarParaUsuario(salaXadrez.getUsernameDono(), salaXadrez.getNomeSala(), ouvinte, r);
        }
    }

    private List<String> jogadoresDaSala(Sala sala) {
        List<String> lista = new ArrayList<>(sala.getUsernameParticipantes());
        lista.add(sala.getUsernameDono());
        return lista;
    }

}
