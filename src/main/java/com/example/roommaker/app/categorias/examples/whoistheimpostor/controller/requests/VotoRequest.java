package com.example.roommaker.app.categorias.examples.whoistheimpostor.controller.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VotoRequest {
    @NotBlank
    private String voto;
}
