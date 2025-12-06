package com.example.roommaker.app.categorias.examples.jokenpo.controller.dto;

import com.example.roommaker.app.categorias.examples.jokenpo.domain.model.Jokenpo;
import com.example.roommaker.app.categorias.examples.jokenpo.domain.model.JokenpoLance;
import com.example.roommaker.app.categorias.examples.jokenpo.domain.model.JokenpoStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JokenpoResponse {
    private Integer numero;
    private JokenpoLance lanceDono;
    private JokenpoLance lanceOponente;
    private JokenpoStatus status;
    public JokenpoResponse(Jokenpo jokenpo) {
        this.numero = jokenpo.getNumero();
        this.lanceDono = jokenpo.getLanceDono();
        this.lanceOponente = jokenpo.getLanceOponente();
        this.status = jokenpo.getStatus();
    }
    public Jokenpo toDomain() {
        return Jokenpo.builder().numero(numero).lanceDono(lanceDono).lanceOponente(lanceOponente).status(status).build();
    }
}
