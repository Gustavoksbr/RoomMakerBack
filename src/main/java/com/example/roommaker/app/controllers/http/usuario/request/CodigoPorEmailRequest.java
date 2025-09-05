package com.example.roommaker.app.controllers.http.usuario.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodigoPorEmailRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String codigo;

}
