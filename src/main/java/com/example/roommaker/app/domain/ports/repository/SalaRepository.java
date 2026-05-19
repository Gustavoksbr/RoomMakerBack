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

    /** Exclui a sala e retorna seus dados (evita double-find no manager). */
    Sala excluirSalaERetornar(String usernameDono, String nomeSala);

    void excluirSala(String usernameDono, String nomeSala);

    Sala sairDaSala(String usernameDono, String nomeSala, String usernameSaindo);

    /** Salva uma sala já carregada em memória (evita double-find em alterações). */
    Sala salvarSala(Sala sala);

    Sala alterarCapacidade(String usernameDono, String nomeSala, Long novaCapacidade);

    String verSenha(String usernameDono, String nomeSala);

    Sala alterarSenha(String usernameDono, String nomeSala, String novaSenha);
}
