package com.example.roommaker.app.categorias.examples;

import com.example.roommaker.app.domain.models.Sala;

public interface JogoPort {
    void saidaDeParticipante(String usernameParticipante, Sala sala);
    void validarSalaParaOJogo(Sala sala);
    void deletarJogo(Sala sala);
}
