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
                .tempoInicialBrancas(300L)
                .tempoInicialPretas(null)
                .build();

        assertFalse(controle.tempoInfinito());
    }

    @Test
    void tempoInfinito_QuandoPretasTemTempo_RetornaFalse() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(null)
                .tempoInicialPretas(300L)
                .build();

        assertFalse(controle.tempoInfinito());
    }

    @Test
    void inicializar_CopiaTempoParaRestante() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(600L)
                .tempoInicialPretas(300L)
                .build();

        controle.inicializar();

        assertEquals(600L, controle.getTempoRestanteBrancas());
        assertEquals(300L, controle.getTempoRestantePretas());
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
                .tempoInicialBrancas(300L)
                .tempoInicialPretas(300L)
                .build();
        controle.inicializar();
        controle.setTempoRestanteBrancas(0L);

        assertTrue(controle.tempoEsgotado(true));
        assertFalse(controle.tempoEsgotado(false));
    }

    @Test
    void tempoEsgotado_QuandoTempoNegativo_RetornaTrue() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(300L)
                .tempoInicialPretas(300L)
                .build();
        controle.inicializar();
        controle.setTempoRestantePretas(-5L);

        assertFalse(controle.tempoEsgotado(true));
        assertTrue(controle.tempoEsgotado(false));
    }

    @Test
    void tempoEsgotado_QuandoTempoPositivo_RetornaFalse() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(300L)
                .tempoInicialPretas(300L)
                .build();
        controle.inicializar();

        assertFalse(controle.tempoEsgotado(true));
        assertFalse(controle.tempoEsgotado(false));
    }

    @Test
    void incremento_PodeSerZero() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(300L)
                .tempoInicialPretas(300L)
                .incrementoBrancas(0L)
                .incrementoPretas(0L)
                .build();

        assertEquals(0L, controle.getIncrementoBrancas());
        assertEquals(0L, controle.getIncrementoPretas());
    }

    @Test
    void incremento_PodeSerDiferente() {
        ControleTempoXadrez controle = ControleTempoXadrez.builder()
                .tempoInicialBrancas(300L)
                .tempoInicialPretas(180L)
                .incrementoBrancas(5L)
                .incrementoPretas(3L)
                .build();

        assertEquals(5L, controle.getIncrementoBrancas());
        assertEquals(3L, controle.getIncrementoPretas());
    }
}
