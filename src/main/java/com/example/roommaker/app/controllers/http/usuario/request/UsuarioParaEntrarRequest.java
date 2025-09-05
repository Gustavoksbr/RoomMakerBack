package com.example.roommaker.app.controllers.http.usuario.request;

import com.example.roommaker.app.domain.models.Usuario;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioParaEntrarRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    public Usuario toDomain(){
        return Usuario.builder()
                .username(username)
                .password(password)
                .descricao(null)
                .email(null)
                .ativo(null)
                .doisFatores(null)
                .build();
    }
}
