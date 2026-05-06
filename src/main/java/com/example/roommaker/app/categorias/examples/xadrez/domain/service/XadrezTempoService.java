package com.example.roommaker.app.categorias.examples.xadrez.domain.service;

import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezLogica;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.*;
import com.github.bhlangonijr.chesslib.Board;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pela lógica de tempo no xadrez.
 * 
 * PRINCÍPIOS:
 * - Não usa polling ou loops
 * - Cálculo de tempo é sob demanda (lazy)
 * - Verificação de timeout ocorre apenas em eventos
 * - Stateless: não mantém estado em memória
 */
@Service
@Slf4j
public class XadrezTempoService {

    /**
     * Cria um controle de tempo a partir de configurações em segundos.
     * Converte para milissegundos internamente.
     * 
     * @param tempoInicialBrancasSegundos tempo inicial das brancas em segundos
     *                                    (null = infinito)
     * @param incrementoBrancasSegundos   incremento das brancas em segundos
     * @param tempoInicialPretasSegundos  tempo inicial das pretas em segundos (null
     *                                    = infinito)
     * @param incrementoPretasSegundos    incremento das pretas em segundos
     * @return controle de tempo configurado
     */
    public ControleTempoXadrez criarControleTempo(
            Integer tempoInicialBrancasSegundos,
            Integer incrementoBrancasSegundos,
            Integer tempoInicialPretasSegundos,
            Integer incrementoPretasSegundos) {

        if (tempoInicialBrancasSegundos == null && tempoInicialPretasSegundos == null) {
            return null; // Tempo infinito
        }

        return ControleTempoXadrez.builder()
                .tempoInicialBrancas(tempoInicialBrancasSegundos != null ? tempoInicialBrancasSegundos * 1000L : null)
                .tempoInicialPretas(tempoInicialPretasSegundos != null ? tempoInicialPretasSegundos * 1000L : null)
                .incrementoBrancas(incrementoBrancasSegundos != null ? incrementoBrancasSegundos * 1000L : 0)
                .incrementoPretas(incrementoPretasSegundos != null ? incrementoPretasSegundos * 1000L : 0)
                .build();
    }

    /**
     * Verifica se o tempo de um jogador esgotou e determina o resultado.
     * 
     * @param partida    partida atual
     * @param salaXadrez sala de xadrez
     * @return resultado do timeout, ou null se o tempo não esgotou
     */
    public ResultadoTimeout verificarTimeout(PartidaXadrez partida, SalaXadrez salaXadrez) {
        ControleTempoXadrez ct = partida.getControleTempo();

        if (ct == null || ct.tempoInfinito()) {
            return null;
        }

        boolean vezBrancas = partida.vezDasBrancas();

        if (!ct.tempoEsgotado(vezBrancas)) {
            return null;
        }

        log.info("Tempo esgotado para {} na partida {} da sala {}/{}",
                vezBrancas ? "brancas" : "pretas",
                partida.getId(),
                salaXadrez.getUsernameDono(),
                salaXadrez.getNomeSala());

        // Verifica se o oponente tem material suficiente para dar mate
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

        return new ResultadoTimeout(resultado, motivo);
    }

    /**
     * Processa o tempo após um lance.
     * 
     * IMPORTANTE: Deve ser chamado ANTES de trocar a vez.
     * 
     * @param partida    partida atual
     * @param vezBrancas true se foi a vez das brancas (quem acabou de jogar)
     */
    public void processarAposLance(PartidaXadrez partida, boolean vezBrancas) {
        ControleTempoXadrez ct = partida.getControleTempo();

        if (ct == null || ct.tempoInfinito()) {
            return;
        }

        ct.atualizarAposLance(vezBrancas);

        log.debug("Tempo atualizado após lance. Brancas: {}s, Pretas: {}s",
                ct.getTempoRestanteBrancasSegundos(),
                ct.getTempoRestantePretasSegundos());
    }

    /**
     * Congela o tempo quando a partida termina.
     * 
     * @param partida partida que terminou
     */
    public void congelarTempo(PartidaXadrez partida) {
        ControleTempoXadrez ct = partida.getControleTempo();

        if (ct == null || ct.tempoInfinito()) {
            return;
        }

        ct.congelar();

        log.debug("Tempo congelado. Brancas: {}s, Pretas: {}s",
                ct.getTempoRestanteBrancasSegundos(),
                ct.getTempoRestantePretasSegundos());
    }

    /**
     * Resultado de uma verificação de timeout.
     */
    public record ResultadoTimeout(ResultadoXadrez resultado, MotivoXadrez motivo) {
    }
}
