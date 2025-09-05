package com.example.roommaker.app.domain.managers.sala;

import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.services.categorias.CategoriaService;
import com.example.roommaker.app.domain.ports.auth.AuthService;

import com.example.roommaker.app.domain.ports.repository.SalaRepository;

import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;
import com.example.roommaker.app.controllers.websocket.sala.SalaSenderWebsocket;
import com.example.roommaker.app.domain.managers.usuario.UsuarioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SalaManager {
    // injecoes

    private final AuthService authService;
    private final SalaRepository salaRepository;
    private final SalaSenderWebsocket webSocketSender;
    private final UsuarioManager usuarioManager;
    private final CategoriaService categoriaService;

    @Autowired
    public SalaManager(AuthService authService, SalaRepository salaRepository, UsuarioManager usuarioManager ,@Lazy SalaSenderWebsocket webSocketSender, CategoriaService categoriaService) {
        this.authService = authService;
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
        return  this.salaRepository.listarPorDono(usernameDono);
    }

    public Sala criar(Sala sala, String username) {
        sala.setUsernameDono(username);
     this.categoriaService.validarSalaParaOJogo(sala);
     Sala salaCriada = this.salaRepository.criar(sala);
     this.categoriaService.aposCriacaoDaSala(sala);
        return salaCriada;
    }

    public Sala mostrarSala(String nomeSala, String usernameDono) {
        return this.salaRepository.mostrarSala(nomeSala, usernameDono);
    }

   public Sala autenticarParticipante(String nomesala, String usernameDono, String senha, String usernameParticipante) {
       this.usuarioManager.encontrarUsername(usernameDono); // unica funcao eh garantir que o dono existe
        if(usernameParticipante.equals(usernameDono)){
            throw new ErroDeRequisicaoGeral("Você já está na sala!");
        }
       Sala sala = salaRepository.adicionarParticipante(nomesala, usernameDono, senha, usernameParticipante);
        this.categoriaService.adicionarJogador(usernameParticipante, sala);

       List<String> ouvintes = new ArrayList<>(sala.getUsernameParticipantes());
       ouvintes.add(usernameDono);
       System.out.println("Adicionando participante: " + sala);
        this.webSocketSender.enviarMensagemParaSala(sala.getUsernameDono(),sala.getNome(),"sala",ouvintes,sala.getUsernameParticipantes());
        return sala;
   }

    public Sala verificarSeUsuarioEstaNaSalaERetornarSala(String nomeSala, String usernameDono, String usernameParticipante) {
        return this.salaRepository.verificarSeUsuarioEstaNaSalaERetornarSala(nomeSala, usernameDono, usernameParticipante);
    }

    public void excluirSala(String usernameDono, String nomeSala, String username) {
        if(!usernameDono.equals(username)){
            throw new UsuarioNaoAutorizado("Você não pode excluir sem ser o dono da sala!");
        }
        // this.usuarioRepository.existePorUsername(usernameDono); //teoricamente ja foi verificado pelo JwtInterceptor (já que username == usernameDono), mas por garantia
        Sala sala = this.salaRepository.mostrarSala(nomeSala, usernameDono);
        this.salaRepository.excluirSala(usernameDono, nomeSala);

        List<String> ouvintes = new ArrayList<>(sala.getUsernameParticipantes());
        ouvintes.add(usernameDono);
        this.webSocketSender.enviarMensagemParaSala(sala.getUsernameDono(),sala.getNome(),"sala",ouvintes,sala.getUsernameParticipantes());
//        this.webSocketSender.enviarMensagemParaSala(usernameDono, nomeSala, "Sala excluida");
        this.categoriaService.excluirJogo(sala);
    }

    public Sala sairDaSala(String usernameDono, String nomeSala, String usernameSaindo, String username) {

        if(username.equals(usernameDono)){ // se você é o dono da sala
            if(usernameDono.equals(usernameSaindo)){  // e está saindo da sala
                throw new ErroDeRequisicaoGeral("O dono da sala não pode sair da sala!");
            }
        }
        else { // se você não é o dono da sala
            if(!username.equals(usernameSaindo)){ // e não está saindo da sala
                throw new UsuarioNaoAutorizado("Você não pode expulsar alguém da sala, se você não é o dono da sala!");
            }

        }
        Sala sala = this.salaRepository.sairDaSala(usernameDono, nomeSala, usernameSaindo);
        System.out.println("Saindo da sala: " + sala);
        this.categoriaService.notificarSaidaDeUsuario(usernameSaindo, sala);

       List<String> ouvintes = new ArrayList<>(sala.getUsernameParticipantes());
        ouvintes.add(usernameDono);
        ouvintes.add(usernameSaindo);
        System.out.println("Saindo da sala2: " + sala);
        this.webSocketSender.enviarMensagemParaSala(sala.getUsernameDono(),sala.getNome(),"sala",ouvintes,sala.getUsernameParticipantes());
        return sala;
    }
}
