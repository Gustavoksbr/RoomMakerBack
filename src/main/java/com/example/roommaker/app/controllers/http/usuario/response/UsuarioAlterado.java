package com.example.roommaker.app.controllers.http.usuario.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAlterado {
    private String descricao;
    private String email;
    private Boolean doisFatores;
}
