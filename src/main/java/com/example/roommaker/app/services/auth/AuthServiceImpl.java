package com.example.roommaker.app.services.auth;

import com.example.roommaker.app.domain.models.Email;
import com.example.roommaker.app.domain.models.Usuario;
import com.example.roommaker.app.domain.ports.auth.AuthService;
import com.example.roommaker.app.domain.ports.email.EmailService;
import com.example.roommaker.app.domain.ports.auth.JwtService;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.exceptions.SenhaIncorretaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Random;


@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final HashMap<String, String> verificationCodes = new HashMap<>();

    private final HashMap<String, String> esqueciSenhaCodes = new HashMap<>();

    // metodos PasswordEncoder
    @Override
    public void matches(CharSequence rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)){
            throw new SenhaIncorretaException();
        }
    }

    @Override
    public String encode(String password) {
        return passwordEncoder.encode(password);
    }

    // metodos JwtService
    @Override
    public String generateToken(String subject) {
        return jwtService.generateToken(subject);
    }

    @Override
    public String getUsername(String token) {

        return jwtService.getUsername(token);
    }

    // metodos EmailService
    @Override
    public void sendVerificationCode(String email, String username) {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        String verificationCode = String.valueOf(code);
        this.verificationCodes.put(username, verificationCode);
        this.emailService.sendEmail(Email.builder()
                .to(email)
                .subject("Código de autenticação")
                .body("Código de autenticacao: " + verificationCode)
                .build());
    }

    public String validarCodigo(String username, String code) {
        String verificationCode = this.verificationCodes.get(username);
        if(verificationCode == null) {
            throw new ErroDeRequisicaoGeral("Código de autenticação não encontrado (ou expirado). Solicite um novo código.");
        }
       if(verificationCode.equals(code)) {
           this.verificationCodes.remove(username);
           return this.generateToken(username);
       } else {
           throw new ErroDeRequisicaoGeral("Código incorreto");
       }
    }

    @Override
    public void sendEsqueciSenhaCode(Usuario usuario) {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        String esqueciSenhaCode = String.valueOf(code);
        this.esqueciSenhaCodes.put(usuario.getEmail(), esqueciSenhaCode);
        this.emailService.sendEmail(Email.builder()
                .to(usuario.getEmail())
                .subject("Código de recuperação de senha")
                .body("Código de recuperação de senha: " + esqueciSenhaCode+".\n Seu username é: "+usuario.getUsername()+".")
                .build());
    }

    @Override
    public String alterarSenha(Usuario usuarioEncontrado, String codigo) {
        String esqueciSenhaCode = this.esqueciSenhaCodes.get(usuarioEncontrado.getEmail());
        if(esqueciSenhaCode == null) {
            throw new ErroDeRequisicaoGeral("Código de recuperação de senha não encontrado (ou expirado). Solicite um novo código.");
        }
        if(esqueciSenhaCode.equals(codigo)) {
            this.esqueciSenhaCodes.remove(usuarioEncontrado.getEmail());
            return this.generateToken(usuarioEncontrado.getUsername());
        } else {
            throw new ErroDeRequisicaoGeral("Código incorreto");
        }
    }
}
