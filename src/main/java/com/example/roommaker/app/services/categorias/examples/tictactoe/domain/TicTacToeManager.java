package com.example.roommaker.app.services.categorias.examples.tictactoe.domain;

import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToe;
import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToeSala;
import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToeLance;
import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToeStatus;
import com.example.roommaker.app.services.categorias.examples.tictactoe.repository.TicTacToeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class TicTacToeManager {
    private final TicTacToeRepository ticTacToeRepository;
    private static List<List<Integer>> possiveisVitorias = new ArrayList<>(Arrays.asList(
            Arrays.asList(0, 1, 2),
            Arrays.asList(3, 4, 5),
            Arrays.asList(6, 7, 8),
            Arrays.asList(0, 3, 6),
            Arrays.asList(1, 4, 7),
            Arrays.asList(2, 5, 8),
            Arrays.asList(0, 4, 8),
            Arrays.asList(2, 4, 6)
    ));
    private String sortearJogadorInicial(String usernameDono, String usernameParticipante){
        String x;
        Random random = new Random();
        int result = random.nextInt(2);
        if(result == 0){
            x = usernameDono;
        }
        else{
            x = usernameParticipante;
        }
        return x;
    }
    private TicTacToe jogar(TicTacToe jogoAtual, Integer lance, String jogador){
        char[] posicao = jogoAtual.getPosicao().toCharArray();
        if(posicao[lance] == '_'){
            posicao[lance] = jogador.charAt(0); // adiciona o lance no tabuleiro
            jogoAtual.setPosicao(String.valueOf(posicao));
            jogoAtual = this.verificarVitoria(jogoAtual, jogador);
        }
        else{
            throw new ErroDeRequisicaoGeral("Posição já ocupada");
        }
        return jogoAtual;
    }
    private TicTacToe verificarVitoria(TicTacToe jogoAtual,String jogador){
        List<Integer> lancesDoJogador = new ArrayList<>();
        List<List<Integer>> vitorias = new ArrayList<>();
        for (int i = 0; i < jogoAtual.getPosicao().length(); i++) {
            if(jogoAtual.getPosicao().charAt(i) == jogador.charAt(0)){
                lancesDoJogador.add(i);
            }
        }
        for (List<Integer> possivelVitoria : possiveisVitorias) {
            if(lancesDoJogador.containsAll(possivelVitoria)){
                vitorias.add(possivelVitoria);
                jogoAtual.setStatus(jogador.equals("x") ? TicTacToeStatus.x_WIN : TicTacToeStatus.o_WIN);
                break;
            }
        }
        if(vitorias.isEmpty()){
            if(jogoAtual.getPosicao().contains("_")){
                jogoAtual.setStatus(jogador.equals("x") ? TicTacToeStatus.o_TURN : TicTacToeStatus.x_TURN);
            }
            else{
                jogoAtual.setStatus(TicTacToeStatus.DRAW);
            }
        }
        return jogoAtual;
    }

    @Autowired
    public TicTacToeManager(TicTacToeRepository ticTacToeRepository) {
        this.ticTacToeRepository = ticTacToeRepository;
    }

    public TicTacToeSala comecar(Sala sala) {
        String x;
        String o;
        TicTacToeSala ticTacToeSala = this.ticTacToeRepository.findByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
        if (ticTacToeSala == null) {
            ticTacToeSala = TicTacToeSala.builder()
                    .nomeSala(sala.getNome())
                    .usernameDono(sala.getUsernameDono())
                    .usernameOponente(sala.getUsernameParticipantes().get(0))
                    .jogoAtual(null)
                    .historico(new ArrayList<>())
                    .build();
            x = this.sortearJogadorInicial(sala.getUsernameDono(),sala.getUsernameParticipantes().get(0));
            o = x.equals(sala.getUsernameDono()) ? sala.getUsernameParticipantes().get(0) : sala.getUsernameDono();
            this.ticTacToeRepository.criar(ticTacToeSala);
        }
        else {
            if (ticTacToeSala.getJogoAtual() == null) { // se ticTacToeSala for nulo e o jogo atual for nulo, eh pq ja teve jogo antes. Senao, esse sera o primeiro jogo da sala
                x = ticTacToeSala.getHistorico().get(ticTacToeSala.getHistorico().size() - 1).getO(); // O jogador "x" será o jogador "o" da última partida
                o = x.equals(sala.getUsernameDono()) ? sala.getUsernameParticipantes().get(0) : sala.getUsernameDono();
            }
            else {
                return ticTacToeSala;
            }
        }
        TicTacToe ticTacToe = TicTacToe.builder()
                .x(x)
                .o(o)
                .posicao("_________")
                .status(TicTacToeStatus.x_TURN)
                .build();
        ticTacToeSala.setJogoAtual(ticTacToe);
        this.ticTacToeRepository.salvar(ticTacToeSala);
        return ticTacToeSala;
    }

    public TicTacToe lance(TicTacToeLance lance, String username, Sala sala) {
        TicTacToeSala ticTacToeSala = this.ticTacToeRepository.findByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
        if (ticTacToeSala == null) {
            throw new ErroDeRequisicaoGeral("Sala de jogo não inicializada");
        }
        TicTacToe jogoAtual = ticTacToeSala.getJogoAtual();
        if(jogoAtual  == null){
            throw new ErroDeRequisicaoGeral("Jogo não inicializado");
        }

        if(jogoAtual.getStatus() == TicTacToeStatus.x_TURN && username.equals(jogoAtual.getX())){
            jogoAtual = this.jogar(jogoAtual, lance.getLance(), "x");
        } else if(jogoAtual.getStatus() == TicTacToeStatus.o_TURN && username.equals(jogoAtual.getO())){
            jogoAtual = this.jogar(jogoAtual, lance.getLance(), "o");
        } else{
            throw new ErroDeRequisicaoGeral("Não é a sua vez de jogar");
        }

        if(jogoAtual.getStatus() == TicTacToeStatus.o_WIN || jogoAtual.getStatus() == TicTacToeStatus.x_WIN || jogoAtual.getStatus() == TicTacToeStatus.DRAW){
            ticTacToeSala.setJogoAtual(null);
            ticTacToeSala.getHistorico().add(jogoAtual);
        } else{
            ticTacToeSala.setJogoAtual(jogoAtual);
        }
        this.ticTacToeRepository.salvar(ticTacToeSala);
        System.out.println("jogo atual: " + jogoAtual.getPosicao());
        return jogoAtual;
    }

    public void validarSalaParaOJogo(Sala sala){
            if(sala.getQtdCapacidade()!=2){
                throw new ErroDeRequisicaoGeral("Sala de tictactoe deve ter capacidade de 2");
        }
    }

    public void saidaDeParticipante(String usernameParticipante, Sala sala){
        this.ticTacToeRepository.deleteByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
//        this.comecar(sala);
    }

    public void deletarJogo(Sala sala) {
        this.ticTacToeRepository.deleteByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
    }

}
