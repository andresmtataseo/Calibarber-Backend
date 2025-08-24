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
import org.springframework.data.domain.Pageable;
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

    @Operation(
            summary = "Crear nueva barbería",
            description = "Crea una nueva barbería en el sistema.",
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

    @Operation(
            summary = "Obtener barberías",
            description = "Devuelve una lista paginada de barberías o una barbería específica por ID.",
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
            Pageable pageable,
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
            Page<BarbershopResponseDto> barbershops = barbershopService.getAllBarbershops(pageable);
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

    @Operation(
            summary = "Actualizar barbería",
            description = "Actualiza los datos de una barbería existente.",
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

    @Operation(
            summary = "Eliminar barbería (soft delete)",
            description = "Marca una barbería como eliminada sin borrarla físicamente de la base de datos.",
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
    public ResponseEntity<ApiResponseDto<Void>> deleteBarbershop(
            @RequestParam String id,
            HttpServletRequest request) {

        barbershopService.deleteBarbershop(id);

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
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
            description = "Devuelve una lista paginada de barberías eliminadas.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Barberías eliminadas obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponseDto<Page<BarbershopResponseDto>>> getDeletedBarbershops(
            Pageable pageable,
            HttpServletRequest request) {

        Page<BarbershopResponseDto> deletedBarbershops = barbershopService.getDeletedBarbershops(pageable);

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