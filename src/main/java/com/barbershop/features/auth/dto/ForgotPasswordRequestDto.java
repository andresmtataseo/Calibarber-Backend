package com.barbershop.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para solicitud de restablecimiento de contraseña")
public class ForgotPasswordRequestDto {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    @Schema(
        description = "Email del usuario que solicita el restablecimiento de contraseña", 
        example = "usuario@ejemplo.com",
        maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;
}