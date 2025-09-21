package com.barbershop.features.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO que representa un bloque de tiempo de 30 minutos con su disponibilidad
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bloque de tiempo de 30 minutos con su disponibilidad")
public class DayAvailabilitySlotDto {

    @Schema(
            description = "Hora de inicio del bloque",
            example = "09:00",
            type = "string",
            format = "time"
    )
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;

    @Schema(
            description = "Indica si el bloque está disponible (al menos un barbero libre)",
            example = "true"
    )
    private boolean available;
}