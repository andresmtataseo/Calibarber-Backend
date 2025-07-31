package com.barbershop.features.auth.util;

import com.barbershop.features.auth.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Utilidades para el módulo de autenticación.
 * Proporciona métodos auxiliares para validación de contraseñas, generación de tokens, etc.
 */
@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final AuthProperties authProperties;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Valida si una contraseña cumple con los requisitos de seguridad configurados.
     *
     * @param password La contraseña a validar
     * @return true si la contraseña es válida, false en caso contrario
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        AuthProperties.Password passwordConfig = authProperties.getPassword();

        // Verificar longitud
        if (password.length() < passwordConfig.getMinLength() || 
            password.length() > passwordConfig.getMaxLength()) {
            return false;
        }

        // Verificar mayúscula
        if (passwordConfig.isRequireUppercase() && !Pattern.compile("[A-Z]").matcher(password).find()) {
            return false;
        }

        // Verificar minúscula
        if (passwordConfig.isRequireLowercase() && !Pattern.compile("[a-z]").matcher(password).find()) {
            return false;
        }

        // Verificar dígito
        if (passwordConfig.isRequireDigit() && !Pattern.compile("\\d").matcher(password).find()) {
            return false;
        }

        // Verificar carácter especial
        if (passwordConfig.isRequireSpecialChar()) {
            String specialChars = Pattern.quote(passwordConfig.getAllowedSpecialChars());
            if (!Pattern.compile("[" + specialChars + "]").matcher(password).find()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Genera un token aleatorio seguro para restablecimiento de contraseña.
     *
     * @return Token aleatorio
     */
    public String generateResetToken() {
        int tokenLength = authProperties.getResetToken().getTokenLength();
        StringBuilder token = new StringBuilder(tokenLength);
        
        for (int i = 0; i < tokenLength; i++) {
            token.append(CHARACTERS.charAt(SECURE_RANDOM.nextInt(CHARACTERS.length())));
        }
        
        return token.toString();
    }

    /**
     * Valida si un email tiene un formato válido.
     *
     * @param email El email a validar
     * @return true si el email es válido, false en caso contrario
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.compile(emailRegex).matcher(email.trim()).matches();
    }

    /**
     * Normaliza un email eliminando espacios y convirtiendo a minúsculas.
     *
     * @param email El email a normalizar
     * @return Email normalizado
     */
    public String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Genera un mensaje de error personalizado para contraseñas inválidas.
     *
     * @return Mensaje de error con los requisitos de contraseña
     */
    public String getPasswordRequirementsMessage() {
        AuthProperties.Password passwordConfig = authProperties.getPassword();
        StringBuilder message = new StringBuilder("La contraseña debe tener:");
        
        message.append(" entre ").append(passwordConfig.getMinLength())
               .append(" y ").append(passwordConfig.getMaxLength()).append(" caracteres");

        if (passwordConfig.isRequireUppercase()) {
            message.append(", al menos una letra mayúscula");
        }

        if (passwordConfig.isRequireLowercase()) {
            message.append(", al menos una letra minúscula");
        }

        if (passwordConfig.isRequireDigit()) {
            message.append(", al menos un dígito");
        }

        if (passwordConfig.isRequireSpecialChar()) {
            message.append(", al menos un carácter especial (")
                   .append(passwordConfig.getAllowedSpecialChars()).append(")");
        }

        return message.toString();
    }

    /**
     * Verifica si un token de restablecimiento ha expirado.
     *
     * @param tokenCreationTime Tiempo de creación del token en milisegundos
     * @return true si el token ha expirado, false en caso contrario
     */
    public boolean isResetTokenExpired(long tokenCreationTime) {
        long currentTime = System.currentTimeMillis();
        long expirationTime = authProperties.getResetToken().getExpirationTime();
        return (currentTime - tokenCreationTime) > expirationTime;
    }
}