package com.barbershop.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se intenta crear un recurso que ya existe.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceAlreadyExistsException extends RuntimeException {
    
    public ResourceAlreadyExistsException() {
        super("El recurso ya existe");
    }
    
    public ResourceAlreadyExistsException(String resourceName) {
        super(resourceName + " ya existe");
    }
    
    public ResourceAlreadyExistsException(String resourceName, String identifier) {
        super(resourceName + " con identificador '" + identifier + "' ya existe");
    }
    
    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}