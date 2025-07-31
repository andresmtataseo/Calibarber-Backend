package com.barbershop.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO para la autenticación de usuarios (inicio de sesión)")
public class SignInRequestDto {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    @Schema(
        description = "Dirección de correo electrónico del usuario registrado", 
        example = "juan.perez@ejemplo.com", 
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255
    )
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    @Schema(
        description = "Contraseña del usuario", 
        example = "MiContraseñaSegura123!", 
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 6,
        maxLength = 100
    )
    private String password;
}