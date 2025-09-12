package com.barbershop.features.barbershop.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO para representar los horarios de operación de una barbería.
 * Contiene información sobre el día de la semana, horarios de apertura y cierre,
 * y estado de disponibilidad.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Horarios de operación de una barbería por día de la semana")
public class BarbershopOperatingHoursDto {

    @Schema(description = "ID único del horario de operación", example = "123e4567-e89b-12d3-a456-426614174000")
    private String operatingHoursId;

    @Schema(description = "Día de la semana", example = "MONDAY")
    private DayOfWeek dayOfWeek;

    @Schema(description = "Nombre del día en español", example = "Lunes")
    private String dayName;

    @Schema(description = "Hora de apertura", example = "09:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openingTime;

    @Schema(description = "Hora de cierre", example = "18:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closingTime;

    @Schema(description = "Indica si la barbería está cerrada este día", example = "false")
    private Boolean isClosed;

    @Schema(description = "Notas adicionales para el día", example = "Horario especial por feriado")
    private String notes;

    @Schema(description = "Horario formateado para mostrar", example = "09:00 - 18:00")
    private String formattedHours;

    @Schema(description = "Indica si está abierto (tiene horarios definidos y no está cerrado)", example = "true")
    private Boolean isOpen;

    /**
     * Obtiene el nombre del día en español
     * @param dayOfWeek Día de la semana
     * @return Nombre en español
     */
    public static String getDayNameInSpanish(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }

    /**
     * Establece el nombre del día basado en dayOfWeek
     */
    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        this.dayName = getDayNameInSpanish(dayOfWeek);
    }
}