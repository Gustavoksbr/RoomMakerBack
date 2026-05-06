package com.example.roommaker.app.categorias.examples.xadrez.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita scheduling para verificação de timeout.
 * 
 * O scheduler é LEVE e apenas verifica timeout, não decrementa tempo.
 */
@Configuration
@EnableScheduling
public class XadrezSchedulingConfig {
}
