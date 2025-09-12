package com.barbershop.features.barbershop.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO para crear o actualizar horarios de operación de una barbería.
 * Valida que los datos sean consistentes y completos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear o actualizar horarios de operación")
public class BarbershopOperatingHoursCreateDto {

    @NotNull(message = "El día de la semana es obligatorio")
    @Schema(description = "Día de la semana", example = "MONDAY", required = true)
    private DayOfWeek dayOfWeek;

    @Schema(description = "Hora de apertura (requerida si no está cerrado)", example = "09:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openingTime;

    @Schema(description = "Hora de cierre (requerida si no está cerrado)", example = "18:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closingTime;

    @Schema(description = "Indica si la barbería está cerrada este día", example = "false")
    @Builder.Default
    private Boolean isClosed = false;

    @Schema(description = "Notas adicionales para el día", example = "Horario especial por feriado")
    private String notes;

    /**
     * Valida que los horarios sean consistentes
     * @return true si los datos son válidos
     */
    public boolean isValid() {
        // Si está cerrado, no necesita horarios
        if (Boolean.TRUE.equals(isClosed)) {
            return true;
        }
        
        // Si no está cerrado, debe tener horarios de apertura y cierre
        if (openingTime == null || closingTime == null) {
            return false;
        }
        
        // La hora de apertura debe ser antes que la de cierre
        return openingTime.isBefore(closingTime);
    }

    /**
     * Obtiene un mensaje de error si los datos no son válidos
     * @return Mensaje de error o null si es válido
     */
    public String getValidationError() {
        if (Boolean.TRUE.equals(isClosed)) {
            return null;
        }
        
        if (openingTime == null) {
            return "La hora de apertura es obligatoria cuando no está cerrado";
        }
        
        if (closingTime == null) {
            return "La hora de cierre es obligatoria cuando no está cerrado";
        }
        
        if (!openingTime.isBefore(closingTime)) {
            return "La hora de apertura debe ser anterior a la hora de cierre";
        }
        
        return null;
    }
}