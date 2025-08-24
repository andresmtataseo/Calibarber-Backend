package com.barbershop.features.payment.controller;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.features.payment.dto.PaymentResponseDto;
import com.barbershop.features.payment.dto.request.CreatePaymentRequestDto;
import com.barbershop.features.payment.model.enums.PaymentMethod;
import com.barbershop.features.payment.model.enums.PaymentStatus;
import com.barbershop.features.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Operaciones relacionadas con la gestión de pagos")
public class PaymentController {

    private final PaymentService paymentService;

    // ========== ENDPOINTS CRUD BÁSICOS ==========

    @Operation(
            summary = "Crear un nuevo pago",
            description = "Crea un nuevo pago para una cita específica. Requiere autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponseDto<PaymentResponseDto>> createPayment(
            @Valid @RequestBody CreatePaymentRequestDto request,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<PaymentResponseDto> response = paymentService.createPayment(request, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(
            summary = "Obtener pago por ID",
            description = "Obtiene los detalles de un pago específico por su ID. Requiere autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/by-id")
    public ResponseEntity<ApiResponseDto<PaymentResponseDto>> getPaymentById(
            @Parameter(description = "ID del pago", required = true)
            @RequestParam String paymentId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<PaymentResponseDto> response = paymentService.getPaymentById(paymentId, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(
            summary = "Obtener todos los pagos",
            description = "Obtiene una lista paginada de todos los pagos. Solo disponible para administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagos obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/all")
    public ResponseEntity<ApiResponseDto<Page<PaymentResponseDto>>> getAllPayments(
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenar", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de ordenamiento", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<Page<PaymentResponseDto>> response = paymentService.getAllPayments(page, size, sortBy, sortDir, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(
            summary = "Actualizar estado de pago",
            description = "Actualiza el estado de un pago específico. Solo disponible para administradores y barberos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del pago actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/status")
    public ResponseEntity<ApiResponseDto<PaymentResponseDto>> updatePaymentStatus(
            @Parameter(description = "ID del pago", required = true)
            @RequestParam String paymentId,
            @Parameter(description = "Nuevo estado del pago", required = true)
            @RequestParam PaymentStatus status,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<PaymentResponseDto> response = paymentService.updatePaymentStatus(paymentId, status, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(
            summary = "Eliminar pago",
            description = "Elimina un pago específico. Solo disponible para administradores y solo si el pago está en estado PENDING o FAILED."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pago eliminado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/by-id")
    public ResponseEntity<ApiResponseDto<Void>> deletePayment(
            @Parameter(description = "ID del pago", required = true)
            @RequestParam String paymentId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<Void> response = paymentService.deletePayment(paymentId, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ========== ENDPOINTS DE CONSULTAS ESPECÍFICAS ==========

    @Operation(
            summary = "Obtener pagos por cita",
            description = "Obtiene todos los pagos asociados a una cita específica. Requiere autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagos de la cita obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/appointment")
    public ResponseEntity<ApiResponseDto<List<PaymentResponseDto>>> getPaymentsByAppointment(
            @Parameter(description = "ID de la cita", required = true)
            @RequestParam String appointmentId,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<List<PaymentResponseDto>> response = paymentService.getPaymentsByAppointment(appointmentId, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(
            summary = "Obtener pagos por estado",
            description = "Obtiene una lista paginada de pagos filtrados por estado. Solo disponible para administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagos por estado obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/status")
    public ResponseEntity<ApiResponseDto<Page<PaymentResponseDto>>> getPaymentsByStatus(
            @Parameter(description = "Estado del pago", required = true)
            @RequestParam PaymentStatus status,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenar", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de ordenamiento", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<Page<PaymentResponseDto>> response = paymentService.getPaymentsByStatus(status, page, size, sortBy, sortDir, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(
            summary = "Obtener pagos por método de pago",
            description = "Obtiene una lista paginada de pagos filtrados por método de pago. Solo disponible para administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagos por método obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/method")
    public ResponseEntity<ApiResponseDto<Page<PaymentResponseDto>>> getPaymentsByMethod(
            @Parameter(description = "Método de pago", required = true)
            @RequestParam PaymentMethod method,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenar", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de ordenamiento", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<Page<PaymentResponseDto>> response = paymentService.getPaymentsByMethod(method, page, size, sortBy, sortDir, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(
            summary = "Obtener pagos por rango de fechas",
            description = "Obtiene una lista paginada de pagos realizados en un rango de fechas específico. Solo disponible para administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagos por rango de fechas obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponseDto<Page<PaymentResponseDto>>> getPaymentsByDateRange(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd'T'HH:mm:ss)", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd'T'HH:mm:ss)", required = true, example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenar", example = "paymentDate")
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @Parameter(description = "Dirección de ordenamiento", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<Page<PaymentResponseDto>> response = paymentService.getPaymentsByDateRange(startDate, endDate, page, size, sortBy, sortDir, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(
            summary = "Obtener pagos por cliente",
            description = "Obtiene una lista paginada de pagos de un cliente específico. Los clientes solo pueden ver sus propios pagos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagos del cliente obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/client")
    public ResponseEntity<ApiResponseDto<Page<PaymentResponseDto>>> getPaymentsByClient(
            @Parameter(description = "ID del cliente", required = true)
            @RequestParam String clientId,
            @Parameter(description = "Número de página (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenar", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de ordenamiento", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<Page<PaymentResponseDto>> response = paymentService.getPaymentsByClient(clientId, page, size, sortBy, sortDir, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ========== ENDPOINTS DE ESTADÍSTICAS ==========

    @Operation(
            summary = "Obtener estadísticas de pagos",
            description = "Obtiene estadísticas generales de pagos incluyendo totales por estado y conteos. Solo disponible para administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estadísticas de pagos obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponseDto<PaymentService.PaymentStatsDto>> getPaymentStats(
            HttpServletRequest httpRequest) {
        
        String token = extractTokenFromRequest(httpRequest);
        ApiResponseDto<PaymentService.PaymentStatsDto> response = paymentService.getPaymentStats(token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Extrae el token JWT del header Authorization
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Token JWT no encontrado en el header Authorization");
    }
}