package com.example.roommaker.app.controllers.http.usuario.request;

import com.example.roommaker.app.domain.models.Usuario;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioParaCriarRequest {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    @Size(max = 15)

    private String username;
    @NotBlank
    @Size(min = 3, max = 64)
    private String password;
    private String descricao;
    @NotBlank
    @Email
    private String email;

    private LocalDate dataNascimento;
    public Usuario toDomain(){
        return Usuario.builder()
                .username(username)
                .password(password)
                .descricao(descricao)
                .email(email)
                .ativo(true)
                .doisFatores(false)
                .dataNascimento(dataNascimento)
                .build();
    }
}
