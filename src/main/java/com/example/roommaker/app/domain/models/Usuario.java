package com.example.roommaker.app.domain.models;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    private String username;
    private String password;
    private String descricao;
    private String email;
    private Boolean ativo;
    private Boolean doisFatores;
    private LocalDate dataNascimento;
}