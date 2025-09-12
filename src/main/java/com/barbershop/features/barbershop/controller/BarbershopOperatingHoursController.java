package com.barbershop.features.barbershop.controller;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.features.barbershop.dto.BarbershopOperatingHoursCreateDto;
import com.barbershop.features.barbershop.dto.BarbershopOperatingHoursDto;
import com.barbershop.features.barbershop.service.BarbershopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Controlador REST para gestionar los horarios de operación de las barberías.
 * Proporciona endpoints para crear, consultar, actualizar y eliminar horarios.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/barbershops/{barbershopId}/operating-hours")
@RequiredArgsConstructor
@Tag(name = "Horarios de Operación", description = "Gestión de horarios de operación de barberías")
public class BarbershopOperatingHoursController {

    private final BarbershopService barbershopService;

    /**
     * Obtiene todos los horarios de operación de una barbería
     */
    @GetMapping
    @Operation(summary = "Obtener horarios de operación", 
               description = "Obtiene todos los horarios de operación de una barbería ordenados por día de la semana")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Horarios obtenidos exitosamente"),
        @ApiResponse(responseCode = "404", description = "Barbería no encontrada")
    })
    public ResponseEntity<ApiResponseDto<List<BarbershopOperatingHoursDto>>> getOperatingHours(
            @Parameter(description = "ID de la barbería", required = true)
            @PathVariable String barbershopId,
            HttpServletRequest request) {
        
        log.info("GET /api/v1/barbershops/{}/operating-hours - Obteniendo horarios", barbershopId);
        
        List<BarbershopOperatingHoursDto> operatingHours = barbershopService
                .getBarbershopOperatingHours(barbershopId);
        
        ApiResponseDto<List<BarbershopOperatingHoursDto>> response = ApiResponseDto.<List<BarbershopOperatingHoursDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Horarios de operación obtenidos exitosamente")
                .data(operatingHours)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el horario de operación para un día específico
     */
    @GetMapping("/day/{dayOfWeek}")
    @Operation(summary = "Obtener horario por día", 
               description = "Obtiene el horario de operación de una barbería para un día específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Horario obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Barbería o horario no encontrado")
    })
    public ResponseEntity<ApiResponseDto<BarbershopOperatingHoursDto>> getOperatingHoursForDay(
            @Parameter(description = "ID de la barbería", required = true)
            @PathVariable String barbershopId,
            @Parameter(description = "Día de la semana", required = true)
            @PathVariable DayOfWeek dayOfWeek,
            HttpServletRequest request) {
        
        log.info("GET /api/v1/barbershops/{}/operating-hours/day/{} - Obteniendo horario", 
                barbershopId, dayOfWeek);
        
        BarbershopOperatingHoursDto operatingHours = barbershopService
                .getBarbershopOperatingHoursForDay(barbershopId, dayOfWeek);
        
        ApiResponseDto<BarbershopOperatingHoursDto> response = ApiResponseDto.<BarbershopOperatingHoursDto>builder()
                .status(HttpStatus.OK.value())
                .message("Horario de operación obtenido exitosamente")
                .data(operatingHours)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Crea o actualiza horarios de operación para una barbería
     */
    @PostMapping
    @Operation(summary = "Crear/actualizar horarios", 
               description = "Crea o actualiza los horarios de operación de una barbería")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Horarios creados/actualizados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de horarios inválidos"),
        @ApiResponse(responseCode = "404", description = "Barbería no encontrada")
    })
    public ResponseEntity<ApiResponseDto<List<BarbershopOperatingHoursDto>>> createOrUpdateOperatingHours(
            @Parameter(description = "ID de la barbería", required = true)
            @PathVariable String barbershopId,
            @Parameter(description = "Lista de horarios de operación", required = true)
            @Valid @RequestBody List<BarbershopOperatingHoursCreateDto> operatingHoursData,
            HttpServletRequest request) {
        
        log.info("POST /api/v1/barbershops/{}/operating-hours - Creando/actualizando {} horarios", 
                barbershopId, operatingHoursData.size());
        
        List<BarbershopOperatingHoursDto> createdHours = barbershopService
                .createOrUpdateBarbershopOperatingHours(barbershopId, operatingHoursData);
        
        ApiResponseDto<List<BarbershopOperatingHoursDto>> response = ApiResponseDto.<List<BarbershopOperatingHoursDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Horarios de operación creados/actualizados exitosamente")
                .data(createdHours)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verifica si una barbería está abierta en un momento específico
     */
    @GetMapping("/check-open")
    @Operation(summary = "Verificar si está abierta", 
               description = "Verifica si una barbería está abierta en un día y hora específicos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificación realizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Barbería no encontrada")
    })
    public ResponseEntity<ApiResponseDto<Boolean>> checkIfOpen(
            @Parameter(description = "ID de la barbería", required = true)
            @PathVariable String barbershopId,
            @Parameter(description = "Día de la semana", required = true)
            @RequestParam DayOfWeek dayOfWeek,
            @Parameter(description = "Hora a verificar (formato HH:mm)", required = true)
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            HttpServletRequest request) {
        
        log.info("GET /api/v1/barbershops/{}/operating-hours/check-open?dayOfWeek={}&time={}", 
                barbershopId, dayOfWeek, time);
        
        boolean isOpen = barbershopService.isBarbershopOpenAt(barbershopId, dayOfWeek, time);
        
        ApiResponseDto<Boolean> response = ApiResponseDto.<Boolean>builder()
                .status(HttpStatus.OK.value())
                .message(isOpen ? "La barbería está abierta" : "La barbería está cerrada")
                .data(isOpen)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina todos los horarios de operación de una barbería
     */
    @DeleteMapping
    @Operation(summary = "Eliminar horarios", 
               description = "Elimina todos los horarios de operación de una barbería")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Horarios eliminados exitosamente"),
        @ApiResponse(responseCode = "404", description = "Barbería no encontrada")
    })
    public ResponseEntity<ApiResponseDto<Void>> deleteOperatingHours(
            @Parameter(description = "ID de la barbería", required = true)
            @PathVariable String barbershopId,
            HttpServletRequest request) {
        
        log.info("DELETE /api/v1/barbershops/{}/operating-hours - Eliminando horarios", barbershopId);
        
        barbershopService.deleteBarbershopOperatingHours(barbershopId);
        
        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Horarios de operación eliminados exitosamente")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.ok(response);
    }
}