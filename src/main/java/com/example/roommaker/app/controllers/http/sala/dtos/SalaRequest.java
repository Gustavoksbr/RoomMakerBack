package com.example.roommaker.app.controllers.http.sala.dtos;

import com.example.roommaker.app.domain.models.Sala;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalaRequest {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    @Size(max = 15)
    private String nome;
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    private String categoria;
    private String senha;
    @Min(2)
    @Max(1000)
    private Long qtdCapacidade;
    public Sala toDomain() {
        return Sala.builder()
                .nome(this.nome)
                .usernameDono(null)
                .categoria(this.categoria)
                .senha(this.senha)
                .qtdCapacidade(this.qtdCapacidade)
                .usernameParticipantes(null)
                .build();
    }
}
