package com.example.roommaker.app.categorias.examples.xadrez;

import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezLogica;
import com.example.roommaker.app.categorias.examples.xadrez.domain.XadrezLogica.*;
import com.github.bhlangonijr.chesslib.Board;

public class TesteAmbiguidade {
    public static void main(String[] args) {
        // Teste 1: Torres em a1 e h1 podem ir para d1
        System.out.println("=== Teste 1: Rd1 com duas torres ===");
        Board board1 = new Board();
        board1.loadFromFen("4k3/8/8/8/8/8/8/R2K3R w - - 0 1");
        Classificacao c1 = XadrezLogica.classificarEntrada(board1, "Rd1");
        System.out.println("Resultado: " + c1.tipo());
        System.out.println("Esperado: LANCE_AMBIGUO");
        System.out.println();

        // Teste 2: Rad1 com desambiguação
        System.out.println("=== Teste 2: Rad1 com desambiguação ===");
        Board board2 = new Board();
        board2.loadFromFen("4k3/8/8/8/8/8/8/R2K3R w - - 0 1");
        Classificacao c2 = XadrezLogica.classificarEntrada(board2, "Rad1");
        System.out.println("Resultado: " + c2.tipo());
        System.out.println("Esperado: VALIDO");
        System.out.println();

        // Teste 3: Ke2 (rei nunca tem ambiguidade)
        System.out.println("=== Teste 3: Ke2 (rei) ===");
        Board board3 = new Board();
        Classificacao c3 = XadrezLogica.classificarEntrada(board3, "Ke2");
        System.out.println("Resultado: " + c3.tipo());
        System.out.println("Esperado: VALIDO");
        System.out.println();

        // Teste 4: Cavalos em g1 e d2 podem ir para f3
        System.out.println("=== Teste 4: Nf3 com dois cavalos ===");
        Board board4 = new Board();
        board4.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPNPPPP/R1BQKBNR w KQkq - 0 1");
        Classificacao c4 = XadrezLogica.classificarEntrada(board4, "Nf3");
        System.out.println("Resultado: " + c4.tipo());
        System.out.println("Esperado: LANCE_AMBIGUO");
    }
}
