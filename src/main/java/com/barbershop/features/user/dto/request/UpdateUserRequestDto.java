package com.barbershop.features.user.dto.request;

import com.barbershop.features.user.model.enums.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "DTO para actualizar un usuario existente")
public class UpdateUserRequestDto {

    @Email(message = "El formato del email no es válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    @Schema(description = "Email del usuario", example = "usuario@ejemplo.com")
    private String email;

    @Schema(description = "Rol del usuario", example = "ROLE_CLIENT")
    private RoleEnum role;

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String firstName;

    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido solo puede contener letras y espacios")
    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "El formato del teléfono no es válido")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Schema(description = "Número de teléfono del usuario", example = "+573001234567")
    private String phoneNumber;

    @Schema(description = "Estado activo del usuario", example = "true")
    private Boolean isActive;

    @Schema(description = "URL de la foto de perfil del usuario", example = "https://ejemplo.com/foto.jpg")
    private String profilePictureUrl;
}