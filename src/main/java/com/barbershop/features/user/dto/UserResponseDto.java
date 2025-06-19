package com.barbershop.features.user.dto;

import com.barbershop.features.user.model.enums.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para los datos de un usuario")
public class UserResponseDto {

    @Schema(description = "ID único del usuario", example = "1")
    private Integer id;

    @Schema(description = "Nombre del usuario", example = "Juan")
    private String name;

    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String lastName;

    @Schema(description = "Email del usuario (nombre de usuario)", example = "juan.perez@example.com")
    private String email;

    @Schema(description = "Rol principal del usuario", example = "ROLE_CLIENT")
    private RoleEnum role;
}