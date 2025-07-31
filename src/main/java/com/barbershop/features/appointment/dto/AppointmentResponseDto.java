package com.barbershop.features.appointment.dto;

import com.barbershop.features.appointment.model.enums.AppointmentStatus;
import com.barbershop.features.barber.dto.BarberResponseDto;
import com.barbershop.features.service.dto.ServiceResponseDto;
import com.barbershop.features.user.dto.UserResponseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "DTO de respuesta para información de la cita")
public class AppointmentResponseDto {

    @Schema(description = "ID único de la cita", example = "550e8400-e29b-41d4-a716-446655440000")
    private String appointmentId;

    @Schema(description = "ID del barbero", example = "550e8400-e29b-41d4-a716-446655440000")
    private String barberId;

    @Schema(description = "ID del usuario", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    @Schema(description = "ID del servicio", example = "550e8400-e29b-41d4-a716-446655440000")
    private String serviceId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha y hora programada para la cita", example = "2024-02-15T14:30:00")
    private LocalDateTime appointmentDateTime;

    @Schema(description = "Duración estimada de la cita en minutos", example = "60")
    private Integer durationMinutes;

    @Schema(description = "Precio del servicio", example = "25.50")
    private BigDecimal price;

    @Schema(description = "Estado actual de la cita", example = "SCHEDULED")
    private AppointmentStatus status;

    @Schema(description = "Notas adicionales para la cita", example = "Cliente prefiere corte clásico")
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación de la cita", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de última actualización de la cita", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Información del barbero asignado")
    private BarberResponseDto barber;

    @Schema(description = "Información del usuario que solicita la cita")
    private UserResponseDto user;

    @Schema(description = "Información del servicio solicitado")
    private ServiceResponseDto service;
}