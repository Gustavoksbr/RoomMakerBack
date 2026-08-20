package com.example.roommaker.app.categorias.examples.xadrez.controller.websocket;

import com.example.roommaker.app.categorias.examples.xadrez.controller.request.*;
import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezManager;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.PreLance;
import com.example.roommaker.app.domain.thread.Contexto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

// Todos os endpoints seguem o padrão: /sala/{usernameDono}/{nomeSala}/{username}/xadrez/...
@Controller
public class XadrezWebsocketController {

    private final XadrezManager xadrezManager;

    @Autowired
    public XadrezWebsocketController(XadrezManager xadrezManager) {
        this.xadrezManager = xadrezManager;
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/configurar")
    public void configurar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username,
            @Payload XadrezConfigurarRequest request) {
        Contexto.setUsername(username);
        xadrezManager.configurar(nomeSala, usernameDono, username,
                request.getUsernameBrancas(), request.getUsernamePretas(), request.getNotacao(),
                request.getModoVisual());
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/iniciar")
    public void iniciar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username) {
        Contexto.setUsername(username);
        xadrezManager.iniciarPartida(nomeSala, usernameDono, username);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/configurar-e-iniciar")
    public void configurarEIniciar(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username,
            @Payload XadrezConfigurarRequest request) {
        Contexto.setUsername(username);
        xadrezManager.configurarEIniciar(nomeSala, usernameDono, username,
                request.getUsernameBrancas(), request.getUsernamePretas(), request.getNotacao(),
                request.calcularTempoTotalBrancas(), request.getIncrementoBrancasSegundos(),
                request.calcularTempoTotalPretas(), request.getIncrementoPretasSegundos(), request.getModoVisual());
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/lance")
    public void lance(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username,
            @Payload XadrezLanceRequest request) {
        Contexto.setUsername(username);
        if (request.temCoordenadas()) {
            xadrezManager.jogarCoordenadas(nomeSala, usernameDono, username,
                    request.getFrom(), request.getTo(), request.getPromocao());
        } else {
            xadrezManager.jogar(nomeSala, usernameDono, username, request.getSan());
        }
    }

    /**
     * Substitui a fila de pré-lances do jogador. Ver
     * {@link com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezManager#definirPreLances}.
     */
    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/pre-lances")
    public void preLances(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username,
            @Payload XadrezPreLancesRequest request) {
        Contexto.setUsername(username);
        xadrezManager.definirPreLances(nomeSala, usernameDono, username, converter(request));
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/cancelar-pre-lances")
    public void cancelarPreLances(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username) {
        Contexto.setUsername(username);
        xadrezManager.limparPreLances(nomeSala, usernameDono, username);
    }

    private List<PreLance> converter(XadrezPreLancesRequest request) {
        if (request == null || request.getFila() == null)
            return List.of();
        // Sem normalizar aqui de propósito: quem valida coordenada é o manager,
        // que é quem também recusa a requisição inteira se alguma vier torta.
        return request.getFila().stream()
                .map(dto -> dto == null ? null : new PreLance(dto.getFrom(), dto.getTo(), dto.getPromocao()))
                .collect(Collectors.toList());
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/desistir")
    public void desistir(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username) {
        Contexto.setUsername(username);
        xadrezManager.desistir(nomeSala, usernameDono, username);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/propor-empate")
    public void proporEmpate(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username) {
        Contexto.setUsername(username);
        xadrezManager.proporEmpate(nomeSala, usernameDono, username);
    }

    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/xadrez/responder-empate")
    public void responderEmpate(
            @DestinationVariable String usernameDono,
            @DestinationVariable String nomeSala,
            @DestinationVariable String username,
            @Payload XadrezResponderEmpateRequest request) {
        Contexto.setUsername(username);
        xadrezManager.responderEmpate(nomeSala, usernameDono, username, Boolean.TRUE.equals(request.getAceitar()));
    }
}
