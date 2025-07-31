package com.barbershop.features.auth.dto;

import com.barbershop.features.user.model.enums.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para verificación de autenticación.
 * Contiene información básica del usuario autenticado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para verificación de autenticación")
public class CheckAuthResponseDto {

    @Schema(
        description = "ID único del usuario", 
        example = "123e4567-e89b-12d3-a456-426614174000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String userId;

    @Schema(
        description = "Email del usuario autenticado", 
        example = "usuario@ejemplo.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Schema(
        description = "Rol del usuario autenticado", 
        example = "CLIENT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private RoleEnum role;

    @Schema(
        description = "Nombre completo del usuario", 
        example = "Juan Pérez",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String fullName;

    @Schema(
        description = "Indica si el usuario está activo", 
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isActive;

    @Schema(
        description = "Indica si el token es válido", 
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isTokenValid;
}