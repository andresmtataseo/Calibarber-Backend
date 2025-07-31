package com.barbershop.features.barber.dto;

import com.barbershop.features.barber.model.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Schema(description = "DTO de respuesta para disponibilidad del barbero")
public class BarberAvailabilityResponseDto {

    @Schema(description = "ID único de la disponibilidad", example = "550e8400-e29b-41d4-a716-446655440000")
    private String barberAvailabilityId;

    @Schema(description = "ID del barbero", example = "550e8400-e29b-41d4-a716-446655440000")
    private String barberId;

    @Schema(description = "Día de la semana", example = "MONDAY")
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(description = "Hora de inicio de disponibilidad", example = "09:00:00")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(description = "Hora de fin de disponibilidad", example = "18:00:00")
    private LocalTime endTime;

    @Schema(description = "Indica si está disponible en este horario", example = "true")
    private Boolean isAvailable;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación de la disponibilidad", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de última actualización de la disponibilidad", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
}