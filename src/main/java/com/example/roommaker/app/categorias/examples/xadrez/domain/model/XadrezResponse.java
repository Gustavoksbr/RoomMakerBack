package com.example.roommaker.app.categorias.examples.xadrez.domain.model;

import lombok.*;

import java.util.List;

/**
 * Resposta enviada via WebSocket para os jogadores da sala de xadrez.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class XadrezResponse {

    // --- configuração da sala ---
    private String usernameBrancas;
    private String usernamePretas;
    private NotacaoXadrez notacao;
    private Boolean modoVisual; // true = tabuleiro visual, false = às cegas

    // --- estado da partida atual ---
    private Boolean partidaEmAndamento;
    private Long partidaId;
    private List<String> lances; // SAN canônica acumulada
    private String resultado; // EM_ANDAMENTO, VITORIA_BRANCAS, etc.
    private String motivo; // XEQUE_MATE, DESISTENCIA, etc.
    private String propostaEmpate; // "BRANCAS", "PRETAS" ou null
    private Integer lancesIlegaisBrancas;
    private Integer lancesIlegaisPretas;
    private Boolean vezDasBrancas;

    // --- pré-lances ---
    /**
     * A fila de pré-lances de QUEM RECEBE esta resposta, nunca a do adversário:
     * saber que o outro pré-lançou (e para onde) é informação de jogo que nem o
     * chess.com nem o lichess entregam.
     */
    private List<PreLance> meusPreLances;

    /**
     * true quando a fila de quem recebe acabou de ser descartada por o primeiro
     * pré-lance ter caído numa posição em que era ilegal. Não é penalidade —
     * serve só para o tabuleiro apagar as setas e avisar.
     */
    private Boolean preLancesCancelados;

    // --- controle de tempo ---
    private Integer tempoInicialBrancas; // segundos (null = infinito)
    private Integer tempoInicialPretas; // segundos (null = infinito)
    private Integer incrementoBrancas; // segundos
    private Integer incrementoPretas; // segundos
    private Integer tempoRestanteBrancas; // segundos
    private Integer tempoRestantePretas; // segundos
    private Long timestampUltimoLance; // milissegundos

    // --- tipo de evento (para o frontend saber o que aconteceu) ---
    private String evento; // LANCE, LANCE_ILEGAL, NOTACAO_INVALIDA, DESISTENCIA, EMPATE_PROPOSTO,
                           // EMPATE_ACEITO, EMPATE_RECUSADO, FIM, CONFIGURACAO_ALTERADA, PARTIDA_INICIADA

    // --- histórico do usuário que recebe (apenas partidas encerradas dele) ---
    private List<PartidaXadrezResumo> historico;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PartidaXadrezResumo {
        private Long id;
        private String pgn;
        private List<String> lances;
        private String resultado;
        private String motivo;
        private Integer lancesIlegaisBrancas;
        private Integer lancesIlegaisPretas;
        private String usernameBrancas;
        private String usernamePretas;
        private NotacaoXadrez notacao;
        private Boolean modoVisual; // Para histórico

        // Informações de tempo
        private Integer tempoInicialBrancas; // segundos (null = infinito)
        private Integer tempoInicialPretas; // segundos (null = infinito)
        private Integer incrementoBrancas; // segundos
        private Integer incrementoPretas; // segundos
    }
}
