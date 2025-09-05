package com.example.roommaker.app.services.categorias.examples.jokenpo.controller.dto;

import com.example.roommaker.app.services.categorias.examples.jokenpo.domain.model.JokenpoLance;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JokenpoLanceRequest {
    private JokenpoLance lance;
}
