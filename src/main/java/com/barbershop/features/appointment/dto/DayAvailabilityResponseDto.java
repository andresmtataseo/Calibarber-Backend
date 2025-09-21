package com.barbershop.features.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta que contiene la disponibilidad detallada de un día en bloques de 30 minutos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Disponibilidad detallada de un día en bloques de 30 minutos")
public class DayAvailabilityResponseDto {

    @Schema(
            description = "Fecha del día consultado",
            example = "2025-09-21",
            type = "string",
            format = "date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Schema(
            description = "Lista de bloques de tiempo de 30 minutos con su disponibilidad",
            example = "[{\"time\": \"09:00\", \"available\": true}, {\"time\": \"09:30\", \"available\": false}]"
    )
    private List<DayAvailabilitySlotDto> slots;
}