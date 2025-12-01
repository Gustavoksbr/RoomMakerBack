package com.example.roommaker.app.services.persistence.usuario.repository;

import com.example.roommaker.app.domain.exceptions.Erro409;
import com.example.roommaker.app.domain.exceptions.ErroDeAutenticacaoGeral;
import com.example.roommaker.app.services.persistence.usuario.entity.UserEntity;
import com.example.roommaker.app.domain.models.Usuario;
import com.example.roommaker.app.domain.ports.repository.UsuarioRepository;
import com.example.roommaker.app.domain.exceptions.UsernameAlreadyExistsException;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoEncontrado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {
    @Autowired
    private MongoUsuarioRepository mongoUsuarioRepository;

    // metodos privados

    private UserEntity entityFindByUsername(String username) {
        return this.mongoUsuarioRepository.findByUsernameAndAtivoTrue(username).orElseThrow(() -> new UsuarioNaoEncontrado(username));
    }


    // implementacoes
    @Override
    public Boolean existePorUsername(String username) {
        return this.mongoUsuarioRepository.existsByUsername(username);
    }

    @Override
    public Usuario encontrarUsernameDoUsuarioAtual(String username) {
        return this.mongoUsuarioRepository.findByUsernameAndAtivoTrue(username).orElseThrow(() -> new ErroDeAutenticacaoGeral("Usuário não encontrado")).toUsuario();
    }

    @Override
    public Usuario encontrarPorEmail(String email) {
        return this.mongoUsuarioRepository.findByEmailAndAtivoTrue(email).orElseThrow(() -> new ErroDeAutenticacaoGeral("Usuário com email "+email+" não encontrado")).toUsuario();
    }

    @Override
    public void alterarSenha(Usuario usuario) {
        UserEntity userEntity = this.entityFindByUsername(usuario.getUsername());
        userEntity.setPassword(usuario.getPassword());
        this.mongoUsuarioRepository.save(userEntity);
    }



    @Override
    public List<Usuario> listar() {
        return this.mongoUsuarioRepository.findAllByAtivoTrue().stream().map(UserEntity::toUsuario).toList();
    }

    @Override
    public List<Usuario> listarComSubstring(String substring) {
        List<Usuario> lista= this.mongoUsuarioRepository.encontrarPorSubstring(substring).orElse(new ArrayList<>()).stream().map(UserEntity::toUsuarioSemSenha).toList();
        return lista;
    }

    @Override
    public void criar(Usuario usuario) {
        try {
            this.mongoUsuarioRepository.save(new UserEntity(usuario)).toUsuario();
        } catch (DataIntegrityViolationException e) {

            String mensagem = e.getMostSpecificCause().getMessage();

            if (mensagem != null) {
                if (mensagem.contains("username")) {
                    throw new Erro409("O username já está em uso.");
                }
                if (mensagem.contains("email")) {
                    throw new Erro409("O email já está em uso.");
                }
            }

            throw new Erro409("Username ou Email já existem.");
        }
    }


    @Override
    public Usuario encontrarUsernameDeOutroUsuario(String username) {
        return this.entityFindByUsername(username).toUsuario();
    }


    @Override
    public void alterarDoisFatores(Usuario usuario) {
         UserEntity userEntity = this.entityFindByUsername(usuario.getUsername());
         userEntity.setDoisFatores(usuario.getDoisFatores());
         this.mongoUsuarioRepository.save(userEntity);
        }

    @Override
    public LocalDate getDataNascimento(String username) {
        UserEntity userEntity = this.entityFindByUsername(username);
        return userEntity.getDataNascimento();
    }
}
