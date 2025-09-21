package com.barbershop.features.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta que contiene la disponibilidad de la barbería por días en un rango de fechas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con la disponibilidad de la barbería por días")
public class AvailabilityResponseDto {

    @Schema(
            description = "Lista de disponibilidad por días",
            example = "[{\"date\": \"2025-09-20\", \"status\": \"LIBRE\"}, {\"date\": \"2025-09-21\", \"status\": \"PARCIALMENTE_DISPONIBLE\"}]"
    )
    private List<DayAvailabilityDto> availability;
}