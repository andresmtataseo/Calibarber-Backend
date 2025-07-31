package com.barbershop.features.barber.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "DTO para crear un nuevo barbero")
public class CreateBarberRequestDto {

    @NotBlank(message = "El ID del usuario es obligatorio")
    @Schema(description = "ID del usuario que será barbero", 
            example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String userId;

    @NotBlank(message = "El ID de la barbería es obligatorio")
    @Schema(description = "ID de la barbería donde trabajará el barbero", 
            example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String barbershopId;

    @Size(max = 100, message = "La especialización no puede exceder 100 caracteres")
    @Schema(description = "Especialización del barbero", 
            example = "Cortes clásicos y barba")
    private String specialization;
}