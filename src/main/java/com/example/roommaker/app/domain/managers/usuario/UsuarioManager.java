package com.example.roommaker.app.domain.managers.usuario;


import com.example.roommaker.app.domain.models.JwtResponse;
import com.example.roommaker.app.domain.models.Response;
import com.example.roommaker.app.domain.models.Usuario;

import com.example.roommaker.app.domain.models.UsuarioBasicAuth;
import com.example.roommaker.app.domain.ports.auth.AuthService;
import com.example.roommaker.app.domain.ports.repository.UsuarioRepository;
import com.example.roommaker.app.domain.exceptions.ErroDeAutenticacaoGeral;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UsuarioManager {

    // injecoes

    private final UsuarioRepository userRepository;
    private final AuthService authService;

    @Autowired
    public UsuarioManager(UsuarioRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

// variaveis e metodos privados

    private Usuario matches(Usuario usuario) {
        Usuario usuarioEncontrado = this.userRepository.encontrarUsernameDoUsuarioAtual(usuario.getUsername());
        authService.matches(usuario.getPassword(), usuarioEncontrado.getPassword());
        return usuarioEncontrado;
    }
    private Usuario matchesThrow404(Usuario usuario) {
        Usuario usuarioEncontrado = this.userRepository.encontrarUsernameDeOutroUsuario(usuario.getUsername()); // se nao encontrar, erro 404
        authService.matches(usuario.getPassword(), usuarioEncontrado.getPassword()); // usuario.getPassword() eh a senha que o usuario digitou e usuarioEncontrado.getPassword() eh a senha criptografada que esta no banco de dados. Se nao forem compativeis, erro 401
        return usuarioEncontrado;
    }

// implementacoes
    public List<Usuario> listarUsuarios(String substring) {
        List<Usuario> lista;
        if (!substring.isEmpty()) {
            lista = this.userRepository.listarComSubstring(substring);
        }
        else{
            lista = this.userRepository.listar();
        }
        return lista;
    }
    public String createUser(Usuario usuario)  {
        // Criar uma nova instância de Usuario com a senha criptografada
        //this.userRepository.validarNovoUsuario(usuario); // verificar se o username ou o email ja estao sendo utilizados por outra pessoa

        String senhaCriptografada = this.authService.encode(usuario.getPassword());
       Usuario usuarioParaSalvar = Usuario.builder()
    .username(usuario.getUsername())
    .password(senhaCriptografada)
    .descricao(usuario.getDescricao())
    .email(usuario.getEmail())
    .ativo(true)
    .doisFatores(false)
               .dataNascimento(usuario.getDataNascimento())
    .build();

        try {
            this.userRepository.criar(usuarioParaSalvar);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return authService.generateToken(usuario.getUsername());
    }
    public Response authenticate(Usuario usuario) {
       Usuario usuarioEncontrado = this.matchesThrow404(usuario); //verifica se a senha esta correta
            if (usuarioEncontrado.getDoisFatores()) { //verifica se o usuario tem 2fa ativado
               this.authService.sendVerificationCode(usuarioEncontrado.getEmail(), usuarioEncontrado.getUsername());
                return new UsuarioBasicAuth(usuarioEncontrado.getUsername());
            }
            return new JwtResponse(authService.generateToken(usuarioEncontrado.getUsername())) ;
    }
    public String authenticate2fa(Usuario usuario, String codigo) {
        userRepository.encontrarPorEmail(usuario.getEmail());
        return authService.validarCodigo(usuario.getUsername(), codigo);
    }

    public Boolean habilitarDesabilitarDoisFatores(String token) {
        String username = authService.getUsername(token);
        Usuario usuario = this.userRepository.encontrarUsernameDeOutroUsuario(username);
        usuario.setDoisFatores(!usuario.getDoisFatores());
        this.userRepository.alterarDoisFatores(usuario);
        return usuario.getDoisFatores();
    }


    public String capturarUsernameDoToken(String token) {
        String username = authService.getUsername(token);
         if(!this.userRepository.existePorUsername(username)){
            throw new ErroDeAutenticacaoGeral("Usuario nao encontrado");
         }
         return username;
    }
    public Usuario encontrarUsername(String username) {
        return this.userRepository.encontrarUsernameDeOutroUsuario(username);
    }

    public void esqueciSenha(Usuario usuario) {
        Usuario usuarioEncontrado = this.userRepository.encontrarPorEmail(usuario.getEmail());
        this.authService.sendEsqueciSenhaCode(usuarioEncontrado);
    }

    public String alterarSenha(Usuario usuario, String codigo) {
        Usuario usuarioEncontrado = this.userRepository.encontrarPorEmail(usuario.getEmail());
        String jwt = this.authService.alterarSenha(usuarioEncontrado, codigo);
        usuarioEncontrado.setPassword(this.authService.encode(usuario.getPassword()));
        this.userRepository.alterarSenha(usuarioEncontrado);
        return jwt;
    }

    public LocalDate getDataNascimento(String username){
        return this.userRepository.getDataNascimento(username);
    }
}
