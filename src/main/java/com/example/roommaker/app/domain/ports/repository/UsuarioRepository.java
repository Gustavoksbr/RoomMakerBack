package com.example.roommaker.app.domain.ports.repository;

import com.example.roommaker.app.domain.models.Usuario;

import java.time.LocalDate;
import java.util.List;

public interface UsuarioRepository {
    List<Usuario> listar();

    List<Usuario> listarComSubstring(String substring);

    void criar(Usuario usuario) throws InterruptedException;

    Usuario editarDescricao(Usuario usuario);

    void deletar(String username); // ainda nao implementado

    Usuario encontrarUsernameDeOutroUsuario(String username);

    void alterarDoisFatores(Usuario usuario);

    void validarNovoUsuario(Usuario usuario);

    Boolean existePorUsername(String username);
    Boolean existePorEmail(String email);

    Usuario encontrarUsernameDoUsuarioAtual(String username);

    Usuario encontrarPorEmail(String email);

    void alterarSenha(Usuario usuario);

    LocalDate getDataNascimento(String username);
}
