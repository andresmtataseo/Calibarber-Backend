package com.barbershop.common.exception;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.features.auth.exception.InvalidCredentialsException;
import com.barbershop.features.auth.exception.InvalidResetTokenException;
import com.barbershop.features.auth.exception.InvalidTokenException;
import com.barbershop.features.auth.exception.PasswordMismatchException;
import com.barbershop.features.auth.exception.UserAlreadyExistsException;
import com.barbershop.features.auth.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de validación de campos (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<String>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Error de validación en los campos de entrada")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .errors(fieldErrors)
                        .build()
        );
    }

    /**
     * Maneja IllegalArgumentException (argumentos inválidos).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<String>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Error de argumentos inválidos: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja EntityNotFoundException (entidad no encontrada).
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDto<String>> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("El recurso solicitado no fue encontrado: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja DataIntegrityViolationException (violación de integridad de datos).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<String>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        String userFriendlyMessage = parseDataIntegrityError(ex.getMessage(), request.getRequestURI());
        
        log.error("Error de integridad de datos en {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.CONFLICT.value())
                        .message(userFriendlyMessage)
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Analiza el mensaje de error de integridad de datos y devuelve un mensaje amigable para el usuario.
     */
    private String parseDataIntegrityError(String errorMessage, String requestPath) {
        if (errorMessage == null) {
            return "Error de integridad de datos. Por favor, verifique los datos enviados.";
        }

        String lowerErrorMessage = errorMessage.toLowerCase();
        
        // Errores de clave foránea
        if (lowerErrorMessage.contains("foreign key") || lowerErrorMessage.contains("llave foránea")) {
            if (lowerErrorMessage.contains("barbershop_id")) {
                return "La barbería especificada no existe. Por favor, seleccione una barbería válida.";
            }
            if (lowerErrorMessage.contains("barber_id")) {
                return "El barbero especificado no existe. Por favor, seleccione un barbero válido.";
            }
            if (lowerErrorMessage.contains("user_id")) {
                return "El usuario especificado no existe. Por favor, verifique el usuario.";
            }
            if (lowerErrorMessage.contains("service_id")) {
                return "El servicio especificado no existe. Por favor, seleccione un servicio válido.";
            }
            if (lowerErrorMessage.contains("appointment_id")) {
                return "La cita especificada no existe. Por favor, verifique la cita.";
            }
            return "Existe una referencia a un registro que no existe. Por favor, verifique los datos relacionados.";
        }
        
        // Errores de clave única
        if (lowerErrorMessage.contains("unique") || lowerErrorMessage.contains("duplicate") || lowerErrorMessage.contains("duplicado")) {
            if (lowerErrorMessage.contains("email")) {
                return "El correo electrónico ya está registrado. Por favor, use un correo diferente.";
            }
            if (lowerErrorMessage.contains("phone") || lowerErrorMessage.contains("telefono")) {
                return "El número de teléfono ya está registrado. Por favor, use un número diferente.";
            }
            if (lowerErrorMessage.contains("name") && requestPath.contains("barbershops")) {
                return "Ya existe una barbería con ese nombre. Por favor, elija un nombre diferente.";
            }
            if (lowerErrorMessage.contains("name") && requestPath.contains("services")) {
                return "Ya existe un servicio con ese nombre en esta barbería. Por favor, elija un nombre diferente.";
            }
            return "Ya existe un registro con esos datos. Por favor, verifique la información ingresada.";
        }
        
        // Errores de restricción NOT NULL
        if (lowerErrorMessage.contains("not null") || lowerErrorMessage.contains("no puede ser nulo")) {
            if (lowerErrorMessage.contains("email")) {
                return "El correo electrónico es obligatorio.";
            }
            if (lowerErrorMessage.contains("name") || lowerErrorMessage.contains("nombre")) {
                return "El nombre es obligatorio.";
            }
            if (lowerErrorMessage.contains("password")) {
                return "La contraseña es obligatoria.";
            }
            if (lowerErrorMessage.contains("price") || lowerErrorMessage.contains("precio")) {
                return "El precio es obligatorio.";
            }
            if (lowerErrorMessage.contains("duration") || lowerErrorMessage.contains("duracion")) {
                return "La duración es obligatoria.";
            }
            return "Faltan campos obligatorios. Por favor, complete toda la información requerida.";
        }
        
        // Errores de restricción CHECK
        if (lowerErrorMessage.contains("check constraint") || lowerErrorMessage.contains("restricción check")) {
            if (lowerErrorMessage.contains("price") || lowerErrorMessage.contains("precio")) {
                return "El precio debe ser mayor a cero.";
            }
            if (lowerErrorMessage.contains("duration") || lowerErrorMessage.contains("duracion")) {
                return "La duración debe ser mayor a cero minutos.";
            }
            if (lowerErrorMessage.contains("email")) {
                return "El formato del correo electrónico no es válido.";
            }
            return "Los datos ingresados no cumplen con las restricciones del sistema.";
        }
        
        // Error genérico
        return "Error de integridad de datos. Por favor, verifique que todos los datos sean correctos y que las referencias existan.";
    }

    /**
     * Maneja AccessDeniedException (acceso denegado).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<String>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .message("No tiene permisos para acceder a este recurso: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja HttpRequestMethodNotSupportedException (método HTTP no soportado).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDto<String>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                        .message("Método HTTP no permitido para este endpoint: " + ex.getMethod() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja HttpMessageNotReadableException (cuerpo de petición no legible).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<String>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("El formato del cuerpo de la petición es inválido: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja MissingServletRequestParameterException (parámetro requerido faltante).
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseDto<String>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Parámetro requerido faltante: " + ex.getParameterName() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja ResourceNotFoundException (recurso no encontrado).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<String>> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Recurso no encontrado: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja BusinessLogicException (error de lógica de negocio).
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ApiResponseDto<String>> handleBusinessLogicException(BusinessLogicException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Error de lógica de negocio: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja ResourceAlreadyExistsException (recurso ya existe).
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<String>> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.CONFLICT.value())
                        .message("Recurso ya existe: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja MethodArgumentTypeMismatchException (tipo de argumento incorrecto).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseDto<String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Tipo de argumento incorrecto: " + ex.getName() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    // ==================== MANEJADORES DE EXCEPCIONES DE AUTENTICACIÓN ====================

    /**
     * Maneja excepciones de credenciales inválidas.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponseDto<String>> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .message("Credenciales inválidas: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja excepciones cuando un usuario ya existe.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<String>> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.CONFLICT.value())
                        .message("Usuario ya existe: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja excepciones de contraseñas que no coinciden.
     */
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiResponseDto<String>> handlePasswordMismatch(PasswordMismatchException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Contraseñas no coinciden: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja excepciones de tokens inválidos.
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponseDto<String>> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .message("Token inválido o expirado: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja excepciones de autenticación de Spring Security.
     */
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiResponseDto<String>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .message("Error de autenticación: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja excepciones cuando un usuario no es encontrado.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDto<String>> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Usuario no encontrado: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    /**
     * Maneja excepciones de tokens de restablecimiento inválidos.
     */
    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<ApiResponseDto<String>> handleInvalidResetTokenException(InvalidResetTokenException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Token de restablecimiento inválido: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    // ==================== MANEJADOR GENÉRICO ====================
    /**
     * Manejador genérico para cualquier otra excepción no esperada.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<String>> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("Error inesperado: " + ex.getMessage() + ".")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }
}