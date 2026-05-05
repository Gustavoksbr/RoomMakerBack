package com.example.roommaker.app.categorias.examples.xadrez;

import com.example.roommaker.app.categorias.examples.xadrez.domain.NotacaoConverter;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.NotacaoXadrez;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o conversor de notação portuguesa/inglesa.
 */
class NotacaoConverterTest {

    @Test
    @DisplayName("paraIngles: notação inglesa não é modificada")
    void paraInglesComNotacaoInglesa() {
        assertEquals("e4", NotacaoConverter.paraIngles("e4", NotacaoXadrez.INGLESA));
        assertEquals("Nf3", NotacaoConverter.paraIngles("Nf3", NotacaoXadrez.INGLESA));
        assertEquals("Qxd5", NotacaoConverter.paraIngles("Qxd5", NotacaoXadrez.INGLESA));
        assertEquals("O-O", NotacaoConverter.paraIngles("O-O", NotacaoXadrez.INGLESA));
    }

    @Test
    @DisplayName("paraIngles: converte notação portuguesa para inglesa")
    void paraInglesComNotacaoPortuguesa() {
        // Rei: R -> K
        assertEquals("Ke2", NotacaoConverter.paraIngles("Re2", NotacaoXadrez.PORTUGUESA));

        // Dama: D -> Q
        assertEquals("Qd4", NotacaoConverter.paraIngles("Dd4", NotacaoXadrez.PORTUGUESA));
        assertEquals("Qxd5", NotacaoConverter.paraIngles("Dxd5", NotacaoXadrez.PORTUGUESA));

        // Torre: T -> R
        assertEquals("Rf1", NotacaoConverter.paraIngles("Tf1", NotacaoXadrez.PORTUGUESA));
        assertEquals("Rxe4", NotacaoConverter.paraIngles("Txe4", NotacaoXadrez.PORTUGUESA));

        // Cavalo: C -> N
        assertEquals("Nf3", NotacaoConverter.paraIngles("Cf3", NotacaoXadrez.PORTUGUESA));
        assertEquals("Nxe5", NotacaoConverter.paraIngles("Cxe5", NotacaoXadrez.PORTUGUESA));

        // Bispo: B permanece B
        assertEquals("Bc4", NotacaoConverter.paraIngles("Bc4", NotacaoXadrez.PORTUGUESA));

        // Peão: sem letra
        assertEquals("e4", NotacaoConverter.paraIngles("e4", NotacaoXadrez.PORTUGUESA));
        assertEquals("exd5", NotacaoConverter.paraIngles("exd5", NotacaoXadrez.PORTUGUESA));

        // Roque
        assertEquals("O-O", NotacaoConverter.paraIngles("O-O", NotacaoXadrez.PORTUGUESA));
        assertEquals("O-O-O", NotacaoConverter.paraIngles("O-O-O", NotacaoXadrez.PORTUGUESA));
    }

    @Test
    @DisplayName("deIngles: notação inglesa não é modificada")
    void deInglesComNotacaoInglesa() {
        assertEquals("e4", NotacaoConverter.deIngles("e4", NotacaoXadrez.INGLESA));
        assertEquals("Nf3", NotacaoConverter.deIngles("Nf3", NotacaoXadrez.INGLESA));
        assertEquals("Qxd5", NotacaoConverter.deIngles("Qxd5", NotacaoXadrez.INGLESA));
        assertEquals("O-O", NotacaoConverter.deIngles("O-O", NotacaoXadrez.INGLESA));
    }

    @Test
    @DisplayName("deIngles: converte notação inglesa para portuguesa")
    void deInglesComNotacaoPortuguesa() {
        // Rei: K -> R
        assertEquals("Re2", NotacaoConverter.deIngles("Ke2", NotacaoXadrez.PORTUGUESA));

        // Dama: Q -> D
        assertEquals("Dd4", NotacaoConverter.deIngles("Qd4", NotacaoXadrez.PORTUGUESA));
        assertEquals("Dxd5", NotacaoConverter.deIngles("Qxd5", NotacaoXadrez.PORTUGUESA));

        // Torre: R -> T
        assertEquals("Tf1", NotacaoConverter.deIngles("Rf1", NotacaoXadrez.PORTUGUESA));
        assertEquals("Txe4", NotacaoConverter.deIngles("Rxe4", NotacaoXadrez.PORTUGUESA));

        // Cavalo: N -> C
        assertEquals("Cf3", NotacaoConverter.deIngles("Nf3", NotacaoXadrez.PORTUGUESA));
        assertEquals("Cxe5", NotacaoConverter.deIngles("Nxe5", NotacaoXadrez.PORTUGUESA));

        // Bispo: B permanece B
        assertEquals("Bc4", NotacaoConverter.deIngles("Bc4", NotacaoXadrez.PORTUGUESA));

        // Peão: sem letra
        assertEquals("e4", NotacaoConverter.deIngles("e4", NotacaoXadrez.PORTUGUESA));
        assertEquals("exd5", NotacaoConverter.deIngles("exd5", NotacaoXadrez.PORTUGUESA));

        // Roque
        assertEquals("O-O", NotacaoConverter.deIngles("O-O", NotacaoXadrez.PORTUGUESA));
        assertEquals("O-O-O", NotacaoConverter.deIngles("O-O-O", NotacaoXadrez.PORTUGUESA));
    }

    @Test
    @DisplayName("paraIngles: entrada null retorna null")
    void paraInglesComNull() {
        assertNull(NotacaoConverter.paraIngles(null, NotacaoXadrez.PORTUGUESA));
        assertNull(NotacaoConverter.paraIngles(null, NotacaoXadrez.INGLESA));
    }

    @Test
    @DisplayName("deIngles: entrada null retorna null")
    void deInglesComNull() {
        assertNull(NotacaoConverter.deIngles(null, NotacaoXadrez.PORTUGUESA));
        assertNull(NotacaoConverter.deIngles(null, NotacaoXadrez.INGLESA));
    }

    @Test
    @DisplayName("Conversão bidirecional: português -> inglês -> português")
    void conversaoBidirecional() {
        String[] lancesPortugues = { "e4", "e5", "Cf3", "Cc6", "Bc4", "Bc5", "Dd5", "Tf6", "Re2" };

        for (String lancePortugues : lancesPortugues) {
            String lanceIngles = NotacaoConverter.paraIngles(lancePortugues, NotacaoXadrez.PORTUGUESA);
            String lancePortuguesNovamente = NotacaoConverter.deIngles(lanceIngles, NotacaoXadrez.PORTUGUESA);
            assertEquals(lancePortugues, lancePortuguesNovamente,
                    "Conversão bidirecional falhou para: " + lancePortugues);
        }
    }
}
