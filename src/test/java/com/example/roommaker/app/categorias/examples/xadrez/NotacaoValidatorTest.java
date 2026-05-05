package com.example.roommaker.app.categorias.examples.xadrez;

import com.example.roommaker.app.categorias.examples.xadrez.domain.NotacaoValidator;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.NotacaoXadrez;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para o validador de notação.
 */
class NotacaoValidatorTest {

    @Test
    @DisplayName("Notação inglesa: aceita lances válidos em inglês")
    void notacaoInglesaValida() {
        assertTrue(NotacaoValidator.validarNotacao("e4", NotacaoXadrez.INGLESA));
        assertTrue(NotacaoValidator.validarNotacao("Nf3", NotacaoXadrez.INGLESA));
        assertTrue(NotacaoValidator.validarNotacao("Qxd5", NotacaoXadrez.INGLESA));
        assertTrue(NotacaoValidator.validarNotacao("Ke2", NotacaoXadrez.INGLESA));
        assertTrue(NotacaoValidator.validarNotacao("Rf1", NotacaoXadrez.INGLESA));
        assertTrue(NotacaoValidator.validarNotacao("Bc4", NotacaoXadrez.INGLESA));
        assertTrue(NotacaoValidator.validarNotacao("O-O", NotacaoXadrez.INGLESA));
        assertTrue(NotacaoValidator.validarNotacao("O-O-O", NotacaoXadrez.INGLESA));
    }

    @Test
    @DisplayName("Notação inglesa: rejeita lances em português")
    void notacaoInglesaRejeitaPortugues() {
        assertFalse(NotacaoValidator.validarNotacao("Cf3", NotacaoXadrez.INGLESA)); // C = cavalo PT
        assertFalse(NotacaoValidator.validarNotacao("Dxd5", NotacaoXadrez.INGLESA)); // D = dama PT
        assertFalse(NotacaoValidator.validarNotacao("Tf1", NotacaoXadrez.INGLESA)); // T = torre PT
    }

    @Test
    @DisplayName("Notação portuguesa: aceita lances válidos em português")
    void notacaoPortuguesaValida() {
        assertTrue(NotacaoValidator.validarNotacao("e4", NotacaoXadrez.PORTUGUESA));
        assertTrue(NotacaoValidator.validarNotacao("Cf3", NotacaoXadrez.PORTUGUESA));
        assertTrue(NotacaoValidator.validarNotacao("Dxd5", NotacaoXadrez.PORTUGUESA));
        assertTrue(NotacaoValidator.validarNotacao("Re2", NotacaoXadrez.PORTUGUESA));
        assertTrue(NotacaoValidator.validarNotacao("Tf1", NotacaoXadrez.PORTUGUESA));
        assertTrue(NotacaoValidator.validarNotacao("Bc4", NotacaoXadrez.PORTUGUESA));
        assertTrue(NotacaoValidator.validarNotacao("O-O", NotacaoXadrez.PORTUGUESA));
        assertTrue(NotacaoValidator.validarNotacao("O-O-O", NotacaoXadrez.PORTUGUESA));
    }

    @Test
    @DisplayName("Notação portuguesa: rejeita lances em inglês")
    void notacaoPortuguesaRejeitaIngles() {
        assertFalse(NotacaoValidator.validarNotacao("Nf3", NotacaoXadrez.PORTUGUESA)); // N = cavalo EN
        assertFalse(NotacaoValidator.validarNotacao("Qxd5", NotacaoXadrez.PORTUGUESA)); // Q = dama EN
        assertFalse(NotacaoValidator.validarNotacao("Ke2", NotacaoXadrez.PORTUGUESA)); // K = rei EN
    }

    @Test
    @DisplayName("obterErroNotacao: retorna mensagem descritiva para notação errada")
    void obterErroNotacaoDescritivo() {
        // Português tentando usar inglês
        String erro1 = NotacaoValidator.obterErroNotacao("Nf3", NotacaoXadrez.PORTUGUESA);
        assertNotNull(erro1);
        assertTrue(erro1.contains("N→C"));

        String erro2 = NotacaoValidator.obterErroNotacao("Qxd5", NotacaoXadrez.PORTUGUESA);
        assertNotNull(erro2);
        assertTrue(erro2.contains("Q→D"));

        String erro3 = NotacaoValidator.obterErroNotacao("Ke2", NotacaoXadrez.PORTUGUESA);
        assertNotNull(erro3);
        assertTrue(erro3.contains("K→R"));

        // Inglês tentando usar português
        String erro4 = NotacaoValidator.obterErroNotacao("Cf3", NotacaoXadrez.INGLESA);
        assertNotNull(erro4);
        assertTrue(erro4.contains("C→N"));

        String erro5 = NotacaoValidator.obterErroNotacao("Dxd5", NotacaoXadrez.INGLESA);
        assertNotNull(erro5);
        assertTrue(erro5.contains("D→Q"));

        String erro6 = NotacaoValidator.obterErroNotacao("Tf1", NotacaoXadrez.INGLESA);
        assertNotNull(erro6);
        assertTrue(erro6.contains("T→R"));
    }

    @Test
    @DisplayName("obterErroNotacao: retorna null para notação correta")
    void obterErroNotacaoNull() {
        assertNull(NotacaoValidator.obterErroNotacao("e4", NotacaoXadrez.INGLESA));
        assertNull(NotacaoValidator.obterErroNotacao("Nf3", NotacaoXadrez.INGLESA));
        assertNull(NotacaoValidator.obterErroNotacao("e4", NotacaoXadrez.PORTUGUESA));
        assertNull(NotacaoValidator.obterErroNotacao("Cf3", NotacaoXadrez.PORTUGUESA));
    }

    @Test
    @DisplayName("validarNotacao: rejeita entrada vazia ou null")
    void validarNotacaoVaziaOuNull() {
        assertFalse(NotacaoValidator.validarNotacao(null, NotacaoXadrez.INGLESA));
        assertFalse(NotacaoValidator.validarNotacao("", NotacaoXadrez.INGLESA));
        assertFalse(NotacaoValidator.validarNotacao("   ", NotacaoXadrez.INGLESA));
    }
}
