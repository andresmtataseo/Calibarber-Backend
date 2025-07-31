package com.barbershop.features.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración para el módulo de autenticación.
 * Permite configurar aspectos como la expiración de tokens, longitud de contraseñas, etc.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /**
     * Configuración de JWT
     */
    private Jwt jwt = new Jwt();

    /**
     * Configuración de contraseñas
     */
    private Password password = new Password();

    /**
     * Configuración de tokens de restablecimiento
     */
    private ResetToken resetToken = new ResetToken();

    @Data
    public static class Jwt {
        /**
         * Tiempo de expiración del token JWT en milisegundos (por defecto 24 horas)
         */
        private long expirationTime = 86400000L; // 24 horas

        /**
         * Clave secreta para firmar los tokens JWT
         */
        private String secretKey = "defaultSecretKey";

        /**
         * Emisor del token
         */
        private String issuer = "Calibarber-Backend";
    }

    @Data
    public static class Password {
        /**
         * Longitud mínima de la contraseña
         */
        private int minLength = 8;

        /**
         * Longitud máxima de la contraseña
         */
        private int maxLength = 128;

        /**
         * Requiere al menos una letra mayúscula
         */
        private boolean requireUppercase = true;

        /**
         * Requiere al menos una letra minúscula
         */
        private boolean requireLowercase = true;

        /**
         * Requiere al menos un dígito
         */
        private boolean requireDigit = true;

        /**
         * Requiere al menos un carácter especial
         */
        private boolean requireSpecialChar = true;

        /**
         * Caracteres especiales permitidos
         */
        private String allowedSpecialChars = "@$!%*?&";
    }

    @Data
    public static class ResetToken {
        /**
         * Tiempo de expiración del token de restablecimiento en milisegundos (por defecto 1 hora)
         */
        private long expirationTime = 3600000L; // 1 hora

        /**
         * Longitud del token de restablecimiento
         */
        private int tokenLength = 32;
    }
}