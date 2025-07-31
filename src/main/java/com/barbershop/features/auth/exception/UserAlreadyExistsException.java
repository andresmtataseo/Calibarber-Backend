package com.barbershop.features.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException() {
        super("Ya existe un usuario registrado con este email");
    }
    
    public UserAlreadyExistsException(String email) {
        super("Ya existe un usuario registrado con el email: " + email);
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}