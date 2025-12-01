package com.example.roommaker.app.services.categorias.examples.tictactoe.controller;

import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;
import com.example.roommaker.app.controllers.websocket.sala.SalaSenderWebsocket;
import com.example.roommaker.app.domain.managers.sala.SalaManager;
import com.example.roommaker.app.services.categorias.examples.tictactoe.controller.dto.TicTacToeDaSalaResponse;
import com.example.roommaker.app.services.categorias.examples.tictactoe.controller.dto.TicTacToeLanceRequestWs;
import com.example.roommaker.app.services.categorias.examples.tictactoe.controller.dto.TicTacToeLanceResponseWs;
import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.TicTacToeManager;
import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToe;
import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToeSala;
import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToeLance;
import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToeStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class GameTicTacToeController {
    private final SalaSenderWebsocket salaSenderWebsocket;
    private final SalaManager salaManager;
    private final TicTacToeManager ticTacToeManager;
    @Autowired
    public GameTicTacToeController(SalaManager salaManager, TicTacToeManager ticTacToeManager, SalaSenderWebsocket salaSenderWebsocket) { //, TicTacToeManager ticTacToeManager, SalaSenderWebsocket salaSenderWebsocket, SimpMessagingTemplate simpMessagingTemplate
        this.salaSenderWebsocket = salaSenderWebsocket;
        this.salaManager = salaManager;
        this.ticTacToeManager = ticTacToeManager;
    }


    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/tictactoe")
    public void comecar( @DestinationVariable String usernameDono, @DestinationVariable String nomeSala, @DestinationVariable String username){ //@Payload TicTacToeComecarRequest request,
        Sala salad = this.salaManager.verificarSeUsuarioEstaNaSalaERetornarSala( nomeSala, usernameDono,username);
       if(!(salad.getCategoria().equals("tictactoe"))){
           throw new UsuarioNaoAutorizado("Esta sala não é de jogo da velha");
       }
       else {
       List<String> ouvintes = salad.getUsernameParticipantes().isEmpty() ? new ArrayList<>() : salad.getUsernameParticipantes();
            ouvintes.add(usernameDono);
            if (salad.getUsernameParticipantes().isEmpty() ) {
                TicTacToeLanceResponseWs response = new TicTacToeLanceResponseWs(null,null, "_________", TicTacToeStatus.WAITING);
                this.salaSenderWebsocket.enviarMensagemParaSala(usernameDono, nomeSala, "tictactoe", ouvintes, response);
            } else {
                TicTacToeSala ticTacToeSala = this.ticTacToeManager.comecar(salad);
                TicTacToeDaSalaResponse response = new TicTacToeDaSalaResponse(ticTacToeSala);
                this.salaSenderWebsocket.enviarMensagemParaSala(usernameDono, nomeSala, "tictactoe", ouvintes, response);
            }
        }
    }
    @MessageMapping("/sala/{usernameDono}/{nomeSala}/{username}/tictactoe/lance")
    public void lance(@Payload TicTacToeLanceRequestWs request, @DestinationVariable String usernameDono, @DestinationVariable String nomeSala, @DestinationVariable String username){
        Sala salad = this.salaManager.verificarSeUsuarioEstaNaSalaERetornarSala( nomeSala, usernameDono,username);
        if(!(salad.getCategoria().equals("tictactoe"))){
            throw new UsuarioNaoAutorizado("Esta sala não é de jogo da velha");
        }
        TicTacToeLance ticTacToeLance = request.toDomain();
       TicTacToe ticTacToe = this.ticTacToeManager.lance(ticTacToeLance,username,salad);

       TicTacToeLanceResponseWs response = new TicTacToeLanceResponseWs(ticTacToe);

       List<String> ouvintes = salad.getUsernameParticipantes();
       ouvintes.add(usernameDono);

       this.salaSenderWebsocket.enviarMensagemParaSala(usernameDono, nomeSala, "tictactoe", ouvintes, response);
    }


}
