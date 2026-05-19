package com.example.roommaker.app.domain.managers.sala;

import com.example.roommaker.app.categorias.CategoriaService;
import com.example.roommaker.app.domain.models.Sala;

import com.example.roommaker.app.domain.ports.repository.SalaRepository;

import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;
import com.example.roommaker.app.controllers.websocket.sala.SalaSenderWebsocket;
import com.example.roommaker.app.domain.managers.usuario.UsuarioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class SalaManager {

    private final SalaRepository salaRepository;
    private final SalaSenderWebsocket webSocketSender;
    private final UsuarioManager usuarioManager;
    private final CategoriaService categoriaService;

    @Autowired
    public SalaManager(SalaRepository salaRepository, UsuarioManager usuarioManager,
            @Lazy SalaSenderWebsocket webSocketSender, CategoriaService categoriaService) {
        this.salaRepository = salaRepository;
        this.usuarioManager = usuarioManager;
        this.categoriaService = categoriaService;
        this.webSocketSender = webSocketSender;
    }

    public List<Sala> listar(String usernameDono, String nomeSala, String categoria) {
        return this.salaRepository.listar(usernameDono, nomeSala, categoria);
    }

    public List<Sala> listarPorParticipante(String usernameParticipante) {
        return this.salaRepository.listarPorParticipante(usernameParticipante);
    }

    public List<Sala> listarPorDono(String usernameDono) {
        return this.salaRepository.listarPorDono(usernameDono);
    }

    @Transactional
    public Sala criar(Sala sala, String username) {
        sala.setUsernameDono(username);
        this.categoriaService.validarSalaParaOJogo(sala);
        Sala salaCriada = this.salaRepository.criar(sala);
        categoriaService.aposCriacaoDaSala(salaCriada);

        // Notifica usuários online sobre nova sala
        this.webSocketSender.notificarAtualizacaoDeSalasParaUsuariosOnline("CRIADA",
                new com.example.roommaker.app.controllers.http.sala.dtos.SalaResponse(salaCriada));

        return salaCriada;
    }

    public Sala mostrarSala(String nomeSala, String usernameDono) {
        return this.salaRepository.mostrarSala(nomeSala, usernameDono);
    }

    @Transactional
    public Sala autenticarParticipante(String nomesala, String usernameDono, String senha,
            String usernameParticipante) {
        // Removida verificação de existência do dono: se a sala existe com esse
        // usernameDono, o dono obviamente existe. Evita 1 query desnecessária ao banco.
        if (usernameParticipante.equals(usernameDono)) {
            throw new ErroDeRequisicaoGeral("Você já está na sala!");
        }
        Sala sala = salaRepository.adicionarParticipante(nomesala, usernameDono, senha, usernameParticipante);
        this.categoriaService.adicionarJogador(usernameParticipante, sala);

        List<String> ouvintes = new ArrayList<>(sala.getUsernameParticipantes());
        ouvintes.add(usernameDono);
        this.webSocketSender.enviarMensagemParaSala(sala.getUsernameDono(), sala.getNome(), "sala", ouvintes,
                sala.getUsernameParticipantes());

        // Notifica usuários online sobre sala atualizada
        this.webSocketSender.notificarAtualizacaoDeSalasParaUsuariosOnline("ATUALIZADA",
                new com.example.roommaker.app.controllers.http.sala.dtos.SalaResponse(sala));

        return sala;
    }

    public Sala verificarSeUsuarioEstaNaSalaERetornarSala(String nomeSala, String usernameDono,
            String usernameParticipante) {
        return this.salaRepository.verificarSeUsuarioEstaNaSalaERetornarSala(nomeSala, usernameDono,
                usernameParticipante);
    }

    @Transactional
    public void excluirSala(String usernameDono, String nomeSala, String username) {
        if (!usernameDono.equals(username)) {
            throw new UsuarioNaoAutorizado("Você não pode excluir sem ser o dono da sala!");
        }
        Sala sala = this.salaRepository.excluirSalaERetornar(usernameDono, nomeSala);

        List<String> ouvintes = new ArrayList<>(sala.getUsernameParticipantes());
        ouvintes.add(usernameDono);
        this.webSocketSender.enviarMensagemParaSala(sala.getUsernameDono(), sala.getNome(), "sala", ouvintes,
                new ArrayList<>());

        this.categoriaService.excluirJogo(sala);

        // Notifica usuários online sobre sala deletada
        this.webSocketSender.notificarAtualizacaoDeSalasParaUsuariosOnline("DELETADA",
                new com.example.roommaker.app.controllers.http.sala.dtos.SalaResponse(sala));
    }

    @Transactional
    public Sala sairDaSala(String usernameDono, String nomeSala, String usernameSaindo, String username) {

        if (username.equals(usernameDono)) { // se você é o dono da sala
            if (usernameDono.equals(usernameSaindo)) { // e está saindo da sala
                throw new ErroDeRequisicaoGeral("O dono da sala não pode sair da sala!");
            }
        } else { // se você não é o dono da sala
            if (!username.equals(usernameSaindo)) { // e não está saindo da sala
                throw new UsuarioNaoAutorizado("Você não pode expulsar alguém da sala, se você não é o dono da sala!");
            }
        }
        Sala sala = this.salaRepository.sairDaSala(usernameDono, nomeSala, usernameSaindo);
        this.categoriaService.notificarSaidaDeUsuario(usernameSaindo, sala);

        List<String> ouvintes = new ArrayList<>(sala.getUsernameParticipantes());
        ouvintes.add(usernameDono);
        ouvintes.add(usernameSaindo);
        this.webSocketSender.enviarMensagemParaSala(sala.getUsernameDono(), sala.getNome(), "sala", ouvintes,
                sala.getUsernameParticipantes());

        // Notifica usuários online sobre sala atualizada
        this.webSocketSender.notificarAtualizacaoDeSalasParaUsuariosOnline("ATUALIZADA",
                new com.example.roommaker.app.controllers.http.sala.dtos.SalaResponse(sala));

        return sala;
    }

    @Transactional
    public Sala alterarCapacidade(String usernameDono, String nomeSala, Long novaCapacidade, String username) {
        if (!usernameDono.equals(username)) {
            throw new UsuarioNaoAutorizado("Apenas o dono da sala pode alterar a capacidade!");
        }
        Sala sala = this.salaRepository.mostrarSala(nomeSala, usernameDono);
        this.categoriaService.validarAlteracaoDeCapacidade(sala, novaCapacidade);

        // +1 para incluir o dono
        int totalAtual = sala.getUsernameParticipantes().size() + 1;
        if (novaCapacidade != null && novaCapacidade < totalAtual) {
            throw new ErroDeRequisicaoGeral(
                    "A nova capacidade (" + novaCapacidade + ") é menor que o número atual de pessoas na sala ("
                            + totalAtual + "). Expulse alguns jogadores antes de reduzir a capacidade.");
        }

        // Reutiliza a sala já carregada para evitar segundo find no repositório
        sala.setQtdCapacidade(novaCapacidade);
        Sala salaAtualizada = this.salaRepository.salvarSala(sala);

        // Notifica usuários online sobre sala atualizada
        this.webSocketSender.notificarAtualizacaoDeSalasParaUsuariosOnline("ATUALIZADA",
                new com.example.roommaker.app.controllers.http.sala.dtos.SalaResponse(salaAtualizada));

        return salaAtualizada;
    }

    public String verSenha(String usernameDono, String nomeSala, String username) {
        if (!usernameDono.equals(username)) {
            throw new UsuarioNaoAutorizado("Apenas o dono da sala pode ver a senha!");
        }
        return this.salaRepository.verSenha(usernameDono, nomeSala);
    }

    @Transactional
    public Sala alterarSenha(String usernameDono, String nomeSala, String novaSenha, String username) {
        if (!usernameDono.equals(username)) {
            throw new UsuarioNaoAutorizado("Apenas o dono da sala pode alterar a senha!");
        }
        Sala salaAtualizada = this.salaRepository.alterarSenha(usernameDono, nomeSala, novaSenha);

        // Notifica usuários online sobre sala atualizada
        this.webSocketSender.notificarAtualizacaoDeSalasParaUsuariosOnline("ATUALIZADA",
                new com.example.roommaker.app.controllers.http.sala.dtos.SalaResponse(salaAtualizada));

        return salaAtualizada;
    }

    /**
     * Retorna lista de usuários online
     */
    public Set<String> getUsuariosOnline() {
        return this.webSocketSender.getUsuariosOnline();
    }

    /**
     * Verifica se um usuário está online
     */
    public boolean isUsuarioOnline(String username) {
        return this.webSocketSender.isUsuarioOnline(username);
    }
}
