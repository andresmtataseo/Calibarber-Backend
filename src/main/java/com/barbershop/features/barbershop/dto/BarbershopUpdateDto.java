package com.barbershop.features.barbershop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO para actualizar una barbería existente")
public class BarbershopUpdateDto {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Nombre de la barbería", example = "Barbería El Corte Perfecto")
    private String name;

    @Schema(description = "Dirección completa de la barbería", example = "Calle 123 #45-67, Bogotá")
    private String addressText;

    @Size(max = 20, message = "El número de teléfono no puede exceder 20 caracteres")
    @Schema(description = "Número de teléfono de la barbería", example = "+573001234567")
    private String phoneNumber;

    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Schema(description = "Email de contacto de la barbería", example = "contacto@barberia.com")
    private String email;

    @Schema(description = "URL del logo de la barbería", example = "https://ejemplo.com/logo.jpg")
    private String logoUrl;
}