package com.barbershop.features.auth.dto;

import com.barbershop.features.user.model.enums.RoleEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para autenticación que contiene solo información esencial")
public class AuthResponseDto {

    @Schema(
        description = "Token de acceso JWT para autenticación en endpoints protegidos", 
        example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c3VhcmlvQGV4YW1wbGUuY29tIiwiaWF0IjoxNjM1NjgyNjQ3LCJleHAiOjE2MzU2ODYyNDd9.somehashstring",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String token;

    @Schema(
        description = "Tipo de token (siempre 'Bearer' para JWT)", 
        example = "Bearer",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
        description = "Fecha y hora de expiración del token", 
        example = "2024-12-31T23:59:59",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime expiresAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
        description = "Fecha y hora de emisión del token", 
        example = "2024-01-15T10:30:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime issuedAt;

    @Schema(
        description = "ID único del usuario autenticado", 
        example = "550e8400-e29b-41d4-a716-446655440000",
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
}