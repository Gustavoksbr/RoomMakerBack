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
    }
}
