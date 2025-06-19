package com.barbershop.features.auth.dto;

import com.barbershop.features.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para la autenticación, incluyendo el token JWT y los datos del usuario")
public class AuthResponseDto {

    @Schema(description = "Token de acceso JWT", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c3VhcmlvQGV4YW1wbGUuY29tIiwiaWF0IjoxNjM1NjgyNjQ3LCJleHAiOjE2MzU2ODYyNDd9.somehashstring")
    private String token;

    @Schema(description = "Tipo de token (usualmente 'Bearer')", example = "Bearer")
    private String type;

    @Schema(description = "Datos del usuario autenticado")
    private UserResponseDto user;
}
