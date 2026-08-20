package com.example.roommaker.app.categorias.examples.xadrez.domain.service;

import com.example.roommaker.app.categorias.examples.xadrez.domain.NotacaoConverter;
import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezLogica;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.*;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Consome as filas de pré-lances de uma partida.
 *
 * Vive separado do XadrezManager porque é a parte da regra que mais tem casos de
 * borda (cadeia entre os dois lados, pré-lance que virou ilegal, pré-lance que dá
 * mate, relógio) e nenhuma delas depende de banco, WebSocket ou sala — dá para
 * testar cada uma com um tabuleiro e uma partida na memória.
 */
@Service
@Slf4j
public class XadrezPreLanceService {

    private final XadrezTempoService tempoService;

    public XadrezPreLanceService(XadrezTempoService tempoService) {
        this.tempoService = tempoService;
    }

    /**
     * O que sobrou depois de rodar a cadeia.
     *
     * @param quantidadeAplicada quantos pré-lances entraram no tabuleiro.
     * @param ladoCanceladoBrancas o lado que teve a fila descartada por o próximo
     *                             pré-lance ter caído ilegal, ou null se nenhum.
     * @param fim                  o desfecho, se a cadeia terminou a partida.
     */
    public record Resultado(int quantidadeAplicada, Boolean ladoCanceladoBrancas,
            XadrezLogica.ResultadoFim fim) {

        public boolean encerrouPartida() {
            return fim != null;
        }
    }

    /**
     * Aplica pré-lances enquanto o primeiro da fila da vez for legal na posição
     * real.
     *
     * O laço troca de lado sozinho: um pré-lance das pretas passa a vez para as
     * brancas, cuja fila pode estar cheia também. É assim que se comporta um final
     * de bullet no chess.com em que os dois lados estão pré-lançando — uma rajada
     * de lances sai de uma vez só. Termina sempre: cada volta ou sai do laço ou
     * consome um item de uma fila finita.
     *
     * IMPORTANTE: o {@code board} é modificado no lugar e, ao final, reflete a
     * posição resultante.
     *
     * @param cobrarTempoDoPrimeiro cobra o tempo decorrido no primeiro lance da
     *                              cadeia. É o caso em que a fila chegou quando já
     *                              era a vez do jogador: ele estava no relógio, e
     *                              não seria pré-lance nenhum se fosse de graça.
     */
    public Resultado aplicarCadeia(PartidaXadrez partida, Board board, NotacaoXadrez notacao,
            boolean cobrarTempoDoPrimeiro) {
        int aplicados = 0;

        while (true) {
            boolean vezBrancas = partida.vezDasBrancas();
            List<PreLance> fila = partida.preLancesDe(vezBrancas);
            if (fila.isEmpty()) {
                return new Resultado(aplicados, null, null);
            }

            PreLance proximo = fila.get(0);
            Move move = XadrezLogica.resolverPorCoordenadas(board,
                    proximo.getFrom(), proximo.getTo(), proximo.getPromocao());

            if (move == null) {
                // Descarta a fila INTEIRA, não só a cabeça: os pré-lances seguintes
                // foram pensados a partir de uma posição que nunca chegou a existir.
                // Tentar o próximo seria jogar dados. Não conta como lance ilegal —
                // o jogador não errou, a posição é que mudou.
                partida.limparPreLances(vezBrancas);
                log.debug("Pré-lance {} descartado: ilegal na posição atual", proximo.toUci());
                return new Resultado(aplicados, vezBrancas, null);
            }

            fila.remove(0);
            aplicarNoTabuleiro(partida, board, move, notacao);

            if (aplicados == 0 && cobrarTempoDoPrimeiro) {
                tempoService.processarAposLance(partida, vezBrancas);
            } else {
                tempoService.processarAposPreLance(partida, vezBrancas);
            }
            aplicados++;

            XadrezLogica.ResultadoFim fim = XadrezLogica.verificarFim(board);
            if (fim != null) {
                partida.encerrar(fim.resultado(), fim.motivo());
                tempoService.congelarTempo(partida);
                return new Resultado(aplicados, null, fim);
            }
        }
    }

    /** Anota o lance em SAN na notação da sala e o executa no tabuleiro. */
    private void aplicarNoTabuleiro(PartidaXadrez partida, Board board, Move move, NotacaoXadrez notacao) {
        String sanCanonica = XadrezLogica.sanCanonica(board, move, partida.getLances(), notacao);
        board.doMove(move);
        partida.getLances().add(NotacaoConverter.deIngles(sanCanonica, notacao));
        partida.setPropostaEmpate(null);
    }
}
