package com.example.roommaker.app.domain.ports.repository;

import com.example.roommaker.app.domain.models.Usuario;

import java.time.LocalDate;
import java.util.List;

public interface UsuarioRepository {
    List<Usuario> listar();

    List<Usuario> listarComSubstring(String substring);

    void criar(Usuario usuario);

    Usuario encontrarUsernameDeOutroUsuario(String username);

    void alterarDoisFatores(Usuario usuario);

    Boolean existePorUsername(String username);

    Usuario encontrarUsernameDoUsuarioAtual(String username);

    Usuario encontrarPorEmail(String email);

    void alterarSenha(Usuario usuario);

    LocalDate getDataNascimento(String username);

    // [CANCELADO] alterarUsername - username é usado como chave em todas as
    // coleções do MongoDB,
    // atualizar exigiria cascade update manual em salas, chats, jogos, etc.
    // void alterarUsername(String usernameAtual, String novoUsername);

    // futuras ideias

    // Usuario editarDescricao(Usuario usuario);
    // void deletar(String username);
    // Boolean existePorEmail(String email);
    // void validarNovoUsuario(Usuario usuario);
}
