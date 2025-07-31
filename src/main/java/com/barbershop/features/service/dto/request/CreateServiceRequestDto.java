package com.barbershop.features.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO para crear un nuevo servicio")
public class CreateServiceRequestDto {

    @NotBlank(message = "El ID de la barbería es obligatorio")
    @Schema(description = "ID de la barbería a la que pertenece el servicio", 
            example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String barbershopId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre del servicio", example = "Corte de cabello clásico", required = true)
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Schema(description = "Descripción detallada del servicio", 
            example = "Corte de cabello tradicional con tijeras y máquina")
    private String description;

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 5, message = "La duración mínima es de 5 minutos")
    @Max(value = 480, message = "La duración máxima es de 480 minutos (8 horas)")
    @Schema(description = "Duración del servicio en minutos", example = "30", required = true)
    private Integer durationMinutes;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @DecimalMax(value = "999999.99", message = "El precio no puede exceder 999,999.99")
    @Digits(integer = 6, fraction = 2, message = "El precio debe tener máximo 6 dígitos enteros y 2 decimales")
    @Schema(description = "Precio del servicio", example = "25000.00", required = true)
    private BigDecimal price;
}