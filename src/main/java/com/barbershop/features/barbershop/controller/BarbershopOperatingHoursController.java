package com.barbershop.features.barbershop.controller;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.features.barbershop.dto.BarbershopOperatingHoursDto;
import com.barbershop.features.barbershop.dto.BarbershopOperatingHoursRequestDto;
import com.barbershop.features.barbershop.service.BarbershopOperatingHoursService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestionar los horarios de operación de las barberías.
 * Proporciona endpoints para crear, actualizar y consultar horarios usando @RequestParam.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/barbershops/operating-hours")
@RequiredArgsConstructor
@Validated
@Tag(name = "Horarios de Operación", description = "Gestión de horarios de operación de barberías")
public class BarbershopOperatingHoursController {

    private final BarbershopOperatingHoursService operatingHoursService;

    /**
     * Crea o actualiza un horario de operación para una barbería usando JSON.
     * Utiliza @RequestBody para recibir los datos como JSON.
     * 
     * @param requestDto DTO con los datos del horario de operación
     * @param request objeto HttpServletRequest para obtener la ruta
     * @return respuesta con el horario creado o actualizado
     */
    @PostMapping
    @Operation(
        summary = "Crear o actualizar horario de operación (usando JSON)",
        description = "Crea un nuevo horario de operación o actualiza uno existente para una barbería en un día específico usando JSON. " +
                     "Si ya existe un horario para el día especificado, se actualiza; si no existe, se crea uno nuevo."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Horario creado o actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos de entrada inválidos",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Barbería no encontrada",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
        )
    })
    public ResponseEntity<ApiResponseDto<BarbershopOperatingHoursDto>> createOrUpdateOperatingHours(
            @Parameter(description = "Datos del horario de operación", required = true)
            @Valid @RequestBody BarbershopOperatingHoursRequestDto requestDto,
            
            HttpServletRequest request) {
        
        log.info("Recibida solicitud JSON para crear/actualizar horario - Barbería: {}, Día: {}, Cerrado: {}", 
                requestDto.getBarbershopId(), requestDto.getDayOfWeek(), requestDto.getIsClosed());
        
        // Validar lógica de negocio personalizada
        if (!requestDto.isValidSchedule()) {
            String errorMessage = requestDto.getValidationErrorMessage();
            log.warn("Validación de horario fallida: {}", errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        // Procesar la solicitud
        BarbershopOperatingHoursDto result = operatingHoursService.createOrUpdateOperatingHours(requestDto);
        
        // Construir respuesta
        ApiResponseDto<BarbershopOperatingHoursDto> response = ApiResponseDto.<BarbershopOperatingHoursDto>builder()
            .status(HttpStatus.OK.value())
            .message("Horario de operación procesado exitosamente")
            .data(result)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        log.info("Horario procesado exitosamente para barbería: {} en día: {}", 
                requestDto.getBarbershopId(), requestDto.getDayOfWeek());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene todos los horarios de operación de una barbería.
     * 
     * @param barbershopId ID de la barbería
     * @param request objeto HttpServletRequest para obtener la ruta
     * @return lista de horarios de operación
     */
    @GetMapping
    @Operation(
        summary = "Obtener horarios de operación de una barbería",
        description = "Obtiene todos los horarios de operación configurados para una barbería específica, " +
                     "ordenados por día de la semana."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Horarios obtenidos exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Barbería no encontrada",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
        )
    })
    public ResponseEntity<ApiResponseDto<List<BarbershopOperatingHoursDto>>> getOperatingHoursByBarbershop(
            @Parameter(description = "ID de la barbería", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam @NotBlank(message = "El ID de la barbería es obligatorio") String barbershopId,
            
            HttpServletRequest request) {
        
        log.info("Obteniendo horarios de operación para barbería: {}", barbershopId);
        
        List<BarbershopOperatingHoursDto> operatingHours = operatingHoursService
            .getOperatingHoursByBarbershop(barbershopId);
        
        ApiResponseDto<List<BarbershopOperatingHoursDto>> response = ApiResponseDto.<List<BarbershopOperatingHoursDto>>builder()
            .status(HttpStatus.OK.value())
            .message(String.format("Se encontraron %d horarios de operación", operatingHours.size()))
            .data(operatingHours)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        log.info("Horarios obtenidos exitosamente: {} registros", operatingHours.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene un horario específico por barbería y día de la semana.
     * 
     * @param barbershopId ID de la barbería
     * @param dayOfWeek día de la semana
     * @param request objeto HttpServletRequest para obtener la ruta
     * @return horario de operación específico
     */
    @GetMapping("/day")
    @Operation(
        summary = "Obtener horario de un día específico",
        description = "Obtiene el horario de operación de una barbería para un día específico de la semana."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Horario obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Horario no encontrado para el día especificado",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
        )
    })
    public ResponseEntity<ApiResponseDto<BarbershopOperatingHoursDto>> getOperatingHoursByDay(
            @Parameter(description = "ID de la barbería", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam @NotBlank(message = "El ID de la barbería es obligatorio") String barbershopId,
            
            @Parameter(description = "Día de la semana", required = true, example = "MONDAY")
            @RequestParam @NotNull(message = "El día de la semana es obligatorio") DayOfWeek dayOfWeek,
            
            HttpServletRequest request) {
        
        log.info("Obteniendo horario para barbería: {} en día: {}", barbershopId, dayOfWeek);
        
        BarbershopOperatingHoursDto operatingHours = operatingHoursService
            .getOperatingHoursByBarbershopAndDay(barbershopId, dayOfWeek);
        
        ApiResponseDto<BarbershopOperatingHoursDto> response = ApiResponseDto.<BarbershopOperatingHoursDto>builder()
            .status(HttpStatus.OK.value())
            .message("Horario obtenido exitosamente")
            .data(operatingHours)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
        
        log.info("Horario obtenido exitosamente para día: {}", dayOfWeek);
        
        return ResponseEntity.ok(response);
    }
}