package com.barbershop.features.barber.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "DTO para actualizar un barbero existente")
public class UpdateBarberRequestDto {

    @Size(max = 100, message = "La especialización no puede exceder 100 caracteres")
    @Schema(description = "Especialización del barbero", 
            example = "Cortes clásicos y barba")
    private String specialization;

    @Schema(description = "Estado activo del barbero", example = "true")
    private Boolean isActive;
}