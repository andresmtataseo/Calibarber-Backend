package com.barbershop.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO genérico para estandarizar todas las respuestas de la API.
 * Proporciona una estructura consistente para respuestas exitosas y de error.
 *
 * @param <T> Tipo de datos que se incluirán en la respuesta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Esquema genérico para las respuestas de la API, incluyendo mensajes de éxito o error.")
public class ApiResponseDto<T> {

    @Schema(description = "Indica si la operación fue exitosa", example = "true")
    private boolean success;

    @Schema(description = "Código de estado HTTP de la respuesta", example = "200")
    private int status;

    @Schema(description = "Mensaje descriptivo de la respuesta", example = "Operación exitosa")
    private String message;

    @Schema(description = "Datos de la respuesta (puede ser cualquier tipo de objeto)")
    private T data;

    @Schema(description = "Marca de tiempo de cuando ocurrió la respuesta", example = "2024-01-15T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Ruta de la petición que generó la respuesta", example = "/api/auth/sign-in")
    private String path;

    @Schema(description = "Lista de errores específicos, como errores de validación de campos")
    private List<String> errors;

    @Schema(description = "Mapa de errores de validación por campo")
    private Map<String, String> fieldErrors;

    // Métodos estáticos para crear respuestas exitosas

    /**
     * Crea una respuesta exitosa sin datos adicionales.
     */
    public static <T> ApiResponseDto<T> success(String message) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crea una respuesta exitosa con datos.
     */
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crea una respuesta exitosa con estado HTTP personalizado.
     */
    public static <T> ApiResponseDto<T> success(HttpStatus status, String message, T data) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .status(status.value())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Métodos estáticos para crear respuestas de error

    /**
     * Crea una respuesta de error simple.
     */
    public static <T> ApiResponseDto<T> error(HttpStatus status, String message) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .status(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crea una respuesta de error con ruta.
     */
    public static <T> ApiResponseDto<T> error(HttpStatus status, String message, String path) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .status(status.value())
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crea una respuesta de error con lista de errores.
     */
    public static <T> ApiResponseDto<T> error(HttpStatus status, String message, List<String> errors) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .status(status.value())
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crea una respuesta de error con errores de validación por campo.
     */
    public static <T> ApiResponseDto<T> error(HttpStatus status, String message, Map<String, String> fieldErrors) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .status(status.value())
                .message(message)
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crea una respuesta de error completa.
     */
    public static <T> ApiResponseDto<T> error(HttpStatus status, String message, String path, List<String> errors) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .status(status.value())
                .message(message)
                .path(path)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Constructores de conveniencia (mantenidos para compatibilidad)

    /**
     * Constructor conveniente para crear respuestas.
     */
    public ApiResponseDto(HttpStatus status, String message, String path) {
        this.success = status.is2xxSuccessful();
        this.status = status.value();
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor conveniente para errores de validación con lista de errores.
     */
    public ApiResponseDto(HttpStatus status, String message, String path, List<String> errors) {
        this.success = false;
        this.status = status.value();
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }
}