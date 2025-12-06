package com.example.roommaker.app.categorias.examples.jokenpo.domain.model;
import com.example.roommaker.app.domain.models.Sala;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class JokenpoSala extends Sala {
    private String nomeSala;
    private String usernameDono;
    private String usernameOponente;
    private Jokenpo jogoAtual;
    private List<Jokenpo> historico;
}