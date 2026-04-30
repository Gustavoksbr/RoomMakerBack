package com.example.roommaker.app.controllers.http.usuario.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlterarUsernameRequest {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username deve conter apenas letras e números")
    @Size(min = 3, max = 20, message = "Username deve ter entre 3 e 20 caracteres")
    private String novoUsername;
}
