package com.example.roommaker.app.domain.managers.usuario;

import com.example.roommaker.app.domain.models.JwtResponse;
import com.example.roommaker.app.domain.models.Response;
import com.example.roommaker.app.domain.models.Usuario;

import com.example.roommaker.app.domain.models.UsuarioBasicAuth;
import com.example.roommaker.app.domain.ports.auth.AuthService;
import com.example.roommaker.app.domain.ports.repository.UsuarioRepository;
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
        Usuario usuarioEncontrado = this.userRepository.encontrarUsernameDeOutroUsuario(usuario.getUsername()); // se
                                                                                                                // nao
                                                                                                                // encontrar,
                                                                                                                // erro
                                                                                                                // 404
        authService.matches(usuario.getPassword(), usuarioEncontrado.getPassword()); // usuario.getPassword() eh a senha
                                                                                     // que o usuario digitou e
                                                                                     // usuarioEncontrado.getPassword()
                                                                                     // eh a senha criptografada que
                                                                                     // esta no banco de dados. Se nao
                                                                                     // forem compativeis, erro 401
        return usuarioEncontrado;
    }

    // implementacoes
    public List<Usuario> listarUsuarios(String substring) {
        List<Usuario> lista;
        if (!substring.isEmpty()) {
            lista = this.userRepository.listarComSubstring(substring);
        } else {
            lista = this.userRepository.listar();
        }
        return lista;
    }

    public JwtResponse createUser(Usuario usuario) {
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
        this.userRepository.criar(usuarioParaSalvar);
        String token = authService.generateToken(usuario.getUsername());
        return new JwtResponse(token, usuario.getUsername(), usuario.getEmail());
    }

    public Response authenticate(Usuario usuario) {
        Usuario usuarioEncontrado = this.matchesThrow404(usuario); // verifica se a senha esta correta
        if (usuarioEncontrado.getDoisFatores()) { // verifica se o usuario tem 2fa ativado
            this.authService.sendVerificationCode(usuarioEncontrado.getEmail(), usuarioEncontrado.getUsername());
            return new UsuarioBasicAuth(usuarioEncontrado.getUsername());
        }
        String token = authService.generateToken(usuarioEncontrado.getUsername());
        return new JwtResponse(token, usuarioEncontrado.getUsername(), usuarioEncontrado.getEmail());
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

    /**
     * Decodifica o username do JWT — sem tocar no banco.
     *
     * É chamado a cada frame STOMP SEND/SUBSCRIBE ({@link
     * com.example.roommaker.app.controllers.websocket.filters.JwtWebsocketInterceptor}),
     * ou seja: uma vez por LANCE. Um "existePorUsername" aqui era uma ida ao
     * Mongo em todo lance de xadrez só para confirmar algo que a assinatura
     * HMAC do token já garante — a mesma verificação já tinha sido removida do
     * lado HTTP (ver {@link
     * com.example.roommaker.app.controllers.http.filters.JwtHTTPInterceptor})
     * pelo mesmo motivo; só faltava espelhar aqui.
     */
    public String capturarUsernameDoToken(String token) {
        return authService.getUsername(token);
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

    public LocalDate getDataNascimento(String username) {
        return this.userRepository.getDataNascimento(username);
    }

    // [CANCELADO] alterarUsername - username é usado como chave em todas as
    // coleções do MongoDB,
    // atualizar exigiria cascade update manual em salas, chats, jogos, etc.
    // public String alterarUsername(String usernameAtual, String novoUsername) {
    // this.userRepository.alterarUsername(usernameAtual, novoUsername);
    // return authService.generateToken(novoUsername);
    // }
}
