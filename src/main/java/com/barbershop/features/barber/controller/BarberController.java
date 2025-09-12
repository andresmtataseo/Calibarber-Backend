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

    /**
     * Crea un nuevo barbero en el sistema asociándolo a una barbería específica.
     *
     * Permisos de acceso:
     * - ADMIN: Puede crear barberos en cualquier barbería
     * - BARBER: No tiene permisos para crear otros barberos
     * - CLIENT: No tiene permisos para crear barberos
     *
     * @param createDto Datos del barbero a crear
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con los datos del barbero creado
     */
    @Operation(
            summary = "Crear nuevo barbero",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede crear barberos en cualquier barbería<br/>" +
                         "• <strong>BARBER:</strong> No tiene permisos para crear otros barberos<br/>" +
                         "• <strong>CLIENT:</strong> No tiene permisos para crear barberos",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Barbero creado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),

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

    /**
     * Obtiene barberos del sistema con diferentes opciones de filtrado y paginación.
     * Puede devolver un barbero específico por ID, barberos por barbería, por especialización o todos los barberos.
     *
     * Permisos de acceso:
     * - ADMIN: Puede obtener información de cualquier barbero

     * - BARBER: Solo puede obtener su propia información cuando se especifica ID
     * - CLIENT: Puede obtener información básica de barberos para agendar citas
     *
     * @param id ID del barbero específico (opcional)
     * @param barbershopId ID de la barbería para filtrar (opcional)
     * @param specialization Especialización para filtrar (opcional)
     * @param page Número de página para paginación
     * @param size Tamaño de página para paginación
     * @param sortBy Campo por el cual ordenar
     * @param sortDir Dirección de ordenamiento
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con barbero específico o lista paginada de barberos
     */
    @Operation(
            summary = "Obtener barberos",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede obtener información de cualquier barbero<br/>" +
                         "• <strong>BARBER:</strong> Solo puede obtener su propia información cuando se especifica ID<br/>" +
                         "• <strong>CLIENT:</strong> Puede obtener información básica de barberos para agendar citas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Operación exitosa",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),

            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ApiResponseDto<?>> getBarbers(
            @Parameter(description = "ID del barbero específico") @RequestParam(required = false) String id,
            @Parameter(description = "ID de la barbería para filtrar") @RequestParam(required = false) String barbershopId,
            @Parameter(description = "Especialización para filtrar") @RequestParam(required = false) String specialization,
            @Parameter(description = "Número de página (empezando desde 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Dirección de ordenamiento (asc o desc)") @RequestParam(defaultValue = "asc") String sortDir,
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
            Page<BarberResponseDto> barbers = barberService.getBarbersByBarbershop(barbershopId, page, size, sortBy, sortDir);
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
            Page<BarberResponseDto> barbers = barberService.getBarbersBySpecialization(specialization, page, size, sortBy, sortDir);
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
            Page<BarberResponseDto> barbers = barberService.getAllBarbers(page, size, sortBy, sortDir);
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

    /**
     * Actualiza los datos de un barbero existente en el sistema.
     *
     * Permisos de acceso:
     * - ADMIN: Puede actualizar cualquier barbero

     * - BARBER: No tiene permisos para actualizar otros barberos
     * - CLIENT: No tiene permisos para actualizar barberos
     *
     * @param id ID del barbero a actualizar
     * @param updateDto Datos actualizados del barbero
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con los datos del barbero actualizado
     */
    @Operation(
            summary = "Actualizar barbero",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede actualizar cualquier barbero<br/>" +
                         "• <strong>BARBER:</strong> No tiene permisos para actualizar otros barberos<br/>" +
                         "• <strong>CLIENT:</strong> No tiene permisos para actualizar barberos",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Barbero actualizado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),

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

    /**
     * Elimina un barbero del sistema mediante soft delete, marcándolo como inactivo
     * sin borrarlo físicamente de la base de datos.
     *
     * Permisos de acceso:
     * - ADMIN: Puede eliminar cualquier barbero

     * - BARBER: No tiene permisos para eliminar otros barberos
     * - CLIENT: No tiene permisos para eliminar barberos
     *
     * @param id ID del barbero a eliminar
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta de confirmación de eliminación
     */
    @Operation(
            summary = "Eliminar barbero (soft delete)",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede eliminar cualquier barbero<br/>" +
                         "• <strong>BARBER:</strong> No tiene permisos para eliminar otros barberos<br/>" +
                         "• <strong>CLIENT:</strong> No tiene permisos para eliminar barberos",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Barbero eliminado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),

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

    /**
     * Restaura un barbero que fue eliminado previamente mediante soft delete,
     * volviéndolo a marcar como activo en el sistema.
     *
     * Permisos de acceso:
     * - ADMIN: Puede restaurar cualquier barbero eliminado

     * - BARBER: No tiene permisos para restaurar barberos
     * - CLIENT: No tiene permisos para restaurar barberos
     *
     * @param id ID del barbero a restaurar
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con los datos del barbero restaurado
     */
    @Operation(
            summary = "Restaurar barbero eliminado",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede restaurar cualquier barbero eliminado<br/>" +
                         "• <strong>BARBER:</strong> No tiene permisos para restaurar barberos<br/>" +
                         "• <strong>CLIENT:</strong> No tiene permisos para restaurar barberos",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Barbero restaurado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),

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

    /**
     * Obtiene una lista paginada de barberos que han sido eliminados mediante soft delete.
     * Útil para administración y posible restauración de barberos.
     *
     * Permisos de acceso:
     * - ADMIN: Puede obtener todos los barberos eliminados del sistema

     * - BARBER: No tiene permisos para ver barberos eliminados
     * - CLIENT: No tiene permisos para ver barberos eliminados
     *
     * @param page Número de página para paginación
     * @param size Tamaño de página para paginación
     * @param sortBy Campo por el cual ordenar
     * @param sortDir Dirección de ordenamiento
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con lista paginada de barberos eliminados
     */
    @Operation(
            summary = "Obtener barberos eliminados",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede obtener todos los barberos eliminados del sistema<br/>" +
                         "• <strong>BARBER:</strong> No tiene permisos para ver barberos eliminados<br/>" +
                         "• <strong>CLIENT:</strong> No tiene permisos para ver barberos eliminados",
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
            @Parameter(description = "Número de página (empezando desde 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Dirección de ordenamiento (asc o desc)") @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {

        Page<BarberResponseDto> deletedBarbers = barberService.getDeletedBarbers(page, size, sortBy, sortDir);

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

    /**
     * Crea una nueva disponibilidad horaria para un barbero específico,
     * estableciendo los días y horarios en los que estará disponible para citas.
     *
     * Permisos de acceso:
     * - ADMIN: Puede crear disponibilidad para cualquier barbero

     * - BARBER: Solo puede crear su propia disponibilidad
     * - CLIENT: No tiene permisos para crear disponibilidades
     *
     * @param createDto Datos de la disponibilidad a crear
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con los datos de la disponibilidad creada
     */
    @Operation(
            summary = "Crear disponibilidad de barbero",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede crear disponibilidad para cualquier barbero<br/>" +
                         "• <strong>BARBER:</strong> Solo puede crear su propia disponibilidad<br/>" +
                         "• <strong>CLIENT:</strong> No tiene permisos para crear disponibilidades",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Disponibilidad creada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),

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

    /**
     * Obtiene la disponibilidad completa de un barbero específico, incluyendo
     * todos sus horarios disponibles para citas.
     *
     * Permisos de acceso:
     * - ADMIN: Puede obtener la disponibilidad de cualquier barbero

     * - BARBER: Puede obtener su propia disponibilidad y la de otros barberos para coordinación
     * - CLIENT: Puede obtener la disponibilidad de cualquier barbero para agendar citas
     *
     * @param barberId ID del barbero cuya disponibilidad se desea consultar
     * @param dayOfWeek Día de la semana para filtrar (opcional)
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con la disponibilidad del barbero
     */
    @Operation(
            summary = "Obtener disponibilidades de barbero",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede obtener la disponibilidad de cualquier barbero<br/>" +
                         "• <strong>BARBER:</strong> Puede obtener su propia disponibilidad y la de otros barberos para coordinación<br/>" +
                         "• <strong>CLIENT:</strong> Puede obtener la disponibilidad de cualquier barbero para agendar citas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Disponibilidades obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),

            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/availability")
    public ResponseEntity<ApiResponseDto<List<BarberAvailabilityResponseDto>>> getBarberAvailability(
            @Parameter(description = "ID del barbero") @RequestParam String barberId,
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

    /**
     * Obtiene la disponibilidad de un barbero específico con paginación,
     * útil para manejar grandes cantidades de horarios disponibles.
     *
     * Permisos de acceso:
     * - ADMIN: Puede obtener la disponibilidad paginada de cualquier barbero

     * - BARBER: Puede obtener su propia disponibilidad paginada y la de otros barberos para coordinación
     * - CLIENT: Puede obtener la disponibilidad paginada de cualquier barbero para agendar citas
     *
     * @param barberId ID del barbero cuya disponibilidad se desea consultar
     * @param page Número de página para paginación
     * @param size Tamaño de página para paginación
     * @param sortBy Campo por el cual ordenar
     * @param sortDir Dirección de ordenamiento
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con la disponibilidad paginada del barbero
     */
    @Operation(
            summary = "Obtener disponibilidades paginadas de barbero",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede obtener la disponibilidad paginada de cualquier barbero<br/>" +
                         "• <strong>BARBER:</strong> Puede obtener su propia disponibilidad paginada y la de otros barberos para coordinación<br/>" +
                         "• <strong>CLIENT:</strong> Puede obtener la disponibilidad paginada de cualquier barbero para agendar citas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Disponibilidades paginadas obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),

            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/availability/paginated")
    public ResponseEntity<ApiResponseDto<Page<BarberAvailabilityResponseDto>>> getBarberAvailabilityPaginated(
            @Parameter(description = "ID del barbero") @RequestParam String barberId,
            @Parameter(description = "Número de página (empezando desde 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Dirección de ordenamiento (asc o desc)") @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {

        Page<BarberAvailabilityResponseDto> availabilities = 
                availabilityService.getAvailabilitiesByBarberPaginated(barberId, page, size, sortBy, sortDir);

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

    /**
     * Actualiza la disponibilidad de un barbero específico, permitiendo modificar
     * horarios, días disponibles y otros aspectos de la disponibilidad.
     *
     * Permisos de acceso:
     * - ADMIN: Puede actualizar la disponibilidad de cualquier barbero

     * - BARBER: Solo puede actualizar su propia disponibilidad
     * - CLIENT: No tiene permisos para actualizar disponibilidades
     *
     * @param availabilityId ID de la disponibilidad específica a actualizar
     * @param updateDto Datos de actualización de la disponibilidad
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con la disponibilidad actualizada
     */
    @Operation(
            summary = "Actualizar disponibilidad",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede actualizar la disponibilidad de cualquier barbero<br/>" +
                         "• <strong>BARBER:</strong> Solo puede actualizar su propia disponibilidad<br/>" +
                         "• <strong>CLIENT:</strong> No tiene permisos para actualizar disponibilidades",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Disponibilidad actualizada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),

            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/availability")
    public ResponseEntity<ApiResponseDto<BarberAvailabilityResponseDto>> updateAvailability(
            @Parameter(description = "ID de la disponibilidad") @RequestParam String availabilityId,
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

    /**
     * Elimina una disponibilidad específica de un barbero, removiendo completamente
     * el horario disponible del sistema.
     *
     * Permisos de acceso:
     * - ADMIN: Puede eliminar la disponibilidad de cualquier barbero

     * - BARBER: Solo puede eliminar su propia disponibilidad
     * - CLIENT: No tiene permisos para eliminar disponibilidades
     *
     * @param barberId ID del barbero cuya disponibilidad se desea eliminar
     * @param availabilityId ID de la disponibilidad específica a eliminar
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta de confirmación de eliminación
     */
    @Operation(
            summary = "Eliminar disponibilidad",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede eliminar la disponibilidad de cualquier barbero<br/>" +
                         "• <strong>BARBER:</strong> Solo puede eliminar su propia disponibilidad<br/>" +
                         "• <strong>CLIENT:</strong> No tiene permisos para eliminar disponibilidades",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Disponibilidad eliminada exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),

            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/availability")
    public ResponseEntity<ApiResponseDto<Void>> deleteAvailability(
            @Parameter(description = "ID de la disponibilidad") @RequestParam String availabilityId,
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

    /**
     * Alterna el estado de una disponibilidad específica de un barbero,
     * activándola o desactivándola según el parámetro proporcionado.
     *
     * Permisos de acceso:
     * - ADMIN: Puede alternar la disponibilidad de cualquier barbero

     * - BARBER: Solo puede alternar su propia disponibilidad
     * - CLIENT: No tiene permisos para alternar disponibilidades
     *
     * @param availabilityId ID de la disponibilidad específica a alternar
     * @param isAvailable Estado de disponibilidad a establecer
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con la disponibilidad actualizada
     */
    @Operation(
            summary = "Habilitar/Deshabilitar disponibilidad",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede alternar la disponibilidad de cualquier barbero<br/>" +
                         "• <strong>BARBER:</strong> Solo puede alternar su propia disponibilidad<br/>" +
                         "• <strong>CLIENT:</strong> No tiene permisos para alternar disponibilidades",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Estado de disponibilidad actualizado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    ),

            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/availability/toggle")
    public ResponseEntity<ApiResponseDto<BarberAvailabilityResponseDto>> toggleAvailability(
            @Parameter(description = "ID de la disponibilidad") @RequestParam String availabilityId,
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

    /**
     * Busca barberos disponibles en una barbería específica para un día y hora determinados.
     * Útil para encontrar barberos que puedan atender citas en horarios específicos.
     *
     * Permisos de acceso:
     * - ADMIN: Puede buscar barberos disponibles en cualquier barbería

     * - BARBER: Puede buscar barberos disponibles para coordinación
     * - CLIENT: Puede buscar barberos disponibles para agendar citas
     *
     * @param barbershopId ID de la barbería donde buscar barberos disponibles
     * @param dayOfWeek Día de la semana para la búsqueda
     * @param time Hora específica para la búsqueda
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con lista de barberos disponibles en el horario especificado
     */
    @Operation(
            summary = "Buscar barberos disponibles",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede buscar barberos disponibles en cualquier barbería<br/>" +
                         "• <strong>BARBER:</strong> Puede buscar barberos disponibles para coordinación<br/>" +
                         "• <strong>CLIENT:</strong> Puede buscar barberos disponibles para agendar citas",
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

    /**
     * Obtiene todas las disponibilidades para un día específico de la semana,
     * mostrando todos los barberos y sus horarios disponibles para ese día.
     *
     * Permisos de acceso:
     * - ADMIN: Puede obtener disponibilidades de cualquier día del sistema

     * - BARBER: Puede obtener disponibilidades del día para coordinación
     * - CLIENT: Puede obtener disponibilidades del día para agendar citas
     *
     * @param dayOfWeek Día de la semana para consultar disponibilidades
     * @param request Request HTTP para extraer el token de autenticación
     * @return Respuesta con lista de disponibilidades para el día especificado
     */
    @Operation(
            summary = "Obtener disponibilidades por día",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede obtener disponibilidades de cualquier día del sistema<br/>" +
                         "• <strong>BARBER:</strong> Puede obtener disponibilidades del día para coordinación<br/>" +
                         "• <strong>CLIENT:</strong> Puede obtener disponibilidades del día para agendar citas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Disponibilidades del día obtenidas exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/availability/day")
    public ResponseEntity<ApiResponseDto<List<BarberAvailabilityResponseDto>>> getAvailabilitiesByDay(
            @Parameter(description = "Día de la semana") @RequestParam DayOfWeek dayOfWeek,
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