package com.example.roommaker.app.services.categorias.examples.coup.domain.model;

import com.example.roommaker.app.domain.models.Sala;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaDosCoups {
    private String nomeSala;
    private String usernameDono;
    private List<String> convidados;
    private List<String> jogadores;
    private List<Coup> historicoCoups;
    private Coup coupAtual;
    public SalaDosCoups(Sala sala){
        this.nomeSala = sala.getNome();
        this.usernameDono = sala.getUsernameDono();
        this.convidados = new ArrayList<>();
       this.jogadores = new ArrayList<>();
       this.jogadores.add(sala.getUsernameDono());
         this.historicoCoups = new ArrayList<>();
         this.coupAtual = null;
    }
}
