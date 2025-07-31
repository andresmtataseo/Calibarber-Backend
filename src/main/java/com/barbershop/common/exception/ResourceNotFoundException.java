package com.barbershop.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando un recurso solicitado no es encontrado.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException() {
        super("El recurso solicitado no fue encontrado");
    }
    
    public ResourceNotFoundException(String resourceName) {
        super(resourceName + " no fue encontrado");
    }
    
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(resourceName + " con identificador '" + identifier + "' no fue encontrado");
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}