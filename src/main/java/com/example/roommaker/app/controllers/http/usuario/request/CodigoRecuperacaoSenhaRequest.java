package com.example.roommaker.app.controllers.http.usuario.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodigoRecuperacaoSenhaRequest extends CodigoPorEmailRequest {
    @NotBlank
    private String password;
}
