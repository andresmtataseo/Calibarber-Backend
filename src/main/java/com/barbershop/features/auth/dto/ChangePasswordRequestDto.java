package com.barbershop.features.auth.dto;

import com.barbershop.features.auth.validation.PasswordMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatch(
    password = "newPassword", 
    confirmPassword = "confirmNewPassword", 
    message = "La nueva contraseña y su confirmación deben coincidir"
)
@Schema(description = "DTO para solicitud de cambio de contraseña del usuario autenticado")
public class ChangePasswordRequestDto {

    @NotBlank(message = "La contraseña actual es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña actual debe tener entre 8 y 100 caracteres")
    @Schema(
        description = "Contraseña actual del usuario para verificación", 
        example = "currentPassword123!",
        minLength = 8,
        maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "La nueva contraseña debe contener al menos: 1 letra minúscula, 1 mayúscula, 1 número y 1 carácter especial (@$!%*?&)"
    )
    @Schema(
        description = "Nueva contraseña que debe cumplir con los criterios de seguridad", 
        example = "NewPassword123!",
        minLength = 8,
        maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String newPassword;

    @NotBlank(message = "La confirmación de la nueva contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La confirmación debe tener entre 8 y 100 caracteres")
    @Schema(
        description = "Confirmación de la nueva contraseña (debe coincidir con newPassword)", 
        example = "NewPassword123!",
        minLength = 8,
        maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String confirmNewPassword;
}