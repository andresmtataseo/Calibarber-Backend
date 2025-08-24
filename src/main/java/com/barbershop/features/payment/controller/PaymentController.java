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

    /**
     * Crea un nuevo pago para una cita específica.
     * 
     * Permisos de acceso:
     * - ADMIN: Puede crear pagos para cualquier cita
     * - BARBER: Solo puede crear pagos para sus propias citas
     * - CLIENT: Solo puede crear pagos para sus propias citas
     * 
     * @param request Datos del pago a crear
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con los datos del pago creado
     */
    @Operation(
            summary = "Crear un nuevo pago",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede crear pagos para cualquier cita<br/>" +
                         "• <strong>BARBER:</strong> Solo puede crear pagos para sus propias citas<br/>" +
                         "• <strong>CLIENT:</strong> Solo puede crear pagos para sus propias citas"
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

    /**
     * Obtiene los detalles de un pago específico por su ID.
     * 
     * Permisos de acceso:
     * - ADMIN: Puede ver cualquier pago del sistema
     * - BARBER: Solo puede ver pagos de sus propias citas
     * - CLIENT: Solo puede ver pagos de sus propias citas
     * 
     * @param paymentId ID del pago a consultar
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con los datos del pago solicitado
     */
    @Operation(
            summary = "Obtener pago por ID",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede ver cualquier pago del sistema<br/>" +
                         "• <strong>BARBER:</strong> Solo puede ver pagos de sus propias citas<br/>" +
                         "• <strong>CLIENT:</strong> Solo puede ver pagos de sus propias citas"
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

    /**
     * Obtiene una lista paginada de todos los pagos del sistema.
     * 
     * Permisos de acceso:
     * - ADMIN: Acceso completo a todos los pagos
     * 
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo para ordenar
     * @param sortDir Dirección de ordenamiento
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la lista paginada de pagos
     */
    @Operation(
            summary = "Obtener todos los pagos",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Acceso completo a todos los pagos del sistema"
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

    /**
     * Actualiza el estado de un pago específico.
     * 
     * Permisos de acceso:
     * - ADMIN: Puede actualizar el estado de cualquier pago
     * - BARBER: Solo puede actualizar pagos de sus propias citas
     * 
     * @param paymentId ID del pago a actualizar
     * @param status Nuevo estado del pago
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con los datos del pago actualizado
     */
    @Operation(
            summary = "Actualizar estado de pago",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede actualizar el estado de cualquier pago<br/>" +
                         "• <strong>BARBER:</strong> Solo puede actualizar pagos de sus propias citas"
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

    /**
     * Elimina un pago específico del sistema.
     * 
     * Permisos de acceso:
     * - ADMIN: Puede eliminar pagos (solo si están en estado PENDING o FAILED)
     * 
     * @param paymentId ID del pago a eliminar
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta de confirmación de eliminación
     */
    @Operation(
            summary = "Eliminar pago",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede eliminar pagos (solo si están en estado PENDING o FAILED)"
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

    /**
     * Obtiene todos los pagos asociados a una cita específica.
     * 
     * Permisos de acceso:
     * - ADMIN: Puede ver pagos de cualquier cita
     * - BARBER: Solo puede ver pagos de sus propias citas
     * - CLIENT: Solo puede ver pagos de sus propias citas
     * 
     * @param appointmentId ID de la cita
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la lista de pagos de la cita
     */
    @Operation(
            summary = "Obtener pagos por cita",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede ver pagos de cualquier cita<br/>" +
                         "• <strong>BARBER:</strong> Solo puede ver pagos de sus propias citas<br/>" +
                         "• <strong>CLIENT:</strong> Solo puede ver pagos de sus propias citas"
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

    /**
     * Obtiene una lista paginada de pagos filtrados por estado.
     * 
     * Permisos de acceso:
     * - ADMIN: Acceso completo a pagos por cualquier estado
     * 
     * @param status Estado del pago a filtrar
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo para ordenar
     * @param sortDir Dirección de ordenamiento
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la lista paginada de pagos filtrados por estado
     */
    @Operation(
            summary = "Obtener pagos por estado",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Acceso completo a pagos por cualquier estado"
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

    /**
     * Obtiene una lista paginada de pagos filtrados por método de pago.
     * 
     * Permisos de acceso:
     * - ADMIN: Acceso completo a pagos por cualquier método de pago
     * 
     * @param method Método de pago a filtrar
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo para ordenar
     * @param sortDir Dirección de ordenamiento
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la lista paginada de pagos filtrados por método
     */
    @Operation(
            summary = "Obtener pagos por método de pago",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Acceso completo a pagos por cualquier método de pago"
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

    /**
     * Obtiene una lista paginada de pagos realizados en un rango de fechas específico.
     * 
     * Permisos de acceso:
     * - ADMIN: Acceso completo a pagos en cualquier rango de fechas
     * 
     * @param startDate Fecha de inicio del rango
     * @param endDate Fecha de fin del rango
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo para ordenar
     * @param sortDir Dirección de ordenamiento
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la lista paginada de pagos en el rango de fechas
     */
    @Operation(
            summary = "Obtener pagos por rango de fechas",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Acceso completo a pagos en cualquier rango de fechas"
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

    /**
     * Obtiene una lista paginada de pagos de un cliente específico.
     * 
     * Permisos de acceso:
     * - ADMIN: Puede ver pagos de cualquier cliente
     * - CLIENT: Solo puede ver sus propios pagos
     * 
     * @param clientId ID del cliente
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo para ordenar
     * @param sortDir Dirección de ordenamiento
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con la lista paginada de pagos del cliente
     */
    @Operation(
            summary = "Obtener pagos por cliente",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Puede ver pagos de cualquier cliente<br/>" +
                         "• <strong>CLIENT:</strong> Solo puede ver sus propios pagos"
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

    /**
     * Obtiene estadísticas generales de pagos del sistema.
     * 
     * Permisos de acceso:
     * - ADMIN: Acceso completo a todas las estadísticas de pagos
     * 
     * @param httpRequest Request HTTP para extraer el token de autenticación
     * @return Respuesta con las estadísticas de pagos (totales por estado, conteos, etc.)
     */
    @Operation(
            summary = "Obtener estadísticas de pagos",
            description = "<strong>Permisos:</strong><br/>" +
                         "• <strong>ADMIN:</strong> Acceso completo a todas las estadísticas de pagos"
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