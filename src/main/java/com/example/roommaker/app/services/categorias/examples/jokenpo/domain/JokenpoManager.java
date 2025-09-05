package com.example.roommaker.app.services.categorias.examples.jokenpo.domain;

import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.domain.exceptions.Erro500;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.services.categorias.examples.jokenpo.domain.model.Jokenpo;
import com.example.roommaker.app.services.categorias.examples.jokenpo.domain.model.JokenpoLance;
import com.example.roommaker.app.services.categorias.examples.jokenpo.domain.model.JokenpoSala;
import com.example.roommaker.app.services.categorias.examples.jokenpo.domain.model.JokenpoStatus;
import com.example.roommaker.app.services.categorias.examples.jokenpo.repository.JokenpoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class JokenpoManager {
    private  final JokenpoRepository jokenpoRepository;
    @Autowired
    public JokenpoManager(JokenpoRepository jokenpoRepository) {
        this.jokenpoRepository = jokenpoRepository;
    }

    private JokenpoStatus verificarResultado(JokenpoLance lanceDono, JokenpoLance lanceOponente) {
        if (lanceDono == JokenpoLance.ESPERANDO || lanceOponente == JokenpoLance.ESPERANDO) {
            return JokenpoStatus.WAITING;
        }
        if (lanceDono == lanceOponente) {
            return JokenpoStatus.DRAW;
        }
        if (lanceDono == JokenpoLance.PEDRA) {
            if (lanceOponente == JokenpoLance.PAPEL) {
                return JokenpoStatus.OPONENTE_WIN;
            }
            return JokenpoStatus.DONO_WIN;
        }
        if (lanceDono == JokenpoLance.PAPEL) {
            if (lanceOponente == JokenpoLance.TESOURA) {
                return JokenpoStatus.OPONENTE_WIN;
            }
                return JokenpoStatus.DONO_WIN;
        }
        if (lanceDono == JokenpoLance.TESOURA) {
            if (lanceOponente == JokenpoLance.PEDRA) {
                return JokenpoStatus.OPONENTE_WIN;
            }
            return JokenpoStatus.DONO_WIN;
        }
        throw new Erro500("Erro ao verificar resultado");
    }
    public JokenpoSala retornarJokenpoSala(String nomeSala, String usernameDono){
        return this.jokenpoRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono);
    }

    public Jokenpo lance(JokenpoLance lance,String username,  Sala sala) {
        JokenpoSala jokenpoSala = this.jokenpoRepository.findByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
        if(lance==null || lance.equals(JokenpoLance.SEGREDO)){
            throw new ErroDeRequisicaoGeral("Lance não pode ser nulo ou segredo");
        }
        if (jokenpoSala == null) {
            new Jokenpo();
            jokenpoSala = JokenpoSala.builder()
                    .nomeSala(sala.getNome())
                    .usernameDono(sala.getUsernameDono())
                    .usernameOponente(null)
                    .jogoAtual(Jokenpo.builder().lanceOponente(JokenpoLance.ESPERANDO).lanceDono(JokenpoLance.ESPERANDO).status(JokenpoStatus.WAITING).build())
                    .historico(new ArrayList<>())
                    .build();

            jokenpoSala = this.jokenpoRepository.criar(jokenpoSala);
        }

        if (jokenpoSala.getUsernameOponente() == null) { // adiciona o oponente no jogo
            if (sala.getUsernameParticipantes() != null && !sala.getUsernameParticipantes().isEmpty()) {
                String usernameOponente = sala.getUsernameParticipantes().get(0);
                jokenpoSala.setUsernameOponente(usernameOponente);
            }
        }
        Jokenpo jogoAtual = jokenpoSala.getJogoAtual();// adiciona o lance do oponente
        if (username.equals(jokenpoSala.getUsernameDono())) {
            jogoAtual.setLanceDono(lance);
        } else if (username.equals(jokenpoSala.getUsernameOponente())) {
            jogoAtual.setLanceOponente(lance);
        }

        jokenpoSala.setJogoAtual(jogoAtual);

        Jokenpo jogo = new Jokenpo(jogoAtual);

        JokenpoLance lanceDono = jogoAtual.getLanceDono();
        JokenpoLance lanceOponente = jogoAtual.getLanceOponente();

        JokenpoStatus status = this.verificarResultado(lanceDono, lanceOponente);
        jogo.setStatus(status);
        if(status!=JokenpoStatus.WAITING){
            jogoAtual.setStatus(status);
            jokenpoSala.getHistorico().add(jogoAtual);
            jokenpoSala.setJogoAtual(Jokenpo.builder().lanceOponente(JokenpoLance.ESPERANDO).lanceDono(JokenpoLance.ESPERANDO).status(JokenpoStatus.WAITING).build());
        }
        this.jokenpoRepository.salvar(jokenpoSala);
        return jogo;
    }
    public void validarSalaParaOJogo(Sala sala){
        if(sala.getQtdCapacidade()!=2){
            throw new ErroDeRequisicaoGeral("Sala de tictactoe deve ter capacidade de 2");
        }
    }
    public void saidaDeParticipante(String usernameParticipante, Sala sala){
        this.jokenpoRepository.deleteByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
    }
    public void deletarJogo(Sala sala){
        this.jokenpoRepository.deleteByNomeSalaAndUsernameDono(sala.getNome(), sala.getUsernameDono());
    }
}
