package com.barbershop.features.auth.exception;

import com.barbershop.common.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
     * Maneja excepciones generales no capturadas específicamente en el módulo de autenticación.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(Exception ex) {
        log.error("Error inesperado en el módulo de autenticación: ", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Ha ocurrido un error interno en el sistema de autenticación. Por favor, inténtelo más tarde."
                ));
    }
}