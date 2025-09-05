package com.example.roommaker.app.controllers.http.usuario.request;

import com.example.roommaker.app.domain.models.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioParaAlterarRequest {
    private String password;
    private String descricao;
    private String email;
    private Boolean doisFatores;
    public Usuario toDomain() {
        return Usuario.builder()
                .password(password)
                .descricao(descricao)
                .email(email)
                .doisFatores(doisFatores)
                .build();
    }
}
