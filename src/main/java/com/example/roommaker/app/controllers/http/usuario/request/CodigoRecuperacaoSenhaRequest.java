package com.example.roommaker.app.controllers.http.usuario.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(min = 3, max = 64)
    private String password;
}
