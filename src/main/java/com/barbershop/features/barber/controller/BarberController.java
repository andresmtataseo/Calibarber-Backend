package com.barbershop.features.barber.controller;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.features.barber.dto.BarberAvailabilityResponseDto;
import com.barbershop.features.barber.dto.BarberResponseDto;
import com.barbershop.features.barber.dto.request.CreateBarberAvailabilityRequestDto;
import com.barbershop.features.barber.dto.request.CreateBarberRequestDto;
import com.barbershop.features.barber.dto.request.UpdateBarberRequestDto;
import com.barbershop.features.barber.model.DayOfWeek;
import com.barbershop.features.barber.service.BarberAvailabilityService;
import com.barbershop.features.barber.service.BarberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/barbers")
@RequiredArgsConstructor
@Tag(name = "Barberos", description = "Operaciones relacionadas con barberos y su disponibilidad")
public class BarberController {

    private final BarberService barberService;
    private final BarberAvailabilityService availabilityService;

    // ========== ENDPOINTS CRUD BÁSICOS PARA BARBEROS ==========

    @Operation(
            summary = "Crear nuevo barbero",
            description = "Crea un nuevo barbero en el sistema.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Barbero creado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos de entrada inválidos"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "El usuario ya es barbero en esta barbería"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponseDto<BarberResponseDto>> createBarber(
            @Valid @RequestBody CreateBarberRequestDto createDto,
            HttpServletRequest request) {

        BarberResponseDto barber = barberService.createBarber(createDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.<BarberResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Barbero creado exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(barber)
                        .build()
        );
    }

    @Operation(
            summary = "Obtener barberos",
            description = "Devuelve una lista paginada de barberos o un barbero específico por ID. Permite filtrar por barbería y especialización.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Operación exitosa",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Barbero no encontrado (cuando se especifica ID)"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ApiResponseDto<?>> getBarbers(
            @Parameter(description = "ID del barbero específico") @RequestParam(required = false) String id,
            @Parameter(description = "ID de la barbería para filtrar") @RequestParam(required = false) String barbershopId,
            @Parameter(description = "Especialización para filtrar") @RequestParam(required = false) String specialization,
            Pageable pageable,
            HttpServletRequest request) {

        if (id != null && !id.isEmpty()) {
            // Obtener barbero específico por ID
            BarberResponseDto barber = barberService.getBarberById(id);
            return ResponseEntity.ok(
                    ApiResponseDto.<BarberResponseDto>builder()
                            .status(HttpStatus.OK.value())
                            .message("Barbero obtenido exitosamente")
                            .timestamp(LocalDateTime.now())
                            .path(request.getRequestURI())
                            .data(barber)
                            .build()
            );
        } else if (barbershopId != null && !barbershopId.isEmpty()) {
            // Obtener barberos por barbería
            Page<BarberResponseDto> barbers = barberService.getBarbersByBarbershop(barbershopId, pageable);
            return ResponseEntity.ok(
                    ApiResponseDto.<Page<BarberResponseDto>>builder()
                            .status(HttpStatus.OK.value())
                            .message("Barberos de la barbería obtenidos exitosamente")
                            .timestamp(LocalDateTime.now())
                            .path(request.getRequestURI())
                            .data(barbers)
                            .build()
            );
        } else if (specialization != null && !specialization.isEmpty()) {
            // Obtener barberos por especialización
            Page<BarberResponseDto> barbers = barberService.getBarbersBySpecialization(specialization, pageable);
            return ResponseEntity.ok(
                    ApiResponseDto.<Page<BarberResponseDto>>builder()
                            .status(HttpStatus.OK.value())
                            .message("Barberos con especialización obtenidos exitosamente")
                            .timestamp(LocalDateTime.now())
                            .path(request.getRequestURI())
                            .data(barbers)
                            .build()
            );
        } else {
            // Obtener todos los barberos paginados
            Page<BarberResponseDto> barbers = barberService.getAllBarbers(pageable);
            return ResponseEntity.ok(
                    ApiResponseDto.<Page<BarberResponseDto>>builder()
                            .status(HttpStatus.OK.value())
                            .message("Barberos obtenidos exitosamente")
                            .timestamp(LocalDateTime.now())
                            .path(request.getRequestURI())
                            .data(barbers)
                            .build()
            );
        }
    }

    @Operation(
            summary = "Actualizar barbero",
            description = "Actualiza los datos de un barbero existente.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Barbero actualizado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Barbero no encontrado"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "No tienes permisos para actualizar este barbero"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping
    public ResponseEntity<ApiResponseDto<BarberResponseDto>> updateBarber(
            @Parameter(description = "ID del barbero a actualizar") @RequestParam String id,
            @Valid @RequestBody UpdateBarberRequestDto updateDto,
            HttpServletRequest request) {

        BarberResponseDto barber = barberService.updateBarber(id, updateDto);

        return ResponseEntity.ok(
                ApiResponseDto.<BarberResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("Barbero actualizado exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(barber)
                        .build()
        );
    }

    @Operation(
            summary = "Eliminar barbero (soft delete)",
            description = "Marca un barbero como eliminado sin borrarlo físicamente de la base de datos.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Barbero eliminado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Barbero no encontrado"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "No tienes permisos para eliminar este barbero"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<ApiResponseDto<Void>> deleteBarber(
            @Parameter(description = "ID del barbero a eliminar") @RequestParam String id,
            HttpServletRequest request) {

        barberService.deleteBarber(id);

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Barbero eliminado exitosamente (soft delete)")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @Operation(
            summary = "Restaurar barbero eliminado",
            description = "Restaura un barbero que fue eliminado previamente.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Barbero restaurado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Barbero no encontrado"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "El barbero no está eliminado"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/restore")
    public ResponseEntity<ApiResponseDto<BarberResponseDto>> restoreBarber(
            @Parameter(description = "ID del barbero a restaurar") @RequestParam String id,
            HttpServletRequest request) {

        BarberResponseDto barber = barberService.restoreBarber(id);

        return ResponseEntity.ok(
                ApiResponseDto.<BarberResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("Barbero restaurado exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(barber)
                        .build()
        );
    }

    @Operation(
            summary = "Obtener barberos eliminados",
            description = "Devuelve una lista paginada de barberos eliminados.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Barberos eliminados obtenidos exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponseDto<Page<BarberResponseDto>>> getDeletedBarbers(
            Pageable pageable,
            HttpServletRequest request) {

        Page<BarberResponseDto> deletedBarbers = barberService.getDeletedBarbers(pageable);

        return ResponseEntity.ok(
                ApiResponseDto.<Page<BarberResponseDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Barberos eliminados obtenidos exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(deletedBarbers)
                        .build()
        );
    }

    // ========== ENDPOINTS PARA GESTIÓN DE DISPONIBILIDAD ==========

    @Operation(
            summary = "Crear disponibilidad de barbero",
            description = "Crea una nueva disponibilidad horaria para un barbero.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Disponibilidad creada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos inválidos o conflicto de horarios"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Barbero no encontrado"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/availability")
    public ResponseEntity<ApiResponseDto<BarberAvailabilityResponseDto>> createAvailability(
            @Valid @RequestBody CreateBarberAvailabilityRequestDto createDto,
            HttpServletRequest request) {

        BarberAvailabilityResponseDto availability = availabilityService.createAvailability(createDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.<BarberAvailabilityResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Disponibilidad creada exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(availability)
                        .build()
        );
    }

    @Operation(
            summary = "Obtener disponibilidades de barbero",
            description = "Obtiene las disponibilidades de un barbero, opcionalmente filtradas por día.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Disponibilidades obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Barbero no encontrado"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{barberId}/availability")
    public ResponseEntity<ApiResponseDto<List<BarberAvailabilityResponseDto>>> getBarberAvailability(
            @Parameter(description = "ID del barbero") @PathVariable String barberId,
            @Parameter(description = "Día de la semana para filtrar") @RequestParam(required = false) DayOfWeek dayOfWeek,
            HttpServletRequest request) {

        List<BarberAvailabilityResponseDto> availabilities;
        String message;

        if (dayOfWeek != null) {
            availabilities = availabilityService.getAvailabilitiesByBarberAndDay(barberId, dayOfWeek);
            message = "Disponibilidades del barbero para el día obtenidas exitosamente";
        } else {
            availabilities = availabilityService.getAvailabilitiesByBarber(barberId);
            message = "Disponibilidades del barbero obtenidas exitosamente";
        }

        return ResponseEntity.ok(
                ApiResponseDto.<List<BarberAvailabilityResponseDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(availabilities)
                        .build()
        );
    }

    @Operation(
            summary = "Obtener disponibilidades paginadas de barbero",
            description = "Obtiene las disponibilidades de un barbero con paginación.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Disponibilidades paginadas obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Barbero no encontrado"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{barberId}/availability/paginated")
    public ResponseEntity<ApiResponseDto<Page<BarberAvailabilityResponseDto>>> getBarberAvailabilityPaginated(
            @Parameter(description = "ID del barbero") @PathVariable String barberId,
            Pageable pageable,
            HttpServletRequest request) {

        Page<BarberAvailabilityResponseDto> availabilities = 
                availabilityService.getAvailabilitiesByBarberPaginated(barberId, pageable);

        return ResponseEntity.ok(
                ApiResponseDto.<Page<BarberAvailabilityResponseDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Disponibilidades paginadas del barbero obtenidas exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(availabilities)
                        .build()
        );
    }

    @Operation(
            summary = "Actualizar disponibilidad",
            description = "Actualiza una disponibilidad existente de un barbero.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Disponibilidad actualizada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Disponibilidad no encontrada"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Conflicto de horarios"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/availability/{availabilityId}")
    public ResponseEntity<ApiResponseDto<BarberAvailabilityResponseDto>> updateAvailability(
            @Parameter(description = "ID de la disponibilidad") @PathVariable String availabilityId,
            @Valid @RequestBody CreateBarberAvailabilityRequestDto updateDto,
            HttpServletRequest request) {

        BarberAvailabilityResponseDto availability = 
                availabilityService.updateAvailability(availabilityId, updateDto);

        return ResponseEntity.ok(
                ApiResponseDto.<BarberAvailabilityResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("Disponibilidad actualizada exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(availability)
                        .build()
        );
    }

    @Operation(
            summary = "Eliminar disponibilidad",
            description = "Elimina una disponibilidad específica de un barbero.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Disponibilidad eliminada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Disponibilidad no encontrada"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/availability/{availabilityId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteAvailability(
            @Parameter(description = "ID de la disponibilidad") @PathVariable String availabilityId,
            HttpServletRequest request) {

        availabilityService.deleteAvailability(availabilityId);

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Disponibilidad eliminada exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @Operation(
            summary = "Habilitar/Deshabilitar disponibilidad",
            description = "Cambia el estado de disponibilidad de un horario específico.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Estado de disponibilidad actualizado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Disponibilidad no encontrada"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/availability/{availabilityId}/toggle")
    public ResponseEntity<ApiResponseDto<BarberAvailabilityResponseDto>> toggleAvailability(
            @Parameter(description = "ID de la disponibilidad") @PathVariable String availabilityId,
            @Parameter(description = "Estado de disponibilidad") @RequestParam boolean isAvailable,
            HttpServletRequest request) {

        BarberAvailabilityResponseDto availability = 
                availabilityService.toggleAvailability(availabilityId, isAvailable);

        return ResponseEntity.ok(
                ApiResponseDto.<BarberAvailabilityResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("Estado de disponibilidad actualizado exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(availability)
                        .build()
        );
    }

    @Operation(
            summary = "Buscar barberos disponibles",
            description = "Busca barberos disponibles en una barbería específica para un día y hora determinados.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Barberos disponibles obtenidos exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/available")
    public ResponseEntity<ApiResponseDto<List<BarberAvailabilityResponseDto>>> findAvailableBarbers(
            @Parameter(description = "ID de la barbería") @RequestParam String barbershopId,
            @Parameter(description = "Día de la semana") @RequestParam DayOfWeek dayOfWeek,
            @Parameter(description = "Hora específica (formato HH:mm:ss)") @RequestParam LocalTime time,
            HttpServletRequest request) {

        List<BarberAvailabilityResponseDto> availableBarbers = 
                availabilityService.findAvailableBarbersAtTime(barbershopId, dayOfWeek, time);

        return ResponseEntity.ok(
                ApiResponseDto.<List<BarberAvailabilityResponseDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Barberos disponibles obtenidos exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(availableBarbers)
                        .build()
        );
    }

    @Operation(
            summary = "Obtener disponibilidades por día",
            description = "Obtiene todas las disponibilidades para un día específico de la semana.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Disponibilidades del día obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/availability/day/{dayOfWeek}")
    public ResponseEntity<ApiResponseDto<List<BarberAvailabilityResponseDto>>> getAvailabilitiesByDay(
            @Parameter(description = "Día de la semana") @PathVariable DayOfWeek dayOfWeek,
            HttpServletRequest request) {

        List<BarberAvailabilityResponseDto> availabilities = 
                availabilityService.getAvailabilitiesByDay(dayOfWeek);

        return ResponseEntity.ok(
                ApiResponseDto.<List<BarberAvailabilityResponseDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Disponibilidades del día obtenidas exitosamente")
                        .timestamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .data(availabilities)
                        .build()
        );
    }
}