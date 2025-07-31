package com.barbershop.features.payment.dto;

import com.barbershop.features.appointment.dto.AppointmentResponseDto;
import com.barbershop.features.payment.model.enums.PaymentMethod;
import com.barbershop.features.payment.model.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "DTO de respuesta para información del pago")
public class PaymentResponseDto {

    @Schema(description = "ID único del pago", example = "550e8400-e29b-41d4-a716-446655440000")
    private String paymentId;

    @Schema(description = "ID de la cita asociada", example = "550e8400-e29b-41d4-a716-446655440000")
    private String appointmentId;

    @Schema(description = "Monto del pago", example = "25.50")
    private BigDecimal amount;

    @Schema(description = "Método de pago utilizado", example = "CREDIT_CARD")
    private PaymentMethod paymentMethod;

    @Schema(description = "Estado actual del pago", example = "COMPLETED")
    private PaymentStatus status;

    @Schema(description = "Referencia de la transacción externa", example = "TXN123456789")
    private String transactionReference;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación del pago", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de última actualización del pago", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Información de la cita asociada")
    private AppointmentResponseDto appointment;
}