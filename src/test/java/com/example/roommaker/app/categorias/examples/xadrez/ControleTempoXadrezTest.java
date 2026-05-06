package com.example.roommaker.app.categorias.examples.xadrez;

import com.example.roommaker.app.categorias.examples.xadrez.domain.model.ControleTempoXadrez;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControleTempoXadrezTest {

    @Test
    void tempoInfinito_QuandoAmbosNull_RetornaTrue() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(null)
                .tempoInicialPretas(null)
                .build();

        assertTrue(controle.tempoInfinito());
    }

    @Test
    void tempoInfinito_QuandoBrancasTemTempo_RetornaFalse() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(300)
                .tempoInicialPretas(null)
                .build();

        assertFalse(controle.tempoInfinito());
    }

    @Test
    void tempoInfinito_QuandoPretasTemTempo_RetornaFalse() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(null)
                .tempoInicialPretas(300)
                .build();

        assertFalse(controle.tempoInfinito());
    }

    @Test
    void inicializar_CopiaTempoParaRestante() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(600)
                .tempoInicialPretas(300)
                .build();

        controle.inicializar();

        assertEquals(600, controle.getTempoRestanteBrancas());
        assertEquals(300, controle.getTempoRestantePretas());
        assertNotNull(controle.getTimestampUltimoLance());
    }

    @Test
    void tempoEsgotado_QuandoTempoInfinito_RetornaFalse() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(null)
                .tempoInicialPretas(null)
                .build();

        assertFalse(controle.tempoEsgotado(true));
        assertFalse(controle.tempoEsgotado(false));
    }

    @Test
    void tempoEsgotado_QuandoTempoZero_RetornaTrue() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(300)
                .tempoInicialPretas(300)
                .build();
        controle.inicializar();
        controle.setTempoRestanteBrancas(0);

        assertTrue(controle.tempoEsgotado(true));
        assertFalse(controle.tempoEsgotado(false));
    }

    @Test
    void tempoEsgotado_QuandoTempoNegativo_RetornaTrue() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(300)
                .tempoInicialPretas(300)
                .build();
        controle.inicializar();
        controle.setTempoRestantePretas(-5);

        assertFalse(controle.tempoEsgotado(true));
        assertTrue(controle.tempoEsgotado(false));
    }

    @Test
    void tempoEsgotado_QuandoTempoPositivo_RetornaFalse() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(300)
                .tempoInicialPretas(300)
                .build();
        controle.inicializar();

        assertFalse(controle.tempoEsgotado(true));
        assertFalse(controle.tempoEsgotado(false));
    }

    @Test
    void incremento_PodeSerZero() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(300)
                .tempoInicialPretas(300)
                .incrementoBrancas(0)
                .incrementoPretas(0)
                .build();

        assertEquals(0, controle.getIncrementoBrancas());
        assertEquals(0, controle.getIncrementoPretas());
    }

    @Test
    void incremento_PodeSerDiferente() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(300)
                .tempoInicialPretas(180)
                .incrementoBrancas(5)
                .incrementoPretas(3)
                .build();

        assertEquals(5, controle.getIncrementoBrancas());
        assertEquals(3, controle.getIncrementoPretas());
    }
}
