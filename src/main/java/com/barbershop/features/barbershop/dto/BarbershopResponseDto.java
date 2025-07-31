package com.barbershop.features.barbershop.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "DTO de respuesta para información de la barbería")
public class BarbershopResponseDto {

    @Schema(description = "ID único de la barbería", example = "550e8400-e29b-41d4-a716-446655440000")
    private String barbershopId;

    @Schema(description = "Nombre de la barbería", example = "Barbería El Corte Perfecto")
    private String name;

    @Schema(description = "Dirección completa de la barbería", example = "Calle 123 #45-67, Bogotá")
    private String addressText;

    @Schema(description = "Número de teléfono de la barbería", example = "+573001234567")
    private String phoneNumber;

    @Schema(description = "Email de contacto de la barbería", example = "contacto@barberia.com")
    private String email;

    @Schema(description = "Horarios de operación en formato JSON", 
            example = "{\"lunes\": \"9:00-18:00\", \"martes\": \"9:00-18:00\"}")
    private String operatingHours;

    @Schema(description = "URL del logo de la barbería", example = "https://ejemplo.com/logo.jpg")
    private String logoUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación de la barbería", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de última actualización de la barbería", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
}