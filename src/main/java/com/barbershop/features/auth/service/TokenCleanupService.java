package com.barbershop.features.auth.service;

import com.barbershop.features.auth.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio para la limpieza automática de tokens de restablecimiento expirados
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Limpia tokens de restablecimiento expirados
     * Se ejecuta cada hora
     */
    @Scheduled(fixedRate = 3600000) // 1 hora en milisegundos
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Iniciando limpieza de tokens de restablecimiento expirados");
        
        try {
            int deletedTokens = passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            
            if (deletedTokens > 0) {
                log.info("Se eliminaron {} tokens de restablecimiento expirados", deletedTokens);
            } else {
                log.debug("No se encontraron tokens expirados para eliminar");
            }
        } catch (Exception e) {
            log.error("Error durante la limpieza de tokens expirados: {}", e.getMessage(), e);
        }
    }

    /**
     * Limpia tokens de restablecimiento expirados manualmente
     * @return Número de tokens eliminados
     */
    @Transactional
    public int cleanupExpiredTokensManually() {
        log.info("Limpieza manual de tokens de restablecimiento expirados");
        
        try {
            int deletedTokens = passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            log.info("Se eliminaron {} tokens de restablecimiento expirados manualmente", deletedTokens);
            return deletedTokens;
        } catch (Exception e) {
            log.error("Error durante la limpieza manual de tokens expirados: {}", e.getMessage(), e);
            throw e;
        }
    }
}