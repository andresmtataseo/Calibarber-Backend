package com.barbershop.features.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO para actualizar un servicio existente")
public class UpdateServiceRequestDto {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre del servicio", example = "Corte de cabello clásico")
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Schema(description = "Descripción detallada del servicio", 
            example = "Corte de cabello tradicional con tijeras y máquina")
    private String description;

    @Min(value = 5, message = "La duración mínima es de 5 minutos")
    @Max(value = 480, message = "La duración máxima es de 480 minutos (8 horas)")
    @Schema(description = "Duración del servicio en minutos", example = "30")
    private Integer durationMinutes;

    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @DecimalMax(value = "999999.99", message = "El precio no puede exceder 999,999.99")
    @Digits(integer = 6, fraction = 2, message = "El precio debe tener máximo 6 dígitos enteros y 2 decimales")
    @Schema(description = "Precio del servicio", example = "25000.00")
    private BigDecimal price;

    @Schema(description = "Estado activo del servicio", example = "true")
    private Boolean isActive;
}