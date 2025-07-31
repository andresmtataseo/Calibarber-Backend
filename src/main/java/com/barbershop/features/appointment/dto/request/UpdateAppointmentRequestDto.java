package com.barbershop.features.appointment.dto.request;

import com.barbershop.features.appointment.model.enums.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "DTO para actualizar una cita")
public class UpdateAppointmentRequestDto {

    @Future(message = "La fecha de la cita debe ser en el futuro")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Nueva fecha y hora programada para la cita", 
            example = "2024-02-15T14:30:00")
    private LocalDateTime appointmentDateTime;

    @Positive(message = "La duración debe ser un número positivo")
    @Schema(description = "Nueva duración estimada de la cita en minutos", 
            example = "60")
    private Integer durationMinutes;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    @Schema(description = "Nuevo precio del servicio", 
            example = "25.50")
    private BigDecimal price;

    @Schema(description = "Nuevo estado de la cita", example = "CONFIRMED")
    private AppointmentStatus status;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    @Schema(description = "Nuevas notas adicionales para la cita", 
            example = "Cliente prefiere corte clásico")
    private String notes;
}