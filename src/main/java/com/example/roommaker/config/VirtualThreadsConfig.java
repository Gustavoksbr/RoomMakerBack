package com.example.roommaker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

/**
 * Configuração para habilitar Virtual Threads (Java 21) no Spring Boot.
 * 
 * Virtual Threads são threads leves que permitem alta concorrência com baixo
 * overhead.
 * Ideal para aplicações com muitas operações I/O como WebSockets, chamadas HTTP
 * e banco de dados.
 * 
 * Benefícios:
 * - Milhares de threads concorrentes com baixo uso de memória
 * - Melhor performance em operações I/O bound
 * - Código mais simples e legível (sem callbacks complexos)
 * - Ideal para WebSockets e operações assíncronas
 */
@Configuration
@EnableAsync
public class VirtualThreadsConfig implements AsyncConfigurer {

    /**
     * Configura o executor de tarefas assíncronas para usar Virtual Threads.
     * 
     * Este executor será usado por:
     * - Métodos anotados com @Async
     * - Tarefas agendadas (@Scheduled)
     * - WebSocket handlers
     * - Operações assíncronas do Spring
     */
    @Bean
    @Override
    public AsyncTaskExecutor getAsyncExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Bean adicional para uso explícito em componentes que precisam de um executor.
     * Útil para injeção direta em serviços que gerenciam suas próprias tarefas
     * assíncronas.
     */
    @Bean(name = "virtualThreadExecutor")
    public AsyncTaskExecutor virtualThreadExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
