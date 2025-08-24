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

    @PostMapping
    @Operation(summary = "Crear un nuevo servicio", description = "Crea un nuevo servicio en una barbería")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Servicio creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un servicio con el mismo nombre en la barbería",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear servicios",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<ServiceResponseDto>> createService(
            @Valid @RequestBody CreateServiceRequestDto request,
            @Parameter(description = "Token de autorización", required = true)
            @RequestHeader("Authorization") String token) {
        
        ApiResponseDto<ServiceResponseDto> response = serviceService.createService(request, token);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{serviceId}")
    @Operation(summary = "Obtener servicio por ID", description = "Obtiene un servicio específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<ServiceResponseDto>> getServiceById(
            @Parameter(description = "ID del servicio", required = true)
            @PathVariable String serviceId) {
        
        ApiResponseDto<ServiceResponseDto> response = serviceService.getServiceById(serviceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los servicios", description = "Obtiene una lista paginada de todos los servicios activos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios obtenidos exitosamente",
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

    @GetMapping("/barbershop/{barbershopId}")
    @Operation(summary = "Obtener servicios por barbería", description = "Obtiene una lista paginada de servicios de una barbería específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Page<ServiceResponseDto>>> getServicesByBarbershop(
            @Parameter(description = "ID de la barbería", required = true)
            @PathVariable String barbershopId,
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

    @GetMapping("/search/name")
    @Operation(summary = "Buscar servicios por nombre", description = "Busca servicios que contengan el nombre especificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios encontrados exitosamente",
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

    @GetMapping("/search/price")
    @Operation(summary = "Buscar servicios por rango de precios", description = "Busca servicios dentro de un rango de precios específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios encontrados exitosamente",
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

    @GetMapping("/search/duration")
    @Operation(summary = "Buscar servicios por rango de duración", description = "Busca servicios dentro de un rango de duración específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios encontrados exitosamente",
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

    @PutMapping("/{serviceId}")
    @Operation(summary = "Actualizar servicio", description = "Actualiza un servicio existente")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un servicio con el mismo nombre en la barbería",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar este servicio",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<ServiceResponseDto>> updateService(
            @Parameter(description = "ID del servicio", required = true)
            @PathVariable String serviceId,
            @Valid @RequestBody UpdateServiceRequestDto request,
            @Parameter(description = "Token de autorización", required = true)
            @RequestHeader("Authorization") String token) {
        
        ApiResponseDto<ServiceResponseDto> response = serviceService.updateService(serviceId, request, token);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{serviceId}")
    @Operation(summary = "Eliminar servicio", description = "Elimina un servicio (soft delete)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio eliminado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar este servicio",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Void>> deleteService(
            @Parameter(description = "ID del servicio", required = true)
            @PathVariable String serviceId,
            @Parameter(description = "Token de autorización", required = true)
            @RequestHeader("Authorization") String token) {
        
        ApiResponseDto<Void> response = serviceService.deleteService(serviceId, token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{serviceId}/restore")
    @Operation(summary = "Restaurar servicio", description = "Restaura un servicio previamente eliminado")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio restaurado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Servicio no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "El servicio ya está activo o ya existe un servicio con el mismo nombre",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para restaurar este servicio",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<ServiceResponseDto>> restoreService(
            @Parameter(description = "ID del servicio", required = true)
            @PathVariable String serviceId,
            @Parameter(description = "Token de autorización", required = true)
            @RequestHeader("Authorization") String token) {
        
        ApiResponseDto<ServiceResponseDto> response = serviceService.restoreService(serviceId, token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deleted")
    @Operation(summary = "Obtener servicios eliminados", description = "Obtiene una lista paginada de servicios eliminados (solo administradores)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios eliminados obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Solo los administradores pueden acceder a esta información",
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
            @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Token de autorización", required = true)
            @RequestHeader("Authorization") String token) {
        
        ApiResponseDto<Page<ServiceResponseDto>> response = serviceService.getDeletedServices(page, size, sortBy, sortDir, token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/deleted/barbershop/{barbershopId}")
    @Operation(summary = "Obtener servicios eliminados por barbería", description = "Obtiene una lista paginada de servicios eliminados de una barbería específica")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicios eliminados obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para acceder a esta información",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Page<ServiceResponseDto>>> getDeletedServicesByBarbershop(
            @Parameter(description = "ID de la barbería", required = true)
            @PathVariable String barbershopId,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Token de autorización", required = true)
            @RequestHeader("Authorization") String token) {
        
        ApiResponseDto<Page<ServiceResponseDto>> response = serviceService.getDeletedServicesByBarbershop(barbershopId, page, size, sortBy, sortDir, token);
        return ResponseEntity.ok(response);
    }
}