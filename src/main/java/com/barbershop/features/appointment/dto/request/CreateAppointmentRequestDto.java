package com.barbershop.features.appointment.dto.request;

import com.barbershop.features.appointment.model.enums.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "DTO para crear una cita")
public class CreateAppointmentRequestDto {

    @NotBlank(message = "El ID del barbero es obligatorio")
    @Schema(description = "ID del barbero que realizará el servicio", 
            example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String barberId;

    @NotBlank(message = "El ID del usuario es obligatorio")
    @Schema(description = "ID del usuario que solicita la cita", 
            example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String userId;

    @NotBlank(message = "El ID del servicio es obligatorio")
    @Schema(description = "ID del servicio a realizar", 
            example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String serviceId;

    @NotNull(message = "La fecha y hora de la cita es obligatoria")
    @Future(message = "La fecha de la cita debe ser en el futuro")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha y hora programada para la cita", 
            example = "2024-02-15T14:30:00", required = true)
    private LocalDateTime appointmentDateTime;

    @NotNull(message = "La duración en minutos es obligatoria")
    @Positive(message = "La duración debe ser un número positivo")
    @Schema(description = "Duración estimada de la cita en minutos", 
            example = "60", required = true)
    private Integer durationMinutes;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    @Schema(description = "Precio del servicio", 
            example = "25.50", required = true)
    private BigDecimal price;

    @Schema(description = "Estado inicial de la cita", example = "SCHEDULED")
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    @Schema(description = "Notas adicionales para la cita", 
            example = "Cliente prefiere corte clásico")
    private String notes;
}