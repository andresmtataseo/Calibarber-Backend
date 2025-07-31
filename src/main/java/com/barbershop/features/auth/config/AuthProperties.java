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
         * Tiempo de expiración del token JWT en milisegundos
         * Debe configurarse en application.properties como: app.auth.jwt.expiration-time
         */
        private long expirationTime;

        /**
         * Clave secreta para firmar los tokens JWT
         * Debe configurarse en application.properties como: app.auth.jwt.secret-key
         * IMPORTANTE: Usar una clave segura en producción
         */
        private String secretKey;

        /**
         * Emisor del token
         * Debe configurarse en application.properties como: app.auth.jwt.issuer
         */
        private String issuer;
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
         * Tiempo de expiración del token de restablecimiento en milisegundos
         * Debe configurarse en application.properties como: app.auth.reset-token.expiration-time
         */
        private long expirationTime;

        /**
         * Longitud del token de restablecimiento
         * Debe configurarse en application.properties como: app.auth.reset-token.token-length
         */
        private int tokenLength;
    }
}