package com.barbershop.features.barber.dto.request;

import com.barbershop.features.barber.model.DayOfWeek;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalTime;

@Data
@Schema(description = "DTO para crear disponibilidad de barbero")
public class CreateBarberAvailabilityRequestDto {

    @NotBlank(message = "El ID del barbero es obligatorio")
    @Schema(description = "ID del barbero", 
            example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String barberId;

    @NotNull(message = "El día de la semana es obligatorio")
    @Schema(description = "Día de la semana", example = "MONDAY", required = true)
    private DayOfWeek dayOfWeek;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Schema(description = "Hora de inicio de disponibilidad", example = "09:00:00", required = true)
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    @Schema(description = "Hora de fin de disponibilidad", example = "18:00:00", required = true)
    private LocalTime endTime;

    @Schema(description = "Indica si está disponible en este horario", example = "true")
    private Boolean isAvailable = true;
}