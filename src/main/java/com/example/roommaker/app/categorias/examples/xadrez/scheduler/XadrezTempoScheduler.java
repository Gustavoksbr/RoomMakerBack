package com.example.roommaker.app.categorias.examples.xadrez.scheduler;

import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezLogica;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.*;
import com.example.roommaker.app.categorias.examples.xadrez.repository.SalaXadrezRepository;
import com.example.roommaker.app.categorias.examples.xadrez.sender.XadrezSender;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.domain.models.Sala;
import com.github.bhlangonijr.chesslib.Board;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class XadrezTempoScheduler {

    private final SalaXadrezRepository repository;
    private final XadrezSender sender;
    private final SalaManager salaManager;

    public XadrezTempoScheduler(SalaXadrezRepository repository, XadrezSender sender, SalaManager salaManager) {
        this.repository = repository;
        this.sender = sender;
        this.salaManager = salaManager;
    }

    @Scheduled(fixedRate = 1000) // Executa a cada 1 segundo
    public void verificarTempoEsgotado() {
        List<SalaXadrez> salasComPartida = repository.findAll().stream()
                .filter(SalaXadrez::partidaEmAndamento)
                .toList();

        for (SalaXadrez salaXadrez : salasComPartida) {
            PartidaXadrez partida = salaXadrez.getPartidaAtual();
            if (partida == null || partida.getControleTempo() == null)
                continue;

            ControleTempoXadrez ct = partida.getControleTempo();
            if (ct.tempoInfinito())
                continue;

            // Atualiza o tempo do jogador atual
            boolean vezBrancas = partida.vezDasBrancas();
            atualizarTempo(partida, vezBrancas);

            // Verifica se o tempo esgotou
            if (ct.tempoEsgotado(vezBrancas)) {
                // Reconstrói o tabuleiro para verificar material
                Board board = XadrezLogica.reconstruirBoard(partida.getLances(), salaXadrez.getNotacao());
                boolean oponenteTemMaterial = XadrezLogica.temMaterialSuficiente(board, !vezBrancas);

                ResultadoXadrez resultado;
                MotivoXadrez motivo;
                if (oponenteTemMaterial) {
                    resultado = vezBrancas ? ResultadoXadrez.VITORIA_PRETAS : ResultadoXadrez.VITORIA_BRANCAS;
                    motivo = MotivoXadrez.TEMPO_ESGOTADO;
                } else {
                    resultado = ResultadoXadrez.EMPATE;
                    motivo = MotivoXadrez.TEMPO_ESGOTADO_MATERIAL_INSUFICIENTE;
                }

                partida.encerrar(resultado, motivo);
                salaXadrez.arquivarPartida(partida);
                repository.save(salaXadrez);

                // Envia notificação para todos os jogadores
                try {
                    Sala sala = salaManager.mostrarSala(salaXadrez.getNomeSala(), salaXadrez.getUsernameDono());
                    enviarParaTodos(sala, salaXadrez, "FIM");
                } catch (Exception e) {
                    // Sala pode ter sido deletada, ignora
                }
            } else {
                // Salva o tempo atualizado
                repository.save(salaXadrez);
            }
        }
    }

    private void atualizarTempo(PartidaXadrez partida, boolean vezBrancas) {
        ControleTempoXadrez ct = partida.getControleTempo();
        if (ct == null || ct.getTimestampUltimoLance() == null)
            return;

        long agora = System.currentTimeMillis();
        long decorrido = (agora - ct.getTimestampUltimoLance()) / 1000; // segundos

        if (vezBrancas && ct.getTempoRestanteBrancas() != null) {
            ct.setTempoRestanteBrancas((int) Math.max(0, ct.getTempoRestanteBrancas() - decorrido));
        } else if (!vezBrancas && ct.getTempoRestantePretas() != null) {
            ct.setTempoRestantePretas((int) Math.max(0, ct.getTempoRestantePretas() - decorrido));
        }

        ct.setTimestampUltimoLance(agora);
    }

    private List<String> jogadoresDaSala(Sala sala) {
        List<String> lista = new ArrayList<>(sala.getUsernameParticipantes());
        lista.add(sala.getUsernameDono());
        return lista;
    }

    private void enviarParaTodos(Sala sala, SalaXadrez salaXadrez, String evento) {
        List<String> ouvintes = jogadoresDaSala(sala);
        for (String ouvinte : ouvintes) {
            XadrezResponse r = construirResponse(salaXadrez, ouvinte, evento);
            sender.enviarParaUsuario(salaXadrez.getUsernameDono(), salaXadrez.getNomeSala(), ouvinte, r);
        }
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
                builder.tempoInicialBrancas(ct.getTempoInicialBrancas())
                        .tempoInicialPretas(ct.getTempoInicialPretas())
                        .incrementoBrancas(ct.getIncrementoBrancas())
                        .incrementoPretas(ct.getIncrementoPretas())
                        .tempoRestanteBrancas(ct.getTempoRestanteBrancas())
                        .tempoRestantePretas(ct.getTempoRestantePretas())
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
                            resumoBuilder.tempoInicialBrancas(ct.getTempoInicialBrancas())
                                    .tempoInicialPretas(ct.getTempoInicialPretas())
                                    .incrementoBrancas(ct.getIncrementoBrancas())
                                    .incrementoPretas(ct.getIncrementoPretas());
                        }

                        return resumoBuilder.build();
                    })
                    .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                    .collect(Collectors.toList());
            builder.historico(historico);
        }

        return builder.build();
    }
}
