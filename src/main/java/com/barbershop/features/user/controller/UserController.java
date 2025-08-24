package com.barbershop.features.user.controller;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.features.user.dto.UserCreateDto;
import com.barbershop.features.user.dto.UserResponseDto;
import com.barbershop.features.user.dto.UserUpdateDto;
import com.barbershop.features.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones relacionadas con usuarios")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Crear nuevo usuario",
            description = "Crea un nuevo usuario en el sistema.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Usuario creado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createUser(
            @Valid @RequestBody UserCreateDto createDto,
            HttpServletRequest request) {

        UserResponseDto user = userService.createUser(createDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.<UserResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Usuario creado exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(user)
                        .build()
        );
    }

    @Operation(
            summary = "Obtener usuarios",
            description = "Devuelve una lista paginada de usuarios o un usuario específico por ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Operación exitosa",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado (cuando se especifica ID)"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ApiResponseDto<?>> getUsers(
            @RequestParam(required = false) String id,
            Pageable pageable,
            HttpServletRequest request) {

        if (id != null && !id.isEmpty()) {
            // Obtener usuario específico por ID
            UserResponseDto user = userService.getUserById(id);
            return ResponseEntity.ok(
                    ApiResponseDto.<UserResponseDto>builder()
                            .status(HttpStatus.OK.value())
                            .message("Usuario obtenido exitosamente")
                            .timestamp(LocalDateTime.now())
                            .path(request.getRequestURI())
                            .data(user)
                            .build()
            );
        } else {
            // Obtener todos los usuarios paginados
            Page<UserResponseDto> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(
                    ApiResponseDto.<Page<UserResponseDto>>builder()
                            .status(HttpStatus.OK.value())
                            .message("Usuarios obtenidos exitosamente")
                            .timestamp(LocalDateTime.now())
                            .path(request.getRequestURI())
                            .data(users)
                            .build()
            );
        }
    }

    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza los datos de un usuario existente.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usuario actualizado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUser(
            @RequestParam String id,
            @Valid @RequestBody UserUpdateDto updateDto,
            HttpServletRequest request) {

        UserResponseDto user = userService.updateUser(id, updateDto);

        return ResponseEntity.ok(
                ApiResponseDto.<UserResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("Usuario actualizado exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(user)
                        .build()
        );
    }

    @Operation(
            summary = "Eliminar usuario (soft delete)",
            description = "Marca un usuario como eliminado sin borrarlo físicamente de la base de datos.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usuario eliminado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<ApiResponseDto<Void>> deleteUser(
            @RequestParam String id,
            HttpServletRequest request) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Usuario eliminado exitosamente (soft delete)")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @Operation(
            summary = "Restaurar usuario eliminado",
            description = "Restaura un usuario que fue eliminado previamente.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usuario restaurado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "El usuario no está eliminado"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/restore")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> restoreUser(
            @RequestParam String id,
            HttpServletRequest request) {

        UserResponseDto user = userService.restoreUser(id);

        return ResponseEntity.ok(
                ApiResponseDto.<UserResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("Usuario restaurado exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(user)
                        .build()
        );
    }

    @Operation(
            summary = "Obtener usuarios eliminados",
            description = "Devuelve una lista paginada de usuarios eliminados.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usuarios eliminados obtenidos exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> getDeletedUsers(
            Pageable pageable,
            HttpServletRequest request) {

        Page<UserResponseDto> deletedUsers = userService.getDeletedUsers(pageable);

        return ResponseEntity.ok(
                ApiResponseDto.<Page<UserResponseDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Usuarios eliminados obtenidos exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(deletedUsers)
                        .build()
        );
    }



}
