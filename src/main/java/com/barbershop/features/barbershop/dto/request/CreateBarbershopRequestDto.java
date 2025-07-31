package com.barbershop.features.barbershop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "DTO para crear una nueva barbería")
public class CreateBarbershopRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre de la barbería", example = "Barbería El Corte Perfecto", required = true)
    private String name;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    @Schema(description = "Dirección completa de la barbería", example = "Calle 123 #45-67, Bogotá", required = true)
    private String addressText;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "El formato del teléfono no es válido")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Schema(description = "Número de teléfono de la barbería", example = "+573001234567")
    private String phoneNumber;

    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Schema(description = "Email de contacto de la barbería", example = "contacto@barberia.com")
    private String email;

    @Schema(description = "Horarios de operación en formato JSON", 
            example = "{\"lunes\": \"9:00-18:00\", \"martes\": \"9:00-18:00\"}")
    private String operatingHours;

    @Schema(description = "URL del logo de la barbería", example = "https://ejemplo.com/logo.jpg")
    private String logoUrl;
}