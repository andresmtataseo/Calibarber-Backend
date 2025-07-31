package com.barbershop.features.auth.exception;

/**
 * Excepción lanzada cuando un usuario no es encontrado en el sistema.
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}