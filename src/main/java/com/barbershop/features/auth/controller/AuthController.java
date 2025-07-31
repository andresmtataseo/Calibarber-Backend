package com.barbershop.features.auth.controller;

import com.barbershop.features.auth.dto.AuthResponseDto;
import com.barbershop.features.auth.dto.ChangePasswordRequestDto;
import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.features.auth.dto.SignInRequestDto;
import com.barbershop.features.auth.dto.SignUpRequestDto;
import com.barbershop.features.auth.service.AuthService;
import com.barbershop.common.util.ApiConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiConstants.AUTH_API_BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Operaciones relacionadas con el registro, inicio de sesión y gestión de contraseñas de usuarios")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Inicia sesión de un usuario",
            description = "Autentica a un usuario con su email y contraseña y devuelve un token JWT con información esencial de autenticación.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Inicio de sesión exitoso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Credenciales inválidas",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos de entrada inválidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @PostMapping(ApiConstants.SIGN_IN_URL)
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> signIn(@Valid @RequestBody SignInRequestDto signInRequestDto){
        AuthResponseDto authResponse = authService.signIn(signInRequestDto);
        return ResponseEntity.ok(ApiResponseDto.success(
                "Inicio de sesión exitoso",
                authResponse
        ));
    }

    @Operation(
            summary = "Registra un nuevo usuario",
            description = "Crea una nueva cuenta de usuario con el rol por defecto (CLIENTE) y devuelve un token JWT con información esencial de autenticación.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Usuario registrado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "El email ya está registrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos de entrada inválidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @PostMapping(ApiConstants.SIGN_UP_URL)
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto){
        AuthResponseDto authResponse = authService.signUp(signUpRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(
                HttpStatus.CREATED,
                "Usuario registrado exitosamente",
                authResponse
        ));
    }



    @Operation(
            summary = "Cambia la contraseña del usuario autenticado",
            description = "Permite al usuario autenticado cambiar su contraseña proporcionando la contraseña actual y la nueva contraseña.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Contraseña cambiada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos de entrada inválidos o contraseñas no coinciden",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Token inválido, expirado o contraseña actual incorrecta",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @PutMapping(ApiConstants.CHANGE_PASSWORD_URL)
    public ResponseEntity<ApiResponseDto<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        authService.changePassword(changePasswordRequestDto, userEmail);
        
        return ResponseEntity.ok(ApiResponseDto.success(
                "Contraseña cambiada exitosamente",
                "La contraseña ha sido actualizada correctamente"
        ));
    }

    @Operation(
            summary = "Verifica si un email está disponible",
            description = "Verifica si un email específico ya está registrado en el sistema.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Verificación completada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Email inválido",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @GetMapping(ApiConstants.CHECK_EMAIL_URL)
    public ResponseEntity<ApiResponseDto<Boolean>> checkEmailAvailability(@RequestParam String email) {
        boolean exists = authService.emailExists(email);
        
        String message = exists ? "El email ya está registrado" : "El email está disponible";
        Boolean isAvailable = !exists; // true si está disponible, false si ya existe
        
        return ResponseEntity.ok(ApiResponseDto.success(message, isAvailable));
    }
}