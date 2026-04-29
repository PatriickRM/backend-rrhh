package com.rrhh.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Habilita @Async en Spring y configura el pool de hilos para tareas asíncronas.
 *
 * El envío de emails usa @Async — el request HTTP retorna inmediatamente
 * y el email se envía en un hilo del pool "emailTaskExecutor".
 *
 * Configuración del pool:
 * - corePoolSize 2:  siempre hay 2 hilos disponibles para emails
 * - maxPoolSize 5:   máximo 5 envíos concurrentes
 * - queueCapacity 50: cola de hasta 50 emails si el pool está ocupado
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("email-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
