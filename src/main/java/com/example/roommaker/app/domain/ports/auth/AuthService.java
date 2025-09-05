package com.example.roommaker.app.domain.ports.auth;

import com.example.roommaker.app.domain.models.Usuario;

public interface AuthService {
    void matches(CharSequence rawPassword, String encodedPassword);
    String encode(String password);
    void sendVerificationCode(String email, String username);
    String generateToken(String subject);
    String getUsername(String token);
    String validarCodigo(String username, String codigo);
    void sendEsqueciSenhaCode(Usuario usuario);


    String alterarSenha(Usuario usuarioEncontrado, String codigo);
}
