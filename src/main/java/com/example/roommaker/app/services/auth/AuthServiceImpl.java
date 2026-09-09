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
        
        String emailBody = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2 style="color: #333;">Autenticação de Dois Fatores - RoomMaker</h2>
                <p>Olá, <strong>%s</strong>!</p>
                <p>Use o código abaixo para completar seu login:</p>
                <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0;">
                    <h1 style="margin: 10px 0; font-size: 32px; color: #28a745; letter-spacing: 5px;">%s</h1>
                </div>
                <p style="color: #666; font-size: 14px;">Este código é válido apenas para esta sessão. Se você não tentou fazer login, altere sua senha imediatamente.</p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                <p style="color: #999; font-size: 12px;">RoomMaker - Plataforma de Salas Colaborativas</p>
            </div>
            """, username, verificationCode);
        
        this.emailService.sendEmail(Email.builder()
                .to(email)
                .subject("Código de Autenticação - RoomMaker")
                .body(emailBody)
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
        
        String emailBody = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2 style="color: #333;">Recuperação de Senha - RoomMaker</h2>
                <p>Olá, <strong>%s</strong>!</p>
                <p>Você solicitou a recuperação de senha para sua conta.</p>
                <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0;">
                    <p style="margin: 0; font-size: 14px; color: #666;">Seu código de recuperação é:</p>
                    <h1 style="margin: 10px 0; font-size: 32px; color: #007bff; letter-spacing: 5px;">%s</h1>
                </div>
                <p><strong>Username:</strong> %s</p>
                <p style="color: #666; font-size: 14px;">Este código é válido apenas para esta sessão. Se você não solicitou a recuperação de senha, ignore este e-mail.</p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                <p style="color: #999; font-size: 12px;">RoomMaker - Plataforma de Salas Colaborativas</p>
            </div>
            """, usuario.getUsername(), esqueciSenhaCode, usuario.getUsername());
        
        this.emailService.sendEmail(Email.builder()
                .to(usuario.getEmail())
                .subject("Recuperação de Senha - RoomMaker")
                .body(emailBody)
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
