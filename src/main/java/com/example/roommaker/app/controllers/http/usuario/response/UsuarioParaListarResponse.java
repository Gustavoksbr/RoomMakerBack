package com.example.roommaker.app.controllers.http.usuario.response;

import com.example.roommaker.app.domain.models.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioParaListarResponse {
    private String descricao;
    private String username;
    private String email;

    public UsuarioParaListarResponse(Usuario usuario) {
        this.descricao = usuario.getDescricao();
        this.username = usuario.getUsername();
        this.email = usuario.getEmail();
    }
}
