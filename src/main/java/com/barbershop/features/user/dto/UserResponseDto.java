package com.barbershop.features.user.dto;

import com.barbershop.features.user.model.enums.RoleEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "DTO de respuesta para información del usuario")
public class UserResponseDto {

    @Schema(description = "ID único del usuario", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    @Schema(description = "Email del usuario", example = "usuario@ejemplo.com")
    private String email;

    @Schema(description = "Rol del usuario", example = "CLIENT")
    private RoleEnum role;

    @Schema(description = "Nombre del usuario", example = "Juan")
    private String firstName;

    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String lastName;

    @Schema(description = "Número de teléfono del usuario", example = "+573001234567")
    private String phoneNumber;

    @Schema(description = "Estado activo del usuario", example = "true")
    private Boolean isActive;

    @Schema(description = "URL de la foto de perfil del usuario", example = "https://ejemplo.com/foto.jpg")
    private String profilePictureUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación del usuario", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de última actualización del usuario", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
}