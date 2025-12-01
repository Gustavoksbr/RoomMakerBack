package com.example.roommaker.app.controllers.websocket.handler;

import com.example.roommaker.app.domain.exceptions.*;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;
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
    @MessageExceptionHandler(ErroDeRequisicaoGeral.class)
    public ErrorMensagem handleErroDeRequisicaoGeral(ErroDeRequisicaoGeral ex) {
        return new ErrorMensagem("mamamia: "+ex.getMessage(),"400") ;
    }

    @MessageExceptionHandler(UsuarioNaoAutorizado.class)
    public void handleUsuarioNaoAutorizado(UsuarioNaoAutorizado ex) {
        String username = Contexto.getUsername();
        if (username != null) {
            this.messagingTemplate.convertAndSend("/topic/" + username, ex);
        }
    }


    @MessageExceptionHandler(MessageConversionException.class)
    public String handleMessageConversionException(MessageConversionException ex) {
        return ex.getMessage();
    }

    @MessageExceptionHandler(UsernameAlreadyExistsException.class)
    public String handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex) {
        return ex.getMessage();
    }

    @MessageExceptionHandler(UsuarioNaoEncontrado.class)
    public String handleUsuarioNaoEncontrado(UsuarioNaoEncontrado ex) {
        return ex.getMessage();
    }

    @MessageExceptionHandler(SalaJaExiste.class)
    public String handleSalaJaExiste(SalaJaExiste ex) {
        return ex.getMessage();
    }

    @MessageExceptionHandler(SalaNaoEncontrada.class)
    public String handleSalaNaoEncontrada(SalaNaoEncontrada ex) {
        return ex.getMessage();
    }

    @MessageExceptionHandler(SenhaIncorretaException.class)
    public String handleSenhaIncorretaException(SenhaIncorretaException ex) {
        return ex.getMessage();
    }

    @MessageExceptionHandler(ExpiredJwtException.class)
    public String handleExpiredJwtException(ExpiredJwtException ex) {
        return "Erro 401: Token expirado.";
    }

    @MessageExceptionHandler(MalformedJwtException.class)
    public String handleMalformedJwtException(MalformedJwtException ex) {
        return "Erro 401: Token inválido.";
    }

    @MessageExceptionHandler(SignatureException.class)
    public String handleSignatureException(SignatureException ex) {
        return "Erro 401: Token ausente ou assinatura inválida.";
    }

    @MessageExceptionHandler(BadJwtException.class)
    public String handleBadJwtException(BadJwtException ex) {
        return "Erro 401: " + ex.getMessage();
    }

    @MessageExceptionHandler(ErroDeAutenticacaoGeral.class)
    public String handleErroDeAutenticacaoGeral(ErroDeAutenticacaoGeral ex) {
        return "Erro 401: " + ex.getMessage();
    }

    @MessageExceptionHandler({ io.jsonwebtoken.JwtException.class })
    public String handleJwtException(io.jsonwebtoken.JwtException ex) {
        return "Erro 401: " + ex.getMessage();
    }



}