package com.example.roommaker.app.categorias.examples.coup.controler;

import com.example.roommaker.app.controllers.websocket.sala.SalaSenderWebsocket;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.categorias.examples.coup.domain.CoupManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class CoupController {
    private final SalaSenderWebsocket salaSenderWebsocket;
    private final SalaManager salaManager;
    private final CoupManager coupManager;
    @Autowired
    public CoupController(SalaSenderWebsocket salaSenderWebsocket, SalaManager salaManager, CoupManager coupManager) {
        this.salaSenderWebsocket = salaSenderWebsocket;
        this.salaManager = salaManager;
        this.coupManager = coupManager;
    }
//  @MessageMapping("/sala/{usernameDono}/{salaNome}/{username}/coup/iniciar")
//    public void iniciarJogo(@DestinationVariable String usernameDono,@DestinationVariable String salaNome,@DestinationVariable String username) {
//        Sala sala = this.salaManager.verificarSeUsuarioEstaNaSalaERetornarSala(salaNome, usernameDono, username);
//        if(!sala.getUsernameDono().equals(username)){
//            throw new UsuarioNaoAutorizado("Apenas o dono da sala pode iniciar o jogo");
//        }
//       Coup coup = this.coupManager.iniciarJogo(sala);
//        for (JogadorCoup jogador : coup.getJogadores()){
//            String usernameParaEnviar = jogador.getUsername();
//            List<String> ouvintes = List.of(usernameParaEnviar);
//            CoupResponse coupResponse = CoupResponse.builder()
//                    .id(coup.getId())
//                    .usernameJogadoresVivos(coup.getUsernameJogadoresVivos())
//                    .usernameJogadoresMortos(coup.getUsernameJogadoresMortos())
//                    .turnos(coup.getTurnos())
//                    .turnoAtual(coup.getTurnoAtual())
//                    .jogador(jogador)
//                    .build();
//            this.salaSenderWebsocket.enviarMensagemParaSala(sala.getUsernameDono(), sala.getNome(), "coup", ouvintes , coupResponse);
//        }
//    }
}