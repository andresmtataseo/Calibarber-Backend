package com.barbershop.features.barbershop.controller;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.features.barbershop.dto.BarbershopCreateDto;
import com.barbershop.features.barbershop.dto.BarbershopResponseDto;
import com.barbershop.features.barbershop.dto.BarbershopUpdateDto;
import com.barbershop.features.barbershop.service.BarbershopService;
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
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/barbershops")
@RequiredArgsConstructor
@Tag(name = "Barberías", description = "Operaciones relacionadas con barberías")
public class BarbershopController {

    private final BarbershopService barbershopService;

    /**
     * Crea una nueva barbería en el sistema
     *
     * Permisos de acceso:
     * - ADMIN: Puede crear cualquier barbería
     * - BARBER: Acceso denegado
     * - CLIENT: Acceso denegado
     *
     * @param createDto Datos de la barbería a crear
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con la barbería creada
     */
    @Operation(
            summary = "Crear nueva barbería",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede crear cualquier barbería<br/>" +
                         "• <strong>BARBER:</strong> Acceso denegado<br/>" +
                         "• <strong>CLIENT:</strong> Acceso denegado",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Barbería creada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponseDto<BarbershopResponseDto>> createBarbershop(
            @Valid @RequestBody BarbershopCreateDto createDto,
            HttpServletRequest request) {

        BarbershopResponseDto barbershop = barbershopService.createBarbershop(createDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.<BarbershopResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Barbería creada exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(barbershop)
                        .build()
        );
    }

    /**
     * Obtiene barberías del sistema (todas paginadas o una específica por ID)
     *
     * Permisos de acceso:
     * - ADMIN: Puede obtener cualquier barbería
     * - BARBER: Puede obtener cualquier barbería
     * - CLIENT: Puede obtener cualquier barbería
     *
     * @param id ID de la barbería específica (opcional)
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el cual ordenar
     * @param sortDir Dirección del ordenamiento (asc/desc)
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con la barbería específica o lista paginada de barberías
     */
    @Operation(
            summary = "Obtener barberías",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede obtener cualquier barbería<br/>" +
                         "• <strong>BARBER:</strong> Puede obtener cualquier barbería<br/>" +
                         "• <strong>CLIENT:</strong> Puede obtener cualquier barbería",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Operación exitosa",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Barbería no encontrada (cuando se especifica ID)"
                    )
            }
    )
    @GetMapping
    public ResponseEntity<ApiResponseDto<?>> getBarbershops(
            @RequestParam(required = false) String id,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {

        if (id != null && !id.isEmpty()) {
            // Obtener barbería específica por ID
            BarbershopResponseDto barbershop = barbershopService.getBarbershopById(id);
            return ResponseEntity.ok(
                    ApiResponseDto.<BarbershopResponseDto>builder()
                            .status(HttpStatus.OK.value())
                            .message("Barbería obtenida exitosamente")
                            .timestamp(LocalDateTime.now())
                            .path(request.getRequestURI())
                            .data(barbershop)
                            .build()
            );
        } else {
            // Obtener todas las barberías paginadas
            Page<BarbershopResponseDto> barbershops = barbershopService.getAllBarbershops(page, size, sortBy, sortDir);
            return ResponseEntity.ok(
                    ApiResponseDto.<Page<BarbershopResponseDto>>builder()
                            .status(HttpStatus.OK.value())
                            .message("Barberías obtenidas exitosamente")
                            .timestamp(LocalDateTime.now())
                            .path(request.getRequestURI())
                            .data(barbershops)
                            .build()
            );
        }
    }

    /**
     * Actualiza los datos de una barbería existente
     *
     * Permisos de acceso:
     * - ADMIN: Puede actualizar cualquier barbería
     * - BARBER: Acceso denegado
     * - CLIENT: Acceso denegado
     *
     * @param id ID de la barbería a actualizar
     * @param updateDto Datos actualizados de la barbería
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con la barbería actualizada
     */
    @Operation(
            summary = "Actualizar barbería",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede actualizar cualquier barbería<br/>" +
                         "• <strong>BARBER:</strong> Acceso denegado<br/>" +
                         "• <strong>CLIENT:</strong> Acceso denegado",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Barbería actualizada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Barbería no encontrada"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping
    public ResponseEntity<ApiResponseDto<BarbershopResponseDto>> updateBarbershop(
            @RequestParam String id,
            @Valid @RequestBody BarbershopUpdateDto updateDto,
            HttpServletRequest request) {

        BarbershopResponseDto barbershop = barbershopService.updateBarbershop(id, updateDto);

        return ResponseEntity.ok(
                ApiResponseDto.<BarbershopResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("Barbería actualizada exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(barbershop)
                        .build()
        );
    }

    /**
     * Elimina una barbería del sistema
     *
     * Permisos de acceso:
     * - ADMIN: Puede eliminar cualquier barbería
     * - BARBER: Acceso denegado
     * - CLIENT: Acceso denegado
     *
     * @param id ID de la barbería a eliminar
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta confirmando la eliminación
     */
    @Operation(
            summary = "Eliminar barbería",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede eliminar cualquier barbería<br/>" +
                         "• <strong>BARBER:</strong> Acceso denegado<br/>" +
                         "• <strong>CLIENT:</strong> Acceso denegado",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Barbería eliminada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Barbería no encontrada"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<ApiResponseDto<String>> deleteBarbershop(
            @RequestParam String id,
            HttpServletRequest request) {

        barbershopService.deleteBarbershop(id);

        return ResponseEntity.ok(
                ApiResponseDto.<String>builder()
                        .status(HttpStatus.OK.value())
                        .message("Barbería eliminada exitosamente (soft delete)")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @Operation(
            summary = "Restaurar barbería eliminada",
            description = "Restaura una barbería que fue eliminada previamente.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Barbería restaurada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Barbería no encontrada"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "La barbería no está eliminada"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/restore")
    public ResponseEntity<ApiResponseDto<BarbershopResponseDto>> restoreBarbershop(
            @RequestParam String id,
            HttpServletRequest request) {

        BarbershopResponseDto barbershop = barbershopService.restoreBarbershop(id);

        return ResponseEntity.ok(
                ApiResponseDto.<BarbershopResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("Barbería restaurada exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(barbershop)
                        .build()
        );
    }

    @Operation(
            summary = "Obtener barberías eliminadas",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede obtener todas las barberías eliminadas<br/>" +
                         "• <strong>BARBER:</strong> No tiene acceso<br/>" +
                         "• <strong>CLIENT:</strong> No tiene acceso",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Operación exitosa",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Acceso denegado - Solo administradores"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponseDto<Page<BarbershopResponseDto>>> getDeletedBarbershops(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {

        Page<BarbershopResponseDto> deletedBarbershops = barbershopService.getDeletedBarbershops(page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                ApiResponseDto.<Page<BarbershopResponseDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Barberías eliminadas obtenidas exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(deletedBarbershops)
                        .build()
        );
    }


}