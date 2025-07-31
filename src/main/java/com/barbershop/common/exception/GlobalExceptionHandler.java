package com.barbershop.common.exception;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.features.auth.exception.InvalidCredentialsException;
import com.barbershop.features.auth.exception.InvalidResetTokenException;
import com.barbershop.features.auth.exception.InvalidTokenException;
import com.barbershop.features.auth.exception.PasswordMismatchException;
import com.barbershop.features.auth.exception.UserAlreadyExistsException;
import com.barbershop.features.auth.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;
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
     * Maneja EntityNotFoundException (entidad no encontrada).
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        
        log.warn("Entidad no encontrada: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(HttpStatus.NOT_FOUND, "El recurso solicitado no fue encontrado"));
    }

    /**
     * Maneja DataIntegrityViolationException (violación de integridad de datos).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        
        log.warn("Violación de integridad de datos: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(
                        HttpStatus.CONFLICT, 
                        "Error de integridad de datos. Verifique que los datos no estén duplicados o que cumplan las restricciones."
                ));
    }

    /**
     * Maneja AccessDeniedException (acceso denegado).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        log.warn("Acceso denegado: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDto.error(
                        HttpStatus.FORBIDDEN, 
                        "No tiene permisos para acceder a este recurso"
                ));
    }

    /**
     * Maneja HttpRequestMethodNotSupportedException (método HTTP no soportado).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        
        log.warn("Método HTTP no soportado: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponseDto.error(
                        HttpStatus.METHOD_NOT_ALLOWED, 
                        "Método HTTP no permitido para este endpoint"
                ));
    }

    /**
     * Maneja HttpMessageNotReadableException (cuerpo de petición no legible).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        log.warn("Cuerpo de petición no legible: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(
                        HttpStatus.BAD_REQUEST, 
                        "El formato del cuerpo de la petición es inválido"
                ));
    }

    /**
     * Maneja MissingServletRequestParameterException (parámetro requerido faltante).
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {
        
        log.warn("Parámetro requerido faltante: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(
                        HttpStatus.BAD_REQUEST, 
                        "Parámetro requerido faltante: " + ex.getParameterName()
                ));
    }

    /**
     * Maneja ResourceNotFoundException (recurso no encontrado).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /**
     * Maneja BusinessLogicException (error de lógica de negocio).
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBusinessLogicException(
            BusinessLogicException ex, WebRequest request) {
        
        log.warn("Error de lógica de negocio: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    /**
     * Maneja ResourceAlreadyExistsException (recurso ya existe).
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex, WebRequest request) {
        
        log.warn("Recurso ya existe: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(HttpStatus.CONFLICT, ex.getMessage()));
    }

    /**
     * Maneja MethodArgumentTypeMismatchException (tipo de argumento incorrecto).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        log.warn("Tipo de argumento incorrecto: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(
                        HttpStatus.BAD_REQUEST, 
                        "Tipo de dato incorrecto para el parámetro: " + ex.getName()
                ));
    }

    // ==================== MANEJADORES DE EXCEPCIONES DE AUTENTICACIÓN ====================

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
     * Maneja excepciones cuando un usuario no es encontrado.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("Usuario no encontrado: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /**
     * Maneja excepciones de tokens de restablecimiento inválidos.
     */
    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidResetTokenException(InvalidResetTokenException ex) {
        log.warn("Token de restablecimiento inválido: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    // ==================== MANEJADOR GENÉRICO ====================
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