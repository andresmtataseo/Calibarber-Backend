package com.barbershop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuración para habilitar el mecanismo de reintentos en Spring
 */
@Configuration
@EnableRetry
public class RetryConfig {
    // La anotación @EnableRetry habilita el procesamiento de @Retryable
}