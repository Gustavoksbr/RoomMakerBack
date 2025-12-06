package com.example.roommaker.app.controllers.http.handler;


import com.example.roommaker.app.domain.exceptions.*;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class HttpExceptionController {
    public HttpExceptionController() {
    }
    // ====================================================================================================
    // 400, 404
    // geral


    @ExceptionHandler(ErroDeRequisicaoGeral.class)
    public ResponseEntity<String> handleErroDeRequisicaoGeral(ErroDeRequisicaoGeral ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({NoHandlerFoundException.class})
    public ResponseEntity<String> NoResourceFoundException(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro 404: URL não encontrada.");
    }

    // provavelmente erro no corpo da requisição com spring validation

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder erros = new StringBuilder();

        // Extrai os erros de campo e os adiciona à string
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            if(errorMessage!=null && errorMessage.equals("deve corresponder a \"^[a-zA-Z0-9]+$\"")) {
                errorMessage = "deve conter apenas letras e números.";
            }
            erros.append("Campo '").append(fieldName).append("': ").append(errorMessage).append(". ");
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(erros.toString().trim());
    }

    // erro explícito no corpo da requisição
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Corpo da requisição inválido.");
    }

    // ====================================================================================================
    // 403
    // nao necessariamente sobre jwt ou login

    @ExceptionHandler(UsuarioNaoAutorizado.class)
    public ResponseEntity<String> handleUsuarioNaoAutorizado(UsuarioNaoAutorizado ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(Erro409.class)
    public ResponseEntity<String> handleErro409(Erro409 ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }




    @ExceptionHandler(UsernameAlreadyExistsException.class) //409
    public ResponseEntity<String> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(UsuarioNaoEncontrado.class) //404
    public ResponseEntity<String> handleUsuarioNaoEncontrado(UsuarioNaoEncontrado ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //sala
    @ExceptionHandler(SalaJaExiste.class) //409
    public ResponseEntity<String> handleSalaJaExiste(SalaJaExiste ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(SalaNaoEncontrada.class)
    public ResponseEntity<String> handleSalaNaoEncontrada(SalaNaoEncontrada ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    // ====================================================================================================
    //401
    // necessariamente sobre jwt ou login

    @ExceptionHandler(SenhaIncorretaException.class)
    public ResponseEntity<String> handleSenhaIncorretaException(SenhaIncorretaException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(ExpiredJwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erro 401: Token expirado. Logue-se novamente.");
    }

    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<String> handleJwtValidationException(JwtValidationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erro 401: Token expirado. Logue-se novamente.");
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<String> handleMalformedJwtException(MalformedJwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erro 401: Token inválido. Logue-se novamente.");
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<String> handleSignatureException(SignatureException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erro 401: Token ausente ou assinatura inválida. Logue-se novamente.");
    }
    @ExceptionHandler(BadJwtException.class)
    public ResponseEntity<String> handleBadJwtException(BadJwtException ex) {
        if(ex.getMessage().startsWith("An error occurred while attempting to decode the Jwt: Jwt expired at")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erro 401: Token expirado. Logue-se novamente.");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erro 401: "+ex.getMessage());
    }
    @ExceptionHandler(ErroDeAutenticacaoGeral.class)
    public ResponseEntity<String> handleErroDeAutenticacaoGeral(ErroDeAutenticacaoGeral ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erro 401: "+ex.getMessage());
    }

    @ExceptionHandler({ io.jsonwebtoken.JwtException.class })
    public ResponseEntity<String> handleInvalidJwtKey(io.jsonwebtoken.JwtException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Erro 401: Token inválido ou chave incompatível.");
    }
    @ExceptionHandler
    public ResponseEntity<String> handleTeapotException(com.example.roommaker.app.domain.exceptions.TeapotException ex) {
        return ResponseEntity.status(418).body(ex.getMessage());
    }

    // ====================================================================================================
    // 500
    @ExceptionHandler
    public ResponseEntity<String> handleException(Exception ex) {
        System.out.println("Erro 500\n Excecao:"+ex.getClass()+"\nMensagem:"+ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno no servidor.");
    }
}