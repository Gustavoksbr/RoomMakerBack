package com.example.roommaker.app.services.persistence.usuario.repository;

import com.example.roommaker.app.domain.exceptions.ErroDeAutenticacaoGeral;
import com.example.roommaker.app.services.persistence.usuario.entity.UserEntity;
import com.example.roommaker.app.domain.models.Usuario;
import com.example.roommaker.app.domain.ports.repository.UsuarioRepository;
import com.example.roommaker.app.domain.exceptions.UsernameAlreadyExistsException;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoEncontrado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Repository
@Transactional
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
    public Boolean existePorEmail(String email) {
        return this.mongoUsuarioRepository.existsByEmail(email);
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
        System.out.println(lista);
        return lista;
    }

    @Override
    public void criar(Usuario usuario) {
        this.validarNovoUsuario(usuario);
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        System.out.println(usuario);
        this.mongoUsuarioRepository.save(new UserEntity(usuario)).toUsuario();
    }

    @Override

    public Usuario editarDescricao(Usuario usuario) {
        return this.mongoUsuarioRepository.findByUsernameAndAtivoTrue(usuario.getUsername()).map(userEntity -> {
            userEntity.setDescricao(usuario.getDescricao());
            return this.mongoUsuarioRepository.save(userEntity).toUsuario();
        }).orElseThrow(() -> new UsuarioNaoEncontrado(usuario.getUsername()));
    }

    @Override
    public void deletar(String username) {
        UserEntity userEntity = this.mongoUsuarioRepository.findByUsernameAndAtivoTrue(username)
                .orElseThrow(() -> new UsuarioNaoEncontrado(username));
        userEntity.deletar();
        this.mongoUsuarioRepository.save(userEntity);

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
    public void validarNovoUsuario(Usuario usuario) {
        String username = usuario.getUsername();
        String email = usuario.getEmail();
        if (this.existePorUsername(usuario.getUsername())) {
            throw new UsernameAlreadyExistsException(username);
        }
        if (this.existePorEmail(usuario.getEmail())) {
            throw new UsernameAlreadyExistsException("with email "+email);
        }
    }
    @Override
    public LocalDate getDataNascimento(String username) {
        UserEntity userEntity = this.entityFindByUsername(username);
        return userEntity.getDataNascimento();
    }
}
