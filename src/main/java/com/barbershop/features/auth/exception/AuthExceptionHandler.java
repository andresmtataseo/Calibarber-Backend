package com.barbershop.features.auth.exception;

import com.barbershop.common.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones específico para el módulo de autenticación.
 * Captura y maneja las excepciones relacionadas con la autenticación y validación.
 */
@RestControllerAdvice(basePackages = "com.barbershop.features.auth")
@Slf4j
public class AuthExceptionHandler {

    /**
     * Maneja excepciones de credenciales inválidas.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Intento de inicio de sesión con credenciales inválidas: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    /**
     * Maneja excepciones cuando un usuario ya existe.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        log.warn("Intento de registro con email ya existente: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(HttpStatus.CONFLICT, ex.getMessage()));
    }

    /**
     * Maneja excepciones de contraseñas que no coinciden.
     */
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiResponseDto<Void>> handlePasswordMismatch(PasswordMismatchException ex) {
        log.warn("Error de validación de contraseña: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    /**
     * Maneja excepciones de tokens inválidos.
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidToken(InvalidTokenException ex) {
        log.warn("Token inválido o expirado: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    /**
     * Maneja excepciones de autenticación de Spring Security.
     */
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiResponseDto<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Error de autenticación: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error(
                        HttpStatus.UNAUTHORIZED, 
                        "Credenciales inválidas. Verifique su email y contraseña."
                ));
    }

    /**
     * Maneja errores de validación de campos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        log.warn("Errores de validación en campos: {}", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(
                        HttpStatus.BAD_REQUEST,
                        "Errores de validación en los datos proporcionados",
                        fieldErrors
                ));
    }

    /**
     * Maneja excepciones generales no capturadas específicamente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(Exception ex) {
        log.error("Error inesperado en el módulo de autenticación: ", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Ha ocurrido un error interno. Por favor, inténtelo más tarde."
                ));
    }
}