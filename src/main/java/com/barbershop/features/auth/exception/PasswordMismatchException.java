package com.barbershop.features.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordMismatchException extends RuntimeException {
    
    public PasswordMismatchException() {
        super("Las contraseñas no coinciden");
    }
    
    public PasswordMismatchException(String message) {
        super(message);
    }
    
    public PasswordMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}