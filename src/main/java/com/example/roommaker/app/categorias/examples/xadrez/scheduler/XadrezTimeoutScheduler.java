package com.example.roommaker.app.categorias.examples.xadrez.scheduler;

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

    public XadrezTimeoutScheduler(SalaXadrezRepository repository, XadrezTempoService tempoService,
            XadrezSender sender, SalaManager salaManager) {
        this.repository = repository;
        this.tempoService = tempoService;
        this.sender = sender;
        this.salaManager = salaManager;
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
            XadrezResponse r = construirResponse(salaXadrez, ouvinte, evento);
            sender.enviarParaUsuario(salaXadrez.getUsernameDono(), salaXadrez.getNomeSala(), ouvinte, r);
        }
    }

    private List<String> jogadoresDaSala(Sala sala) {
        List<String> lista = new ArrayList<>(sala.getUsernameParticipantes());
        lista.add(sala.getUsernameDono());
        return lista;
    }

    private XadrezResponse construirResponse(SalaXadrez salaXadrez, String username, String evento) {
        PartidaXadrez partida = salaXadrez.getPartidaAtual();

        XadrezResponse.XadrezResponseBuilder builder = XadrezResponse.builder()
                .usernameBrancas(salaXadrez.getUsernameBrancas())
                .usernamePretas(salaXadrez.getUsernamePretas())
                .notacao(salaXadrez.getNotacao())
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
        }

        if (username != null && salaXadrez.getHistoricoPorUsername().containsKey(username)) {
            List<XadrezResponse.PartidaXadrezResumo> historico = salaXadrez.getHistoricoPorUsername()
                    .get(username).stream()
                    .map(p -> {
                        XadrezResponse.PartidaXadrezResumo.PartidaXadrezResumoBuilder resumoBuilder = XadrezResponse.PartidaXadrezResumo
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
                                .notacao(p.getNotacao());

                        if (p.getControleTempo() != null) {
                            ControleTempoXadrez ct = p.getControleTempo();
                            resumoBuilder.tempoInicialBrancas(ct.getTempoInicialBrancasSegundos())
                                    .tempoInicialPretas(ct.getTempoInicialPretasSegundos())
                                    .incrementoBrancas(ct.getIncrementoBrancasSegundos())
                                    .incrementoPretas(ct.getIncrementoPretasSegundos());
                        }

                        return resumoBuilder.build();
                    })
                    .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                    .toList();
            builder.historico(historico);
        }

        return builder.build();
    }
}
