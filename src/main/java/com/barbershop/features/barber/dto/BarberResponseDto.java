package com.barbershop.features.barber.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "DTO de respuesta para información del barbero")
public class BarberResponseDto {

    @Schema(description = "ID único del barbero", example = "550e8400-e29b-41d4-a716-446655440000")
    private String barberId;

    @Schema(description = "ID del usuario asociado", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    @Schema(description = "ID de la barbería donde trabaja", example = "550e8400-e29b-41d4-a716-446655440000")
    private String barbershopId;

    @Schema(description = "Especialización del barbero", example = "Cortes clásicos y barba")
    private String specialization;

    @Schema(description = "Estado activo del barbero", example = "true")
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación del barbero", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de última actualización del barbero", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;


}