package com.barbershop.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * Servicio para manejar reintentos de envío de emails con backoff exponencial
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailRetryService {

    private final EmailService emailService;

    /**
     * Envía email de bienvenida con reintentos automáticos
     * @param destinatario Email del destinatario
     * @param nombreUsuario Nombre del usuario
     * @throws Exception si todos los reintentos fallan
     */
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void enviarEmailBienvenidaConReintentos(String destinatario, String nombreUsuario) throws Exception {
        log.debug("Intentando enviar email de bienvenida a: {}", destinatario);
        emailService.enviarEmailBienvenida(destinatario, nombreUsuario);
        log.info("Email de bienvenida enviado exitosamente a: {}", destinatario);
    }

    /**
     * Envía token de recuperación con reintentos automáticos
     * @param destinatario Email del destinatario
     * @param nombreUsuario Nombre del usuario
     * @param token Token de recuperación
     * @param tiempoExpiracion Tiempo de expiración en minutos
     * @throws Exception si todos los reintentos fallan
     */
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void enviarTokenRecuperacionConReintentos(String destinatario, String nombreUsuario, 
                                                    String token, int tiempoExpiracion) throws Exception {
        log.debug("Intentando enviar token de recuperación a: {}", destinatario);
        emailService.enviarTokenRecuperacion(destinatario, nombreUsuario, token, tiempoExpiracion);
        log.info("Token de recuperación enviado exitosamente a: {}", destinatario);
    }

    /**
     * Maneja el fallo final después de todos los reintentos
     * @param ex Excepción que causó el fallo
     * @param destinatario Email del destinatario
     */
    public void handleEmailFailure(Exception ex, String destinatario) {
        log.error("Falló el envío de email después de todos los reintentos para {}: {} - Tipo: {}", 
                 destinatario, ex.getMessage(), ex.getClass().getSimpleName());
    }
}