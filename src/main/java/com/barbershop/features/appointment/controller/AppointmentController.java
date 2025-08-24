package com.barbershop.features.appointment.controller;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.features.appointment.dto.AppointmentResponseDto;
import com.barbershop.features.appointment.dto.request.CreateAppointmentRequestDto;
import com.barbershop.features.appointment.dto.request.UpdateAppointmentRequestDto;
import com.barbershop.features.appointment.model.enums.AppointmentStatus;
import com.barbershop.features.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Citas", description = "Operaciones relacionadas con la gestión de citas")
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ========== ENDPOINTS CRUD BÁSICOS ==========

    @Operation(
            summary = "Crear nueva cita",
            description = "Crea una nueva cita en el sistema. Los clientes solo pueden crear citas para sí mismos.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Cita creada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos inválidos o barbero no disponible"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "No tienes permisos para crear esta cita"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario, barbero o servicio no encontrado"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<ApiResponseDto<AppointmentResponseDto>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequestDto request,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<AppointmentResponseDto> response = appointmentService.createAppointment(request, token);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Obtener cita por ID",
            description = "Obtiene los detalles de una cita específica. Los usuarios solo pueden ver sus propias citas.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita obtenida exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "No tienes permisos para ver esta cita"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Cita no encontrada"
                    )
            }
    )
    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponseDto<AppointmentResponseDto>> getAppointmentById(
            @Parameter(description = "ID de la cita", required = true)
            @PathVariable String appointmentId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<AppointmentResponseDto> response = appointmentService.getAppointmentById(appointmentId, token);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener todas las citas",
            description = "Obtiene todas las citas del sistema con paginación. Solo disponible para administradores.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Citas obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Solo los administradores pueden ver todas las citas"
                    )
            }
    )
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<AppointmentResponseDto>>> getAllAppointments(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "appointmentDatetimeStart")
            @RequestParam(defaultValue = "appointmentDatetimeStart") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<Page<AppointmentResponseDto>> response = appointmentService.getAllAppointments(page, size, sortBy, sortDir, token);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Actualizar cita",
            description = "Actualiza los datos de una cita existente. Los usuarios solo pueden modificar sus propias citas.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita actualizada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos inválidos o cita no se puede modificar"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "No tienes permisos para modificar esta cita"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Cita no encontrada"
                    )
            }
    )
    @PutMapping("/{appointmentId}")
    public ResponseEntity<ApiResponseDto<AppointmentResponseDto>> updateAppointment(
            @Parameter(description = "ID de la cita", required = true)
            @PathVariable String appointmentId,
            @Valid @RequestBody UpdateAppointmentRequestDto request,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<AppointmentResponseDto> response = appointmentService.updateAppointment(appointmentId, request, token);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Eliminar cita",
            description = "Elimina una cita del sistema. Solo disponible para administradores.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita eliminada exitosamente"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Solo los administradores pueden eliminar citas"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Cita no encontrada"
                    )
            }
    )
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteAppointment(
            @Parameter(description = "ID de la cita", required = true)
            @PathVariable String appointmentId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<Void> response = appointmentService.deleteAppointment(appointmentId, token);
        
        return ResponseEntity.ok(response);
    }

    // ========== ENDPOINTS DE CONSULTA POR FILTROS ==========

    @Operation(
            summary = "Obtener citas por cliente",
            description = "Obtiene todas las citas de un cliente específico. Los clientes solo pueden ver sus propias citas.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Citas del cliente obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "No tienes permisos para ver las citas de este cliente"
                    )
            }
    )
    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponseDto<Page<AppointmentResponseDto>>> getAppointmentsByClient(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable String clientId,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "appointmentDatetimeStart")
            @RequestParam(defaultValue = "appointmentDatetimeStart") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<Page<AppointmentResponseDto>> response = appointmentService.getAppointmentsByClient(clientId, page, size, sortBy, sortDir, token);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener citas por barbero",
            description = "Obtiene todas las citas de un barbero específico. Los barberos solo pueden ver sus propias citas.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Citas del barbero obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "No tienes permisos para ver las citas de este barbero"
                    )
            }
    )
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<ApiResponseDto<Page<AppointmentResponseDto>>> getAppointmentsByBarber(
            @Parameter(description = "ID del barbero", required = true)
            @PathVariable String barberId,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "appointmentDatetimeStart")
            @RequestParam(defaultValue = "appointmentDatetimeStart") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<Page<AppointmentResponseDto>> response = appointmentService.getAppointmentsByBarber(barberId, page, size, sortBy, sortDir, token);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener citas por estado",
            description = "Obtiene todas las citas filtradas por estado. Solo disponible para administradores.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Citas filtradas por estado obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Solo los administradores pueden filtrar por estado"
                    )
            }
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponseDto<Page<AppointmentResponseDto>>> getAppointmentsByStatus(
            @Parameter(description = "Estado de la cita", required = true)
            @PathVariable AppointmentStatus status,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "appointmentDatetimeStart")
            @RequestParam(defaultValue = "appointmentDatetimeStart") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<Page<AppointmentResponseDto>> response = appointmentService.getAppointmentsByStatus(status, page, size, sortBy, sortDir, token);
        
        return ResponseEntity.ok(response);
    }

    // ========== ENDPOINTS DE PRÓXIMAS CITAS ==========

    @Operation(
            summary = "Obtener próximas citas del cliente",
            description = "Obtiene las próximas citas de un cliente específico. Los clientes solo pueden ver sus propias citas.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Próximas citas obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "No tienes permisos para ver las citas de este cliente"
                    )
            }
    )
    @GetMapping("/client/{clientId}/upcoming")
    public ResponseEntity<ApiResponseDto<List<AppointmentResponseDto>>> getUpcomingAppointmentsByClient(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable String clientId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<List<AppointmentResponseDto>> response = appointmentService.getUpcomingAppointmentsByClient(clientId, token);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener próximas citas del barbero",
            description = "Obtiene las próximas citas de un barbero específico. Los barberos solo pueden ver sus propias citas.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Próximas citas obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "No tienes permisos para ver las citas de este barbero"
                    )
            }
    )
    @GetMapping("/barber/{barberId}/upcoming")
    public ResponseEntity<ApiResponseDto<List<AppointmentResponseDto>>> getUpcomingAppointmentsByBarber(
            @Parameter(description = "ID del barbero", required = true)
            @PathVariable String barberId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<List<AppointmentResponseDto>> response = appointmentService.getUpcomingAppointmentsByBarber(barberId, token);
        
        return ResponseEntity.ok(response);
    }

    // ========== ENDPOINTS DE GESTIÓN DE ESTADO ==========

    @Operation(
            summary = "Cancelar cita",
            description = "Cancela una cita existente. Los usuarios pueden cancelar sus propias citas.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita cancelada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "La cita no se puede cancelar"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "No tienes permisos para cancelar esta cita"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Cita no encontrada"
                    )
            }
    )
    @PatchMapping("/{appointmentId}/cancel")
    public ResponseEntity<ApiResponseDto<AppointmentResponseDto>> cancelAppointment(
            @Parameter(description = "ID de la cita", required = true)
            @PathVariable String appointmentId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<AppointmentResponseDto> response = appointmentService.cancelAppointment(appointmentId, token);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Confirmar cita",
            description = "Confirma una cita programada. Solo disponible para barberos y administradores.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita confirmada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solo se pueden confirmar citas programadas"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Solo barberos y administradores pueden confirmar citas"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Cita no encontrada"
                    )
            }
    )
    @PatchMapping("/{appointmentId}/confirm")
    public ResponseEntity<ApiResponseDto<AppointmentResponseDto>> confirmAppointment(
            @Parameter(description = "ID de la cita", required = true)
            @PathVariable String appointmentId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<AppointmentResponseDto> response = appointmentService.confirmAppointment(appointmentId, token);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Completar cita",
            description = "Marca una cita como completada. Solo disponible para barberos y administradores.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita completada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solo se pueden completar citas confirmadas o en progreso"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Solo barberos y administradores pueden completar citas"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Cita no encontrada"
                    )
            }
    )
    @PatchMapping("/{appointmentId}/complete")
    public ResponseEntity<ApiResponseDto<AppointmentResponseDto>> completeAppointment(
            @Parameter(description = "ID de la cita", required = true)
            @PathVariable String appointmentId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<AppointmentResponseDto> response = appointmentService.completeAppointment(appointmentId, token);
        
        return ResponseEntity.ok(response);
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Extrae el token JWT del header Authorization de la petición HTTP
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Token JWT no encontrado en el header Authorization");
    }
}