package com.example.roommaker.app.categorias.examples.coup.controler.request;

import com.example.roommaker.app.categorias.examples.coup.domain.model.main.Acao;
import com.example.roommaker.app.categorias.examples.coup.domain.model.main.Personagem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcaoRequest {
    private Acao acao;
    private String alvo;
    private List<Personagem> influenciasTrocadas;
}
