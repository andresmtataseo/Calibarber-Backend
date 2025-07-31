package com.barbershop.features.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "DTO de respuesta para información del servicio")
public class ServiceResponseDto {

    @Schema(description = "ID único del servicio", example = "550e8400-e29b-41d4-a716-446655440000")
    private String serviceId;

    @Schema(description = "ID de la barbería a la que pertenece el servicio", 
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String barbershopId;

    @Schema(description = "Nombre del servicio", example = "Corte de cabello clásico")
    private String name;

    @Schema(description = "Descripción detallada del servicio", 
            example = "Corte de cabello tradicional con tijeras y máquina")
    private String description;

    @Schema(description = "Duración del servicio en minutos", example = "30")
    private Integer durationMinutes;

    @Schema(description = "Precio del servicio", example = "25000.00")
    private BigDecimal price;

    @Schema(description = "Estado activo del servicio", example = "true")
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación del servicio", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de última actualización del servicio", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
}