package com.barbershop.features.barbershop.exception;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para el módulo de horarios de operación de barberías.
 * Proporciona respuestas consistentes y apropiadas para diferentes tipos de errores.
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.barbershop.features.barbershop.controller")
public class BarbershopOperatingHoursExceptionHandler {

    /**
     * Maneja excepciones cuando no se encuentra un recurso (barbería, horario, etc.).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        
        ApiResponseDto<Object> response = ApiResponseDto.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .data(null)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Maneja excepciones de validación de argumentos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        log.warn("Error de validación: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ApiResponseDto<Object> response = ApiResponseDto.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Datos de entrada inválidos")
            .data(null)
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Maneja excepciones de violación de restricciones de validación.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        log.warn("Error de restricción de validación: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        int index = 1;
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put("constraint_" + index++, violation.getMessage());
        }
        
        ApiResponseDto<Object> response = ApiResponseDto.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Violación de restricciones de validación")
            .data(null)
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Maneja excepciones cuando falta un parámetro requerido en la request.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleMissingParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        log.warn("Parámetro faltante: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getParameterName(), 
                  String.format("Parámetro requerido de tipo '%s' no está presente", ex.getParameterType()));
        
        ApiResponseDto<Object> response = ApiResponseDto.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Parámetro requerido faltante")
            .data(null)
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Maneja excepciones de tipo de argumento incorrecto.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        log.warn("Tipo de argumento incorrecto: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido";
        errors.put(ex.getName(), 
                  String.format("Debe ser de tipo '%s'. Valor recibido: '%s'", expectedType, ex.getValue()));
        
        ApiResponseDto<Object> response = ApiResponseDto.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Tipo de parámetro incorrecto")
            .data(null)
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Maneja excepciones de argumentos ilegales (por ejemplo, validaciones de negocio).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        log.warn("Argumento ilegal: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        errors.put("validation", ex.getMessage());
        
        ApiResponseDto<Object> response = ApiResponseDto.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Datos de entrada inválidos")
            .data(null)
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Maneja cualquier otra excepción no contemplada específicamente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        
        ApiResponseDto<Object> response = ApiResponseDto.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("Error interno del servidor")
            .data(null)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}