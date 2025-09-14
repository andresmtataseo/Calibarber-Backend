package com.barbershop.features.barbershop.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO para request de creación y actualización de horarios de operación de barbería.
 * Incluye validaciones personalizadas y manejo de casos especiales.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para crear o actualizar horarios de operación de barbería")
public class BarbershopOperatingHoursRequestDto {

    @NotNull(message = "El ID de la barbería es obligatorio")
    @Schema(description = "ID de la barbería", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private String barbershopId;

    @NotNull(message = "El día de la semana es obligatorio")
    @Schema(description = "Día de la semana", example = "MONDAY", required = true)
    private DayOfWeek dayOfWeek;

    @Schema(description = "Hora de apertura (formato HH:mm)", example = "09:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openingTime;

    @Schema(description = "Hora de cierre (formato HH:mm)", example = "18:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closingTime;

    @Schema(description = "Indica si la barbería está cerrada este día", example = "false")
    @Builder.Default
    private Boolean isClosed = false;

    @Size(max = 255, message = "Las notas no pueden exceder 255 caracteres")
    @Schema(description = "Notas adicionales para el día", example = "Horario especial por feriado")
    private String notes;

    /**
     * Valida que los datos del horario sean consistentes.
     * Si no está cerrado, debe tener horarios de apertura y cierre.
     * La hora de cierre debe ser posterior a la de apertura.
     * 
     * @return true si los datos son válidos, false en caso contrario
     */
    public boolean isValidSchedule() {
        // Si está cerrado, no necesita validar horarios
        if (Boolean.TRUE.equals(isClosed)) {
            return true;
        }
        
        // Si no está cerrado, debe tener horarios
        if (openingTime == null || closingTime == null) {
            return false;
        }
        
        // La hora de cierre debe ser posterior a la de apertura
        return closingTime.isAfter(openingTime);
    }

    /**
     * Obtiene un mensaje de error específico si la validación falla.
     * 
     * @return mensaje de error descriptivo
     */
    public String getValidationErrorMessage() {
        if (Boolean.TRUE.equals(isClosed)) {
            return null; // No hay error si está cerrado
        }
        
        if (openingTime == null) {
            return "La hora de apertura es obligatoria cuando la barbería no está cerrada";
        }
        
        if (closingTime == null) {
            return "La hora de cierre es obligatoria cuando la barbería no está cerrada";
        }
        
        if (!closingTime.isAfter(openingTime)) {
            return "La hora de cierre debe ser posterior a la hora de apertura";
        }
        
        return null;
    }
}