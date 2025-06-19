package com.barbershop.common.exception;

import com.barbershop.common.dto.ApiResponseDto; // Updated import
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest; // Importa WebRequest

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; // Importar Collectors

@RestControllerAdvice // Esta anotación reemplaza @ControllerAdvice y ya implica @ResponseBody
public class GlobalExceptionHandler {

    // --- Maneja excepciones de validación (@Valid) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage) // Solo toma el mensaje de error
                .collect(Collectors.toList());

        ApiResponseDto apiError = ApiResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .message("Error de validación en los campos de entrada.")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .errors(errors) // Incluimos los mensajes de error de campo
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // --- Maneja IllegalArgumentException (ej. "email ya existe") ---
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ApiResponseDto apiError = ApiResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .message(ex.getMessage()) // Mensaje específico de la excepción
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // --- Maneja BadCredentialsException (autenticación fallida) ---
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDto> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        ApiResponseDto apiError = ApiResponseDto.builder()
                .status(HttpStatus.UNAUTHORIZED.value()) // 401
                .message("Credenciales de autenticación inválidas. Verifique su email y contraseña.")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    // --- Manejador genérico para cualquier otra excepción no esperada ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto> handleGlobalException(Exception ex, WebRequest request) {
        // Loggear la excepción completa para depuración en el servidor, pero no enviar detalles sensibles al cliente
        System.err.println("Error inesperado: " + ex.getClass().getName() + " - " + ex.getMessage());
        ex.printStackTrace(); // Esto es bueno para la depuración en desarrollo, pero considera un logger en producción

        ApiResponseDto apiError = ApiResponseDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()) // 500
                .message("Ocurrió un error inesperado en el servidor. Por favor, inténtelo de nuevo más tarde.")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}