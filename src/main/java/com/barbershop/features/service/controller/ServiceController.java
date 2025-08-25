package com.barbershop.features.service.controller;

import com.barbershop.features.service.dto.ServiceResponseDto;
import com.barbershop.features.service.dto.request.CreateServiceRequestDto;
import com.barbershop.features.service.dto.request.UpdateServiceRequestDto;
import com.barbershop.features.service.service.ServiceService;
import com.barbershop.common.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Tag(name = "Servicios", description = "API para la gestión de servicios de barbería")
public class ServiceController {

    private final ServiceService serviceService;

    /**
     * Crea un nuevo servicio en una barbería específica
     *
     * Permisos de acceso:
     * - ADMIN: Puede crear servicios en cualquier barbería
     * - BARBER: Solo puede crear servicios en su propia barbería
     * - CLIENT: Acceso denegado
     *
     * @param request Datos del servicio a crear
     * @param token Token de autorización para validar permisos
     * @return Respuesta con el servicio creado
     */
    @PostMapping
    @Operation(
            summary = "Crear un nuevo servicio",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede crear servicios en cualquier barbería<br/>" +
                         "• <strong>BARBER:</strong> Solo puede crear servicios en su propia barbería<br/>" +
                         "• <strong>CLIENT:</strong> Acceso denegado"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Servicio creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<ServiceResponseDto>> createService(
            @Valid @RequestBody CreateServiceRequestDto request) {
        
        ApiResponseDto<ServiceResponseDto> response = serviceService.createService(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtiene un servicio específico por su ID
     *
     * Permisos de acceso:
     * - ADMIN: Puede obtener cualquier servicio
     * - BARBER: Puede obtener cualquier servicio
     * - CLIENT: Puede obtener cualquier servicio
     *
     * @param serviceId ID del servicio a obtener
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con los datos del servicio
     */
    @GetMapping("/by-id")
    @Operation(
            summary = "Obtener servicio por ID",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede obtener cualquier servicio<br/>" +
                         "• <strong>BARBER:</strong> Puede obtener cualquier servicio<br/>" +
                         "• <strong>CLIENT:</strong> Puede obtener cualquier servicio"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<ServiceResponseDto>> getServiceById(
            @Parameter(description = "ID del servicio", required = true)
            @RequestParam String serviceId) {
        
        ApiResponseDto<ServiceResponseDto> response = serviceService.getServiceById(serviceId);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene una lista de todos los servicios disponibles
     *
     * Permisos de acceso:
     * - ADMIN: Puede obtener todos los servicios del sistema
     * - BARBER: Puede obtener todos los servicios del sistema
     * - CLIENT: Puede obtener todos los servicios del sistema
     *
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la lista de todos los servicios
     */
    @GetMapping("/all")
    @Operation(
            summary = "Obtener todos los servicios",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede obtener todos los servicios del sistema<br/>" +
                         "• <strong>BARBER:</strong> Puede obtener todos los servicios del sistema<br/>" +
                         "• <strong>CLIENT:</strong> Puede obtener todos los servicios del sistema"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de servicios obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Page<ServiceResponseDto>>> getAllServices(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        ApiResponseDto<Page<ServiceResponseDto>> response = serviceService.getAllServices(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todos los servicios de una barbería específica
     *
     * Permisos de acceso:
     * - ADMIN: Puede obtener servicios de cualquier barbería
     * - BARBER: Puede obtener servicios de cualquier barbería
     * - CLIENT: Puede obtener servicios de cualquier barbería
     *
     * @param barbershopId ID de la barbería
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la lista de servicios de la barbería
     */
    @GetMapping("/by-barbershop")
    @Operation(
            summary = "Obtener servicios por barbería",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede obtener servicios de cualquier barbería<br/>" +
                         "• <strong>BARBER:</strong> Puede obtener servicios de cualquier barbería<br/>" +
                         "• <strong>CLIENT:</strong> Puede obtener servicios de cualquier barbería"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Page<ServiceResponseDto>>> getServicesByBarbershop(
            @Parameter(description = "ID de la barbería", required = true)
            @RequestParam String barbershopId,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        ApiResponseDto<Page<ServiceResponseDto>> response = serviceService.getServicesByBarbershop(barbershopId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca servicios que contengan el nombre especificado
     *
     * Permisos de acceso:
     * - ADMIN: Puede buscar servicios por nombre en todo el sistema
     * - BARBER: Puede buscar servicios por nombre en todo el sistema
     * - CLIENT: Puede buscar servicios por nombre en todo el sistema
     *
     * @param name Nombre o parte del nombre del servicio a buscar
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la lista de servicios que coinciden con el nombre
     */
    @GetMapping("/search/name")
    @Operation(
            summary = "Buscar servicios por nombre",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede buscar servicios por nombre en todo el sistema<br/>" +
                         "• <strong>BARBER:</strong> Puede buscar servicios por nombre en todo el sistema<br/>" +
                         "• <strong>CLIENT:</strong> Puede buscar servicios por nombre en todo el sistema"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios encontrados",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Page<ServiceResponseDto>>> searchServicesByName(
            @Parameter(description = "Nombre a buscar", required = true)
            @RequestParam String name,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        ApiResponseDto<Page<ServiceResponseDto>> response = serviceService.searchServicesByName(name, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca servicios dentro de un rango de precios específico
     *
     * Permisos de acceso:
     * - ADMIN: Puede buscar servicios por rango de precio en todo el sistema
     * - BARBER: Puede buscar servicios por rango de precio en todo el sistema
     * - CLIENT: Puede buscar servicios por rango de precio en todo el sistema
     *
     * @param minPrice Precio mínimo del rango de búsqueda
     * @param maxPrice Precio máximo del rango de búsqueda
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la lista de servicios dentro del rango de precio
     */
    @GetMapping("/search/price")
    @Operation(
            summary = "Buscar servicios por rango de precio",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede buscar servicios por rango de precio en todo el sistema<br/>" +
                         "• <strong>BARBER:</strong> Puede buscar servicios por rango de precio en todo el sistema<br/>" +
                         "• <strong>CLIENT:</strong> Puede buscar servicios por rango de precio en todo el sistema"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios encontrados",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Page<ServiceResponseDto>>> searchServicesByPriceRange(
            @Parameter(description = "Precio mínimo", required = true)
            @RequestParam BigDecimal minPrice,
            @Parameter(description = "Precio máximo", required = true)
            @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "price")
            @RequestParam(defaultValue = "price") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        ApiResponseDto<Page<ServiceResponseDto>> response = serviceService.searchServicesByPriceRange(minPrice, maxPrice, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca servicios dentro de un rango de duración específico
     *
     * Permisos de acceso:
     * - ADMIN: Puede buscar servicios por rango de duración en todo el sistema
     * - BARBER: Puede buscar servicios por rango de duración en todo el sistema
     * - CLIENT: Puede buscar servicios por rango de duración en todo el sistema
     *
     * @param minDuration Duración mínima en minutos del rango de búsqueda
     * @param maxDuration Duración máxima en minutos del rango de búsqueda
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el cual ordenar
     * @param sortDir Dirección del ordenamiento (asc/desc)
     * @return Respuesta con la lista de servicios dentro del rango de duración
     */
    @GetMapping("/search/duration")
    @Operation(
            summary = "Buscar servicios por rango de duración",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede buscar servicios por rango de duración en todo el sistema<br/>" +
                         "• <strong>BARBER:</strong> Puede buscar servicios por rango de duración en todo el sistema<br/>" +
                         "• <strong>CLIENT:</strong> Puede buscar servicios por rango de duración en todo el sistema"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios encontrados",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Page<ServiceResponseDto>>> searchServicesByDurationRange(
            @Parameter(description = "Duración mínima en minutos", required = true)
            @RequestParam Integer minDuration,
            @Parameter(description = "Duración máxima en minutos", required = true)
            @RequestParam Integer maxDuration,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "durationMinutes")
            @RequestParam(defaultValue = "durationMinutes") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        ApiResponseDto<Page<ServiceResponseDto>> response = serviceService.searchServicesByDurationRange(minDuration, maxDuration, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza un servicio existente
     *
     * Permisos de acceso:
     * - ADMIN: Puede actualizar cualquier servicio
     * - BARBER: Solo puede actualizar servicios de su propia barbería
     * - CLIENT: Acceso denegado
     *
     * @param id ID del servicio a actualizar
     * @param request Datos actualizados del servicio
     * @return Respuesta con el servicio actualizado
     */
    @PutMapping("/update")
    @Operation(
            summary = "Actualizar servicio",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede actualizar cualquier servicio<br/>" +
                         "• <strong>BARBER:</strong> Solo puede actualizar servicios de su propia barbería<br/>" +
                         "• <strong>CLIENT:</strong> Acceso denegado"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<ServiceResponseDto>> updateService(
            @Parameter(description = "ID del servicio a actualizar", required = true)
            @RequestParam @NotBlank(message = "El ID del servicio es obligatorio") String id,
            @Valid @RequestBody UpdateServiceRequestDto request) {
        
        ApiResponseDto<ServiceResponseDto> response = serviceService.updateService(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un servicio (soft delete)
     *
     * Permisos de acceso:
     * - ADMIN: Puede eliminar cualquier servicio
     * - BARBER: Solo puede eliminar servicios de su propia barbería
     * - CLIENT: Acceso denegado
     *
     * @param id ID del servicio a eliminar
     * @return Respuesta confirmando la eliminación del servicio
     */
    @DeleteMapping("/delete")
    @Operation(
            summary = "Eliminar servicio",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede eliminar cualquier servicio<br/>" +
                         "• <strong>BARBER:</strong> Solo puede eliminar servicios de su propia barbería<br/>" +
                         "• <strong>CLIENT:</strong> Acceso denegado"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio eliminado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Void>> deleteService(
            @Parameter(description = "ID del servicio a eliminar", required = true)
            @RequestParam @NotBlank(message = "El ID del servicio es obligatorio") String id) {
        
        ApiResponseDto<Void> response = serviceService.deleteService(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Restaura un servicio eliminado
     *
     * Permisos de acceso:
     * - ADMIN: Puede restaurar cualquier servicio eliminado
     * - BARBER: Solo puede restaurar servicios eliminados de su propia barbería
     * - CLIENT: Acceso denegado
     *
     * @param serviceId ID del servicio a restaurar
     * @param token Token de autorización para validar permisos
     * @return Respuesta confirmando la restauración del servicio
     */
    @PostMapping("/restore")
    @Operation(
            summary = "Restaurar servicio",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede restaurar cualquier servicio eliminado<br/>" +
                         "• <strong>BARBER:</strong> Solo puede restaurar servicios eliminados de su propia barbería<br/>" +
                         "• <strong>CLIENT:</strong> Acceso denegado"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio restaurado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<ServiceResponseDto>> restoreService(
            @Parameter(description = "ID del servicio", required = true)
            @RequestParam String serviceId) {
        
        ApiResponseDto<ServiceResponseDto> response = serviceService.restoreService(serviceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deleted")
    @Operation(summary = "Obtener servicios eliminados", description = "Obtiene una lista paginada de todos los servicios eliminados")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios eliminados obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Page<ServiceResponseDto>>> getDeletedServices(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        ApiResponseDto<Page<ServiceResponseDto>> response = serviceService.getDeletedServices(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deleted/by-barbershop")
    @Operation(summary = "Obtener servicios eliminados por barbería", description = "Obtiene una lista paginada de servicios eliminados de una barbería específica")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios eliminados obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Page<ServiceResponseDto>>> getDeletedServicesByBarbershop(
            @Parameter(description = "ID de la barbería", required = true)
            @RequestParam String barbershopId,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        ApiResponseDto<Page<ServiceResponseDto>> response = serviceService.getDeletedServicesByBarbershop(barbershopId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
}