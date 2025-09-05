package com.example.roommaker.app.domain.ports.repository;

import com.example.roommaker.app.domain.models.Sala;

import java.util.List;

public interface SalaRepository {
    List<Sala> listar(String usernameDono, String nomeSala, String categoria);
    List<Sala> listarPorParticipante(String usernameParticipante);
    List<Sala> listarPorDono(String usernameDono);
    Sala criar(Sala sala);
    Sala mostrarSala(String nomeSala, String usernameDono);
    Sala adicionarParticipante(String nomesala, String usernameDono, String senha, String usernameParticipante);
    Sala verificarSeUsuarioEstaNaSalaERetornarSala(String nomeSala, String usernameDono, String usernameParticipante);
    void excluirSala(String usernameDono, String nomeSala);
    Sala sairDaSala(String usernameDono, String nomeSala, String usernameSaindo);
}
