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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Citas", description = "Operaciones relacionadas con la gestión de citas")
public class    AppointmentController {

    private final AppointmentService appointmentService;

    // ========== ENDPOINTS CRUD BÁSICOS ==========

    /**
     * Crea una nueva cita en el sistema de barbería
     *
     * Permisos de acceso:
     * - ADMIN: Puede crear citas para cualquier cliente
     * - BARBER: Puede crear citas para cualquier cliente
     * - CLIENT: Solo puede crear citas para sí mismo
     *
     * @param request Datos de la nueva cita a crear
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con los datos de la cita creada
     */
    @Operation(
            summary = "Crear nueva cita",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede crear citas para cualquier cliente<br/>" +
                         "• <strong>BARBER:</strong> Puede crear citas para cualquier cliente<br/>" +
                         "• <strong>CLIENT:</strong> Solo puede crear citas para sí mismo",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Cita creada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponseDto<AppointmentResponseDto>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequestDto request,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<AppointmentResponseDto> response = appointmentService.createAppointment(request, token);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene los detalles de una cita específica por su ID
     *
     * Permisos de acceso:
     * - ADMIN: Puede ver cualquier cita del sistema
     * - BARBER: Solo puede ver citas asignadas a él
     * - CLIENT: Solo puede ver sus propias citas
     *
     * @param appointmentId ID único de la cita a consultar
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con los detalles de la cita solicitada
     */
    @Operation(
            summary = "Obtener cita por ID",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede ver cualquier cita del sistema<br/>" +
                         "• <strong>BARBER:</strong> Solo puede ver citas asignadas a él<br/>" +
                         "• <strong>CLIENT:</strong> Solo puede ver sus propias citas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita obtenida exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/by-id")
    public ResponseEntity<ApiResponseDto<AppointmentResponseDto>> getAppointmentById(
            @Parameter(description = "ID de la cita", required = true)
            @RequestParam String appointmentId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<AppointmentResponseDto> response = appointmentService.getAppointmentById(appointmentId, token);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todas las citas del sistema con paginación y ordenamiento
     *
     * Permisos de acceso:
     * - ADMIN: Acceso completo a todas las citas del sistema
     * - BARBER: Sin acceso a este endpoint
     * - CLIENT: Sin acceso a este endpoint
     *
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el cual ordenar
     * @param sortDir Dirección del ordenamiento (asc/desc)
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta paginada con todas las citas del sistema
     */
    @Operation(
            summary = "Obtener todas las citas",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Acceso completo a todas las citas del sistema<br/>" +
                         "• <strong>BARBER:</strong> Sin acceso a este endpoint<br/>" +
                         "• <strong>CLIENT:</strong> Sin acceso a este endpoint",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Citas obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/all")
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

    /**
     * Actualiza los datos de una cita existente
     *
     * Permisos de acceso:
     * - ADMIN: Puede actualizar cualquier cita del sistema
     * - BARBER: Solo puede actualizar citas asignadas a él
     * - CLIENT: Solo puede actualizar sus propias citas
     *
     * @param appointmentId ID único de la cita a actualizar
     * @param request Datos actualizados de la cita
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con los datos actualizados de la cita
     */
    @Operation(
            summary = "Actualizar cita",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede actualizar cualquier cita del sistema<br/>" +
                         "• <strong>BARBER:</strong> Solo puede actualizar citas asignadas a él<br/>" +
                         "• <strong>CLIENT:</strong> Solo puede actualizar sus propias citas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita actualizada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/update")
    public ResponseEntity<ApiResponseDto<AppointmentResponseDto>> updateAppointment(
            @Parameter(description = "ID de la cita", required = true)
            @RequestParam String appointmentId,
            @Valid @RequestBody UpdateAppointmentRequestDto request,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<AppointmentResponseDto> response = appointmentService.updateAppointment(appointmentId, request, token);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina una cita del sistema de forma permanente
     *
     * Permisos de acceso:
     * - ADMIN: Acceso completo para eliminar cualquier cita
     * - BARBER: Sin acceso a este endpoint
     * - CLIENT: Sin acceso a este endpoint
     *
     * @param appointmentId ID único de la cita a eliminar
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta de confirmación de eliminación
     */
    @Operation(
            summary = "Eliminar cita",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Acceso completo para eliminar cualquier cita<br/>" +
                         "• <strong>BARBER:</strong> Sin acceso a este endpoint<br/>" +
                         "• <strong>CLIENT:</strong> Sin acceso a este endpoint",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita eliminada exitosamente"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponseDto<Void>> deleteAppointment(
            @Parameter(description = "ID de la cita", required = true)
            @RequestParam String appointmentId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<Void> response = appointmentService.deleteAppointment(appointmentId, token);
        
        return ResponseEntity.ok(response);
    }

    // ========== ENDPOINTS DE CONSULTA POR FILTROS ==========

    /**
     * Obtiene todas las citas de un cliente específico con paginación
     *
     * Permisos de acceso:
     * - ADMIN: Puede ver citas de cualquier cliente
     * - BARBER: Sin acceso a este endpoint
     * - CLIENT: Solo puede ver sus propias citas
     *
     * @param clientId ID único del cliente
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el cual ordenar
     * @param sortDir Dirección del ordenamiento (asc/desc)
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta paginada con las citas del cliente
     */
    @Operation(
            summary = "Obtener citas por cliente",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede ver citas de cualquier cliente<br/>" +
                         "• <strong>BARBER:</strong> Sin acceso a este endpoint<br/>" +
                         "• <strong>CLIENT:</strong> Solo puede ver sus propias citas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Citas del cliente obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/by-client")
    public ResponseEntity<ApiResponseDto<Page<AppointmentResponseDto>>> getAppointmentsByClient(
            @Parameter(description = "ID del cliente", required = true)
            @RequestParam String clientId,
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

    /**
     * Obtiene todas las citas de un barbero específico con paginación
     *
     * Permisos de acceso:
     * - ADMIN: Puede ver citas de cualquier barbero
     * - BARBER: Solo puede ver sus propias citas
     * - CLIENT: Sin acceso a este endpoint
     *
     * @param barberId ID único del barbero
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el cual ordenar
     * @param sortDir Dirección del ordenamiento (asc/desc)
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta paginada con las citas del barbero
     */
    @Operation(
            summary = "Obtener citas por barbero",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede ver citas de cualquier barbero<br/>" +
                         "• <strong>BARBER:</strong> Solo puede ver sus propias citas<br/>" +
                         "• <strong>CLIENT:</strong> Sin acceso a este endpoint",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Citas del barbero obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/by-barber")
    public ResponseEntity<ApiResponseDto<Page<AppointmentResponseDto>>> getAppointmentsByBarber(
            @Parameter(description = "ID del barbero", required = true)
            @RequestParam String barberId,
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

    /**
     * Obtiene todas las citas filtradas por estado específico con paginación
     *
     * Permisos de acceso:
     * - ADMIN: Acceso completo para filtrar citas por cualquier estado
     * - BARBER: Sin acceso a este endpoint
     * - CLIENT: Sin acceso a este endpoint
     *
     * @param status Estado específico de las citas a filtrar
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el cual ordenar
     * @param sortDir Dirección del ordenamiento (asc/desc)
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta paginada con las citas filtradas por estado
     */
    @Operation(
            summary = "Obtener citas por estado",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Acceso completo para filtrar citas por cualquier estado<br/>" +
                         "• <strong>BARBER:</strong> Sin acceso a este endpoint<br/>" +
                         "• <strong>CLIENT:</strong> Sin acceso a este endpoint",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Citas filtradas por estado obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/by-status")
    public ResponseEntity<ApiResponseDto<Page<AppointmentResponseDto>>> getAppointmentsByStatus(
            @Parameter(description = "Estado de la cita", required = true)
            @RequestParam AppointmentStatus status,
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

    /**
     * Obtiene las próximas citas de un cliente específico
     *
     * Permisos de acceso:
     * - ADMIN: Puede ver próximas citas de cualquier cliente
     * - BARBER: Sin acceso a este endpoint
     * - CLIENT: Solo puede ver sus propias próximas citas
     *
     * @param clientId ID único del cliente
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con las próximas citas del cliente
     */
    @Operation(
            summary = "Obtener próximas citas del cliente",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede ver próximas citas de cualquier cliente<br/>" +
                         "• <strong>BARBER:</strong> Sin acceso a este endpoint<br/>" +
                         "• <strong>CLIENT:</strong> Solo puede ver sus propias próximas citas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Próximas citas del cliente obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/upcoming/by-client")
    public ResponseEntity<ApiResponseDto<List<AppointmentResponseDto>>> getUpcomingAppointmentsByClient(
            @Parameter(description = "ID del cliente", required = true)
            @RequestParam String clientId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<List<AppointmentResponseDto>> response = appointmentService.getUpcomingAppointmentsByClient(clientId, token);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene las próximas citas de un barbero específico
     *
     * Permisos de acceso:
     * - ADMIN: Puede ver próximas citas de cualquier barbero
     * - BARBER: Solo puede ver sus propias próximas citas
     * - CLIENT: Sin acceso a este endpoint
     *
     * @param barberId ID único del barbero
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con las próximas citas del barbero
     */
    @Operation(
            summary = "Obtener próximas citas del barbero",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede ver próximas citas de cualquier barbero<br/>" +
                         "• <strong>BARBER:</strong> Solo puede ver sus propias próximas citas<br/>" +
                         "• <strong>CLIENT:</strong> Sin acceso a este endpoint",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Próximas citas del barbero obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/upcoming/by-barber")
    public ResponseEntity<ApiResponseDto<List<AppointmentResponseDto>>> getUpcomingAppointmentsByBarber(
            @Parameter(description = "ID del barbero", required = true)
            @RequestParam String barberId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<List<AppointmentResponseDto>> response = appointmentService.getUpcomingAppointmentsByBarber(barberId, token);
        
        return ResponseEntity.ok(response);
    }

    // ========== ENDPOINTS DE GESTIÓN DE ESTADO ==========

    /**
     * Cancela una cita existente cambiando su estado a CANCELLED
     *
     * Permisos de acceso:
     * - ADMIN: Puede cancelar cualquier cita
     * - BARBER: Solo puede cancelar citas asignadas a él
     * - CLIENT: Solo puede cancelar sus propias citas
     *
     * @param appointmentId ID único de la cita a cancelar
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la cita cancelada
     */
    @Operation(
            summary = "Cancelar cita",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede cancelar cualquier cita<br/>" +
                         "• <strong>BARBER:</strong> Solo puede cancelar citas asignadas a él<br/>" +
                         "• <strong>CLIENT:</strong> Solo puede cancelar sus propias citas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita cancelada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/cancel")
    public ResponseEntity<ApiResponseDto<AppointmentResponseDto>> cancelAppointment(
            @Parameter(description = "ID de la cita", required = true)
            @RequestParam String appointmentId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<AppointmentResponseDto> response = appointmentService.cancelAppointment(appointmentId, token);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Confirma una cita existente cambiando su estado a CONFIRMED
     *
     * Permisos de acceso:
     * - ADMIN: Puede confirmar cualquier cita
     * - BARBER: Solo puede confirmar citas asignadas a él
     * - CLIENT: Sin acceso a este endpoint
     *
     * @param appointmentId ID único de la cita a confirmar
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la cita confirmada
     */
    @Operation(
            summary = "Confirmar cita",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede confirmar cualquier cita<br/>" +
                         "• <strong>BARBER:</strong> Solo puede confirmar citas asignadas a él<br/>" +
                         "• <strong>CLIENT:</strong> Sin acceso a este endpoint",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita confirmada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/confirm")
    public ResponseEntity<ApiResponseDto<AppointmentResponseDto>> confirmAppointment(
            @Parameter(description = "ID de la cita", required = true)
            @RequestParam String appointmentId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<AppointmentResponseDto> response = appointmentService.confirmAppointment(appointmentId, token);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Marca una cita como completada cambiando su estado a COMPLETED
     *
     * Permisos de acceso:
     * - ADMIN: Puede completar cualquier cita
     * - BARBER: Solo puede completar citas asignadas a él
     * - CLIENT: Sin acceso a este endpoint
     *
     * @param appointmentId ID único de la cita a completar
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la cita completada
     */
    @Operation(
            summary = "Completar cita",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede completar cualquier cita<br/>" +
                         "• <strong>BARBER:</strong> Solo puede completar citas asignadas a él<br/>" +
                         "• <strong>CLIENT:</strong> Sin acceso a este endpoint",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita completada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/complete")
    public ResponseEntity<ApiResponseDto<AppointmentResponseDto>> completeAppointment(
            @Parameter(description = "ID de la cita", required = true)
            @RequestParam String appointmentId,
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