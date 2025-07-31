package com.barbershop.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "DTO para registrar un nuevo usuario en la aplicación")
public class SignUpRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    @Schema(
        description = "Nombre del usuario", 
        example = "Juan", 
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 2,
        maxLength = 50
    )
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido solo puede contener letras y espacios")
    @Schema(
        description = "Apellido del usuario", 
        example = "Pérez", 
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 2,
        maxLength = 50
    )
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    @Schema(
        description = "Dirección de correo electrónico del usuario, utilizada también como nombre de usuario", 
        example = "juan.perez@ejemplo.com", 
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255
    )
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$",
        message = "La contraseña debe contener al menos una letra minúscula, una mayúscula, un número y un carácter especial"
    )
    @Schema(
        description = "Contraseña del usuario. Debe contener al menos una letra minúscula, una mayúscula, un número y un carácter especial", 
        example = "MiPassword123!", 
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 8,
        maxLength = 100
    )
    private String password;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "El formato del teléfono no es válido")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Schema(
        description = "Número de teléfono del usuario (opcional)", 
        example = "+573001234567",
        maxLength = 20
    )
    private String phoneNumber;

    @Size(max = 500, message = "La URL de la foto de perfil no puede exceder 500 caracteres")
    @Pattern(
        regexp = "^https?://.*", 
        message = "La URL debe comenzar con http:// o https://"
    )
    @Schema(
        description = "URL de la foto de perfil del usuario (opcional)", 
        example = "https://ejemplo.com/foto.jpg",
        maxLength = 500
    )
    private String profilePictureUrl;
}