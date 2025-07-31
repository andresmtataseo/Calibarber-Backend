package com.barbershop.features.auth.exception;

/**
 * Excepción lanzada cuando se intenta usar un token de restablecimiento que no existe o no es válido.
 */
public class InvalidResetTokenException extends RuntimeException {
    
    public InvalidResetTokenException(String message) {
        super(message);
    }
    
    public InvalidResetTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}