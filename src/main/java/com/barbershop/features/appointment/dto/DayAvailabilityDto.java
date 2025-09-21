package com.barbershop.features.appointment.dto;

import com.barbershop.features.appointment.model.enums.AvailabilityStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO que representa la disponibilidad de la barbería para un día específico
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Disponibilidad de la barbería para un día específico")
public class DayAvailabilityDto {

    @Schema(
            description = "Fecha del día",
            example = "2025-09-20",
            type = "string",
            format = "date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Schema(
            description = "Estado de disponibilidad del día",
            example = "LIBRE",
            allowableValues = {"LIBRE", "PARCIALMENTE_DISPONIBLE", "SIN_DISPONIBILIDAD"}
    )
    private AvailabilityStatus status;
}