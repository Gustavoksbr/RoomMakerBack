package com.example.roommaker.app.services.categorias.examples.coup.controler.request;

import com.example.roommaker.app.services.categorias.examples.coup.domain.model.main.Personagem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RespostaCoupRequest {
    private RespostaCoupEnum resposta;
    private Personagem personagem;
}
