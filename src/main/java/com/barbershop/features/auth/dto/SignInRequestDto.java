package com.barbershop.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "DTO para la autenticación de usuarios (inicio de sesión)")
public class SignInRequestDto {

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El formato del email es inválido")
    @Schema(description = "Dirección de correo electrónico del usuario", example = "usuario@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Schema(description = "Contraseña del usuario", example = "MiContraseñaSegura123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

}