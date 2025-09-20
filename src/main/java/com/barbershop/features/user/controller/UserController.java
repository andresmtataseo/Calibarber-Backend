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
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
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

    /**
     * Crea un nuevo usuario en el sistema con los datos proporcionados.
     *
     * Permisos de acceso:
     * - ROLE_ADMIN: Puede crear cualquier usuario con cualquier rol
     * - ROLE_BARBER: No tiene permisos para crear usuarios
     * - ROLE_CLIENT: No tiene permisos para crear usuarios
     *
     * @param createDto Datos del usuario a crear
     * @param request Request HTTP para extraer el token de autenticación
     * @return ResponseEntity con los datos del usuario creado
     */
    @Operation(
            summary = "Crear nuevo usuario",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ROLE_ADMIN:</strong> Puede crear cualquier usuario con cualquier rol<br/>" +
                         "• <strong>ROLE_BARBER:</strong> No tiene permisos para crear usuarios<br/>" +
                         "• <strong>ROLE_CLIENT:</strong> No tiene permisos para crear usuarios",
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

    /**
     * Obtiene una lista paginada de usuarios o un usuario específico por ID.
     *
     * Permisos de acceso:
     * - ROLE_ADMIN: Puede obtener cualquier usuario o lista completa de usuarios
     * - ROLE_BARBER: Solo puede obtener su propia información de usuario
     * - ROLE_CLIENT: Solo puede obtener su propia información de usuario
     *
     * @param id ID del usuario específico (opcional)
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el cual ordenar
     * @param sortDir Dirección del ordenamiento (asc/desc)
     * @param request Request HTTP para extraer el token de autenticación
     * @return ResponseEntity con los datos del usuario o lista de usuarios
     */
    @Operation(
            summary = "Obtener usuarios",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ROLE_ADMIN:</strong> Puede obtener cualquier usuario o lista completa de usuarios<br/>" +
                         "• <strong>ROLE_BARBER:</strong> Solo puede obtener su propia información de usuario<br/>" +
                         "• <strong>ROLE_CLIENT:</strong> Solo puede obtener su propia información de usuario",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Operación exitosa",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @GetMapping
    public ResponseEntity<ApiResponseDto<?>> getUsers(
            @RequestParam(required = false) String id,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "firstName")
            @RequestParam(defaultValue = "firstName") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir,
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
            Page<UserResponseDto> users = userService.getAllUsers(page, size, sortBy, sortDir);
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

    /**
     * Actualiza los datos de un usuario existente en el sistema.
     *
     * Permisos de acceso:
     * - ROLE_ADMIN: Puede actualizar cualquier usuario
     * - ROLE_BARBER: Solo puede actualizar su propia información de usuario
     * - ROLE_CLIENT: Solo puede actualizar su propia información de usuario
     *
     * @param id ID del usuario a actualizar
     * @param updateDto Datos actualizados del usuario
     * @param request Request HTTP para extraer el token de autenticación
     * @return ResponseEntity con los datos del usuario actualizado
     */
    @Operation(
            summary = "Actualizar usuario",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ROLE_ADMIN:</strong> Puede actualizar cualquier usuario<br/>" +
                         "• <strong>ROLE_BARBER:</strong> Solo puede actualizar su propia información de usuario<br/>" +
                         "• <strong>ROLE_CLIENT:</strong> Solo puede actualizar su propia información de usuario",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usuario actualizado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
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

    /**
     * Marca un usuario como eliminado sin borrarlo físicamente de la base de datos.
     *
     * Permisos de acceso:
     * - ROLE_ADMIN: Puede eliminar cualquier usuario del sistema
     * - ROLE_BARBER: No tiene permisos para eliminar usuarios
     * - ROLE_CLIENT: No tiene permisos para eliminar usuarios
     *
     * @param id ID del usuario a eliminar
     * @param request Request HTTP para extraer el token de autenticación
     * @return ResponseEntity con confirmación de eliminación
     */
    @Operation(
            summary = "Eliminar usuario (soft delete)",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ROLE_ADMIN:</strong> Puede eliminar cualquier usuario del sistema<br/>" +
                         "• <strong>ROLE_BARBER:</strong> No tiene permisos para eliminar usuarios<br/>" +
                         "• <strong>ROLE_CLIENT:</strong> No tiene permisos para eliminar usuarios",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usuario eliminado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
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

    /**
     * Restaura un usuario que fue eliminado previamente mediante soft delete.
     *
     * Permisos de acceso:
     * - ROLE_ADMIN: Puede restaurar cualquier usuario eliminado del sistema
     * - ROLE_BARBER: No tiene permisos para restaurar usuarios
     * - ROLE_CLIENT: No tiene permisos para restaurar usuarios
     *
     * @param id ID del usuario a restaurar
     * @param request Request HTTP para extraer el token de autenticación
     * @return ResponseEntity con los datos del usuario restaurado
     */
    @Operation(
            summary = "Restaurar usuario eliminado",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ROLE_ADMIN:</strong> Puede restaurar cualquier usuario eliminado del sistema<br/>" +
                         "• <strong>ROLE_BARBER:</strong> No tiene permisos para restaurar usuarios<br/>" +
                         "• <strong>ROLE_CLIENT:</strong> No tiene permisos para restaurar usuarios",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usuario restaurado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
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

    /**
     * Obtiene una lista paginada de usuarios que han sido eliminados mediante soft delete.
     *
     * Permisos de acceso:
     * - ROLE_ADMIN: Puede obtener la lista completa de usuarios eliminados
     * - ROLE_BARBER: No tiene permisos para ver usuarios eliminados
     * - ROLE_CLIENT: No tiene permisos para ver usuarios eliminados
     *
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el cual ordenar
     * @param sortDir Dirección del ordenamiento (asc/desc)
     * @param request Request HTTP para extraer el token de autenticación
     * @return ResponseEntity con la lista paginada de usuarios eliminados
     */
    @Operation(
            summary = "Obtener usuarios eliminados",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ROLE_ADMIN:</strong> Puede obtener la lista completa de usuarios eliminados<br/>" +
                         "• <strong>ROLE_BARBER:</strong> No tiene permisos para ver usuarios eliminados<br/>" +
                         "• <strong>ROLE_CLIENT:</strong> No tiene permisos para ver usuarios eliminados",
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
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "firstName")
            @RequestParam(defaultValue = "firstName") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {

        Page<UserResponseDto> deletedUsers = userService.getDeletedUsers(page, size, sortBy, sortDir);

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

    /**
     * Obtiene el total de usuarios activos en el sistema.
     *
     * Permisos de acceso:
     * - ROLE_ADMIN: Puede obtener el total de usuarios activos
     * - ROLE_BARBER: No tiene permisos para obtener estadísticas
     * - ROLE_CLIENT: No tiene permisos para obtener estadísticas
     *
     * @param request Request HTTP para extraer el token de autenticación
     * @return ResponseEntity con el total de usuarios activos
     */
    @Operation(
            summary = "Obtener total de usuarios activos",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ROLE_ADMIN:</strong> Puede obtener el total de usuarios activos<br/>" +
                         "• <strong>ROLE_BARBER:</strong> No tiene permisos para obtener estadísticas<br/>" +
                         "• <strong>ROLE_CLIENT:</strong> No tiene permisos para obtener estadísticas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Total de usuarios activos obtenido exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/count/active")
    public ResponseEntity<ApiResponseDto<Long>> getTotalActiveUsers(HttpServletRequest request) {
        
        Long totalActiveUsers = userService.getTotalActiveUsers();

        return ResponseEntity.ok(
                ApiResponseDto.<Long>builder()
                        .status(HttpStatus.OK.value())
                        .message("Total de usuarios activos obtenido exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(totalActiveUsers)
                        .build()
        );
    }
}
