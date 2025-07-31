package com.barbershop.features.payment.dto.request;

import com.barbershop.features.payment.model.enums.PaymentMethod;
import com.barbershop.features.payment.model.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO para crear un pago")
public class CreatePaymentRequestDto {

    @NotBlank(message = "El ID de la cita es obligatorio")
    @Schema(description = "ID de la cita asociada al pago", 
            example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String appointmentId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "El monto debe tener máximo 10 dígitos enteros y 2 decimales")
    @Schema(description = "Monto del pago", 
            example = "25.50", required = true)
    private BigDecimal amount;

    @NotNull(message = "El método de pago es obligatorio")
    @Schema(description = "Método de pago utilizado", example = "CREDIT_CARD", required = true)
    private PaymentMethod paymentMethod;

    @Schema(description = "Estado inicial del pago", example = "PENDING")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Size(max = 100, message = "La referencia de transacción no puede exceder 100 caracteres")
    @Schema(description = "Referencia de la transacción externa", 
            example = "TXN123456789")
    private String transactionReference;
}