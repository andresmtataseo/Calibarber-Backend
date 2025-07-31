package com.barbershop.common.exception;

import com.barbershop.common.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Captura excepciones no manejadas por manejadores específicos de módulos.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de validación de campos (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        log.warn("Errores de validación global: {}", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(
                        HttpStatus.BAD_REQUEST,
                        "Error de validación en los campos de entrada",
                        fieldErrors
                ));
    }

    /**
     * Maneja IllegalArgumentException (argumentos inválidos).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        log.warn("Argumento ilegal: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    /**
     * Maneja BadCredentialsException (credenciales inválidas).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        log.warn("Credenciales inválidas: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciales de autenticación inválidas. Verifique su email y contraseña."
                ));
    }

    /**
     * Manejador genérico para cualquier otra excepción no esperada.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        log.error("Error inesperado: {} - {}", ex.getClass().getName(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Ocurrió un error inesperado en el servidor. Por favor, inténtelo de nuevo más tarde."
                ));
    }
}