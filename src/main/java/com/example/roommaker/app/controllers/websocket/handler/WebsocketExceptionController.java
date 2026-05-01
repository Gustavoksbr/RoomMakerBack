package com.example.roommaker.app.controllers.websocket.handler;

import com.example.roommaker.app.domain.exceptions.*;
import com.example.roommaker.app.domain.thread.Contexto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class WebsocketExceptionController {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebsocketExceptionController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // envia ErrorMensagem para o tópico pessoal do usuário: /topic/{username}/erro
    private void enviarErro(String mensagem, String status) {
        String username = Contexto.getUsername();
        if (username != null) {
            this.messagingTemplate.convertAndSend(
                    "/topic/" + username + "/erro",
                    new ErrorMensagem(mensagem, status));
        }
    }

    @MessageExceptionHandler(ErroDeRequisicaoGeral.class)
    public void handleErroDeRequisicaoGeral(ErroDeRequisicaoGeral ex) {
        enviarErro(ex.getMessage(), "400");
    }

    @MessageExceptionHandler(UsuarioNaoAutorizado.class)
    public void handleUsuarioNaoAutorizado(UsuarioNaoAutorizado ex) {
        enviarErro(ex.getMessage(), "403");
    }

    @MessageExceptionHandler(MessageConversionException.class)
    public void handleMessageConversionException(MessageConversionException ex) {
        enviarErro("Corpo da mensagem inválido.", "400");
    }

    @MessageExceptionHandler(UsernameAlreadyExistsException.class)
    public void handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex) {
        enviarErro(ex.getMessage(), "409");
    }

    @MessageExceptionHandler(UsuarioNaoEncontrado.class)
    public void handleUsuarioNaoEncontrado(UsuarioNaoEncontrado ex) {
        enviarErro(ex.getMessage(), "404");
    }

    @MessageExceptionHandler(SalaJaExiste.class)
    public void handleSalaJaExiste(SalaJaExiste ex) {
        enviarErro(ex.getMessage(), "409");
    }

    @MessageExceptionHandler(SalaNaoEncontrada.class)
    public void handleSalaNaoEncontrada(SalaNaoEncontrada ex) {
        enviarErro(ex.getMessage(), "404");
    }

    @MessageExceptionHandler(SenhaIncorretaException.class)
    public void handleSenhaIncorretaException(SenhaIncorretaException ex) {
        enviarErro(ex.getMessage(), "401");
    }

    @MessageExceptionHandler(ExpiredJwtException.class)
    public void handleExpiredJwtException(ExpiredJwtException ex) {
        enviarErro("Token expirado.", "401");
    }

    @MessageExceptionHandler(MalformedJwtException.class)
    public void handleMalformedJwtException(MalformedJwtException ex) {
        enviarErro("Token inválido.", "401");
    }

    @MessageExceptionHandler(SignatureException.class)
    public void handleSignatureException(SignatureException ex) {
        enviarErro("Token ausente ou assinatura inválida.", "401");
    }

    @MessageExceptionHandler(BadJwtException.class)
    public void handleBadJwtException(BadJwtException ex) {
        enviarErro(ex.getMessage(), "401");
    }

    @MessageExceptionHandler(ErroDeAutenticacaoGeral.class)
    public void handleErroDeAutenticacaoGeral(ErroDeAutenticacaoGeral ex) {
        enviarErro(ex.getMessage(), "401");
    }

    @MessageExceptionHandler(io.jsonwebtoken.JwtException.class)
    public void handleJwtException(io.jsonwebtoken.JwtException ex) {
        enviarErro(ex.getMessage(), "401");
    }
}
