package com.example.roommaker.app.categorias.examples.jokenpo.domain.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Jokenpo {
    private Integer numero;
    private JokenpoLance lanceDono;
    private JokenpoLance lanceOponente;
    private JokenpoStatus status;
    public Jokenpo(Jokenpo jokenpo){
        this.numero = jokenpo.getNumero();
        this.lanceDono = jokenpo.getLanceDono();
        this.lanceOponente = jokenpo.getLanceOponente();
        this.status = jokenpo.getStatus();
    }
}
