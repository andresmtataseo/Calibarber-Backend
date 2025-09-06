package com.barbershop.features.auth.controller;

import com.barbershop.features.auth.dto.AuthResponseDto;
import com.barbershop.features.auth.dto.ChangePasswordRequestDto;
import com.barbershop.features.auth.dto.CheckAuthResponseDto;
import com.barbershop.features.auth.dto.ForgotPasswordRequestDto;
import com.barbershop.features.auth.dto.ResetPasswordRequestDto;
import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.features.auth.dto.SignInRequestDto;
import com.barbershop.features.auth.dto.SignUpRequestDto;
import com.barbershop.features.auth.service.AuthService;
import com.barbershop.features.auth.service.TokenCleanupService;
import com.barbershop.common.util.ApiConstants;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping(ApiConstants.AUTH_API_BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Operaciones relacionadas con el registro, inicio de sesión y gestión de contraseñas de usuarios")
public class AuthController {

    private final AuthService authService;
    private final TokenCleanupService tokenCleanupService;

    @Operation(
            summary = "Inicia sesión de un usuario",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede iniciar sesión con credenciales de administrador<br/>" +
                         "• <strong>BARBER:</strong> Puede iniciar sesión con credenciales de barbero<br/>" +
                         "• <strong>CLIENT:</strong> Puede iniciar sesión con credenciales de cliente",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Inicio de sesión exitoso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @PostMapping(ApiConstants.SIGN_IN_URL)
    @SecurityRequirements()
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> signIn(@Valid @RequestBody SignInRequestDto signInRequestDto, HttpServletRequest request) {
        AuthResponseDto authResponse = authService.signIn(signInRequestDto);
        return ResponseEntity.ok(
                ApiResponseDto.<AuthResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("Inicio de sesión exitoso")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(authResponse)
                        .build()
        );
    }

    @Operation(
            summary = "Registra un nuevo usuario",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede registrar nuevos usuarios (acceso público)<br/>" +
                         "• <strong>BARBER:</strong> Puede registrar nuevos usuarios (acceso público)<br/>" +
                         "• <strong>CLIENT:</strong> Puede registrar nuevos usuarios (acceso público)",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Usuario registrado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @PostMapping(ApiConstants.SIGN_UP_URL)
    @SecurityRequirements()
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto, HttpServletRequest request) {
        AuthResponseDto authResponse = authService.signUp(signUpRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.<AuthResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Usuario registrado exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(authResponse)
                        .build()
        );
    }

    @Operation(
            summary = "Cambia la contraseña del usuario autenticado",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Solo puede cambiar su propia contraseña<br/>" +
                         "• <strong>BARBER:</strong> Solo puede cambiar su propia contraseña<br/>" +
                         "• <strong>CLIENT:</strong> Solo puede cambiar su propia contraseña",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Contraseña cambiada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @PutMapping(ApiConstants.CHANGE_PASSWORD_URL)
    public ResponseEntity<ApiResponseDto<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto,
            Authentication authentication,
            HttpServletRequest request) {
        String userEmail = authentication.getName();
        authService.changePassword(changePasswordRequestDto, userEmail);
        return ResponseEntity.ok(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.OK.value())
                        .message("Contraseña cambiada exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @Operation(
            summary = "Verifica si un email está disponible",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede verificar disponibilidad de cualquier email (acceso público)<br/>" +
                         "• <strong>BARBER:</strong> Puede verificar disponibilidad de cualquier email (acceso público)<br/>" +
                         "• <strong>CLIENT:</strong> Puede verificar disponibilidad de cualquier email (acceso público)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Verificación completada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @GetMapping(ApiConstants.CHECK_EMAIL_URL)
    @SecurityRequirements()
    public ResponseEntity<ApiResponseDto<String>> checkEmailAvailability(@RequestParam @Valid @Email String email, HttpServletRequest request) {

        String normalizedEmail = email.toLowerCase().trim();
        boolean exists = authService.emailExists(normalizedEmail);

        String message = exists ? "El email no está disponible" : "El email está disponible";

        return ResponseEntity.ok(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @Operation(
            summary = "Solicita recuperación de contraseña",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede solicitar recuperación de contraseña (acceso público)<br/>" +
                         "• <strong>BARBER:</strong> Puede solicitar recuperación de contraseña (acceso público)<br/>" +
                         "• <strong>CLIENT:</strong> Puede solicitar recuperación de contraseña (acceso público)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Email de recuperación enviado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @PostMapping(ApiConstants.FORGOT_PASSWORD_URL)
    @SecurityRequirements()
    public ResponseEntity<ApiResponseDto<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request, HttpServletRequest request2) {
        authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.OK.value())
                        .message("Solicitud procesada exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request2.getRequestURI())
                        .build()
        );
    }

    @Operation(
            summary = "Restablece la contraseña del usuario",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede restablecer contraseña con token válido (acceso público)<br/>" +
                         "• <strong>BARBER:</strong> Puede restablecer contraseña con token válido (acceso público)<br/>" +
                         "• <strong>CLIENT:</strong> Puede restablecer contraseña con token válido (acceso público)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Contraseña restablecida exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @PostMapping(ApiConstants.RESET_PASSWORD_URL)
    @SecurityRequirements()
    public ResponseEntity<ApiResponseDto<String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto resetPasswordRequestDto, HttpServletRequest request) {
        authService.resetPassword(resetPasswordRequestDto);

        return ResponseEntity.ok(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.OK.value())
                        .message("Contraseña restablecida exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @Operation(
            summary = "Verifica el estado de autenticación",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede verificar su propio estado de autenticación<br/>" +
                         "• <strong>BARBER:</strong> Puede verificar su propio estado de autenticación<br/>" +
                         "• <strong>CLIENT:</strong> Puede verificar su propio estado de autenticación",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Token válido - Usuario autenticado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @GetMapping("/check-auth")
    public ResponseEntity<ApiResponseDto<CheckAuthResponseDto>> checkAuth(
            Authentication authentication,
            HttpServletRequest request) {
        
        try {
            String userEmail = authentication.getName();
            CheckAuthResponseDto authInfo = authService.checkAuthByEmail(userEmail);

            return ResponseEntity.ok(
                    ApiResponseDto.<CheckAuthResponseDto>builder()
                            .status(HttpStatus.OK.value())
                            .message("Token válido")
                            .data(authInfo)
                            .timestamp(LocalDateTime.now())
                            .path(request.getRequestURI())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponseDto.<CheckAuthResponseDto>builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("Token inválido o expirado")
                            .timestamp(LocalDateTime.now())
                            .path(request.getRequestURI())
                            .build()
            );
        }
    }

    @Operation(
            summary = "Limpia tokens de restablecimiento expirados",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Acceso completo a funcionalidades administrativas de limpieza<br/>" +
                         "• <strong>BARBER:</strong> Acceso denegado - funcionalidad exclusiva de administradores<br/>" +
                         "• <strong>CLIENT:</strong> Acceso denegado - funcionalidad exclusiva de administradores",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Limpieza completada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @PostMapping("/admin/cleanup-tokens")
    public ResponseEntity<ApiResponseDto<String>> cleanupExpiredTokens(HttpServletRequest request) {
        int deletedTokens = tokenCleanupService.cleanupExpiredTokensManually();

        return ResponseEntity.ok(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.OK.value())
                        .message("Limpieza completada exitosamente")
                        .data("Tokens eliminados: " + deletedTokens)
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }
}