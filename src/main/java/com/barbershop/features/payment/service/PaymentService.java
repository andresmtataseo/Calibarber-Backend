package com.barbershop.features.payment.service;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.common.exception.ResourceNotFoundException;
import com.barbershop.common.exception.BusinessLogicException;
import com.barbershop.common.exception.ResourceAlreadyExistsException;
import com.barbershop.features.payment.dto.PaymentResponseDto;
import com.barbershop.features.payment.dto.request.CreatePaymentRequestDto;
import com.barbershop.features.payment.mapper.PaymentMapper;
import com.barbershop.features.payment.model.Payment;
import com.barbershop.features.payment.model.enums.PaymentMethod;
import com.barbershop.features.payment.model.enums.PaymentStatus;
import com.barbershop.features.payment.repository.PaymentRepository;
import com.barbershop.features.appointment.repository.AppointmentRepository;
import com.barbershop.features.appointment.model.Appointment;
import com.barbershop.features.appointment.model.enums.AppointmentStatus;
import com.barbershop.features.auth.security.JwtService;
import com.barbershop.features.user.repository.UserRepository;
import com.barbershop.features.user.model.User;
import com.barbershop.features.user.model.enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final AppointmentRepository appointmentRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    // ========== OPERACIONES CRUD BÁSICAS ==========

    /**
     * Crea un nuevo pago
     */
    public ApiResponseDto<PaymentResponseDto> createPayment(CreatePaymentRequestDto request, String token) {
        log.info("Creando nuevo pago para la cita: {}", request.getAppointmentId());
        
        // Validar autorización
        validatePaymentCreationAccess(token, request.getAppointmentId());
        
        // Validar datos de entrada
        validatePaymentRequest(request);
        
        // Verificar que la cita existe y está en estado válido
        Appointment appointment = validateAppointmentForPayment(request.getAppointmentId());
        
        // Verificar que no existe ya un pago completado para esta cita
        validateNoDuplicatePayment(request.getAppointmentId());
        
        // Crear entidad de pago
        Payment payment = paymentMapper.toEntity(request);
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setAppointment(appointment);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        
        // Si no se especifica fecha de pago y el estado es COMPLETED, establecer fecha actual
        if (payment.getPaymentDate() == null && payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        
        // Guardar pago
        Payment savedPayment = paymentRepository.save(payment);
        
        // Actualizar estado de la cita si el pago está completado
        if (savedPayment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            updateAppointmentStatusAfterPayment(appointment);
        }
        
        PaymentResponseDto responseDto = paymentMapper.toResponseDto(savedPayment);
        
        return ApiResponseDto.<PaymentResponseDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("Pago creado exitosamente")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene un pago por ID
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<PaymentResponseDto> getPaymentById(String paymentId, String token) {
        log.info("Obteniendo pago por ID: {}", paymentId);
        
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + paymentId));
        
        // Validar autorización
        validatePaymentAccess(token, payment);
        
        PaymentResponseDto responseDto = paymentMapper.toResponseDto(payment);
        
        return ApiResponseDto.<PaymentResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("Pago obtenido exitosamente")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene todos los pagos con paginación
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<PaymentResponseDto>> getAllPayments(int page, int size, String sortBy, String sortDir, String token) {
        log.info("Obteniendo todos los pagos - Página: {}, Tamaño: {}", page, size);
        
        // Solo administradores pueden ver todos los pagos
        validateAdminAccess(token);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Payment> paymentsPage = paymentRepository.findAll(pageable);
        Page<PaymentResponseDto> responsePage = paymentsPage.map(paymentMapper::toResponseDto);
        
        return ApiResponseDto.<Page<PaymentResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Pagos obtenidos exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Actualiza el estado de un pago
     */
    public ApiResponseDto<PaymentResponseDto> updatePaymentStatus(String paymentId, PaymentStatus newStatus, String token) {
        log.info("Actualizando estado del pago {} a {}", paymentId, newStatus);
        
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + paymentId));
        
        // Validar autorización
        validatePaymentUpdateAccess(token, payment);
        
        // Validar transición de estado
        validateStatusTransition(payment.getPaymentStatus(), newStatus);
        
        // Actualizar estado
        payment.setPaymentStatus(newStatus);
        payment.setUpdatedAt(LocalDateTime.now());
        
        // Si se marca como completado, establecer fecha de pago
        if (newStatus == PaymentStatus.COMPLETED && payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        
        Payment updatedPayment = paymentRepository.save(payment);
        
        // Actualizar estado de la cita si es necesario
        if (newStatus == PaymentStatus.COMPLETED) {
            updateAppointmentStatusAfterPayment(payment.getAppointment());
        }
        
        PaymentResponseDto responseDto = paymentMapper.toResponseDto(updatedPayment);
        
        return ApiResponseDto.<PaymentResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("Estado del pago actualizado exitosamente")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Elimina un pago (solo si está en estado PENDING o FAILED)
     */
    public ApiResponseDto<Void> deletePayment(String paymentId, String token) {
        log.info("Eliminando pago: {}", paymentId);
        
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + paymentId));
        
        // Validar autorización (solo administradores)
        validateAdminAccess(token);
        
        // Validar que el pago se puede eliminar
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED || payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new BusinessLogicException("No se puede eliminar un pago completado o reembolsado");
        }
        
        paymentRepository.delete(payment);
        
        return ApiResponseDto.<Void>builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message("Pago eliminado exitosamente")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ========== CONSULTAS ESPECÍFICAS ==========

    /**
     * Obtiene pagos por cita
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<List<PaymentResponseDto>> getPaymentsByAppointment(String appointmentId, String token) {
        log.info("Obteniendo pagos para la cita: {}", appointmentId);
        
        // Verificar que la cita existe
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));
        
        // Validar autorización
        validateAppointmentAccess(token, appointment);
        
        List<Payment> payments = paymentRepository.findByAppointmentId(appointmentId);
        List<PaymentResponseDto> responseList = paymentMapper.toResponseDtoList(payments);
        
        return ApiResponseDto.<List<PaymentResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Pagos de la cita obtenidos exitosamente")
                .data(responseList)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene pagos por estado
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<PaymentResponseDto>> getPaymentsByStatus(PaymentStatus status, int page, int size, String sortBy, String sortDir, String token) {
        log.info("Obteniendo pagos por estado: {}", status);
        
        // Solo administradores pueden ver pagos por estado
        validateAdminAccess(token);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Payment> paymentsPage = paymentRepository.findByPaymentStatus(status, pageable);
        Page<PaymentResponseDto> responsePage = paymentsPage.map(paymentMapper::toResponseDto);
        
        return ApiResponseDto.<Page<PaymentResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Pagos por estado obtenidos exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene pagos por método de pago
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<PaymentResponseDto>> getPaymentsByMethod(PaymentMethod method, int page, int size, String sortBy, String sortDir, String token) {
        log.info("Obteniendo pagos por método: {}", method);
        
        // Solo administradores pueden ver pagos por método
        validateAdminAccess(token);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Payment> paymentsPage = paymentRepository.findByPaymentMethod(method, pageable);
        Page<PaymentResponseDto> responsePage = paymentsPage.map(paymentMapper::toResponseDto);
        
        return ApiResponseDto.<Page<PaymentResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Pagos por método obtenidos exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene pagos por rango de fechas
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<PaymentResponseDto>> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size, String sortBy, String sortDir, String token) {
        log.info("Obteniendo pagos entre {} y {}", startDate, endDate);
        
        // Solo administradores pueden ver pagos por rango de fechas
        validateAdminAccess(token);
        
        // Validar rango de fechas
        if (startDate.isAfter(endDate)) {
            throw new BusinessLogicException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Payment> paymentsPage = paymentRepository.findByPaymentDateBetween(startDate, endDate, pageable);
        Page<PaymentResponseDto> responsePage = paymentsPage.map(paymentMapper::toResponseDto);
        
        return ApiResponseDto.<Page<PaymentResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Pagos por rango de fechas obtenidos exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene pagos de un cliente
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<PaymentResponseDto>> getPaymentsByClient(String clientId, int page, int size, String sortBy, String sortDir, String token) {
        log.info("Obteniendo pagos del cliente: {}", clientId);
        
        // Validar autorización
        validateClientOrAdminAccess(token, clientId);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Payment> paymentsPage = paymentRepository.findByClientId(clientId, pageable);
        Page<PaymentResponseDto> responsePage = paymentsPage.map(paymentMapper::toResponseDto);
        
        return ApiResponseDto.<Page<PaymentResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Pagos del cliente obtenidos exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene estadísticas de pagos
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<PaymentStatsDto> getPaymentStats(String token) {
        log.info("Obteniendo estadísticas de pagos");
        
        // Solo administradores pueden ver estadísticas
        validateAdminAccess(token);
        
        BigDecimal totalCompleted = paymentRepository.sumAmountByStatus(PaymentStatus.COMPLETED);
        BigDecimal totalPending = paymentRepository.sumAmountByStatus(PaymentStatus.PENDING);
        long countCompleted = paymentRepository.countByPaymentStatus(PaymentStatus.COMPLETED);
        long countPending = paymentRepository.countByPaymentStatus(PaymentStatus.PENDING);
        long countFailed = paymentRepository.countByPaymentStatus(PaymentStatus.FAILED);
        
        PaymentStatsDto stats = PaymentStatsDto.builder()
                .totalCompletedAmount(totalCompleted != null ? totalCompleted : BigDecimal.ZERO)
                .totalPendingAmount(totalPending != null ? totalPending : BigDecimal.ZERO)
                .completedPaymentsCount(countCompleted)
                .pendingPaymentsCount(countPending)
                .failedPaymentsCount(countFailed)
                .build();
        
        return ApiResponseDto.<PaymentStatsDto>builder()
                .status(HttpStatus.OK.value())
                .message("Estadísticas de pagos obtenidas exitosamente")
                .data(stats)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ========== MÉTODOS DE VALIDACIÓN ==========

    private void validatePaymentRequest(CreatePaymentRequestDto request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessLogicException("El monto del pago debe ser mayor a cero");
        }
        
        if (request.getPaymentMethod() == null) {
            throw new BusinessLogicException("El método de pago es obligatorio");
        }
        
        if (request.getAppointmentId() == null || request.getAppointmentId().trim().isEmpty()) {
            throw new BusinessLogicException("El ID de la cita es obligatorio");
        }
    }

    private Appointment validateAppointmentForPayment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));
        
        // Verificar que la cita esté en un estado válido para pago
        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BusinessLogicException("No se puede procesar el pago para una cita cancelada o no presentada");
        }
        
        return appointment;
    }

    private void validateNoDuplicatePayment(String appointmentId) {
        List<Payment> existingPayments = paymentRepository.findByAppointmentId(appointmentId);
        boolean hasCompletedPayment = existingPayments.stream()
                .anyMatch(payment -> payment.getPaymentStatus() == PaymentStatus.COMPLETED);
        
        if (hasCompletedPayment) {
            throw new ResourceAlreadyExistsException("Ya existe un pago completado para esta cita");
        }
    }

    private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        // Definir transiciones válidas
        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == PaymentStatus.COMPLETED || newStatus == PaymentStatus.FAILED || newStatus == PaymentStatus.CANCELLED;
            case COMPLETED -> newStatus == PaymentStatus.REFUNDED;
            case FAILED -> newStatus == PaymentStatus.PENDING || newStatus == PaymentStatus.COMPLETED || newStatus == PaymentStatus.CANCELLED;
            case REFUNDED -> false; // No se puede cambiar desde REFUNDED
            case CANCELLED -> newStatus == PaymentStatus.PENDING; // Solo se puede reactivar
        };
        
        if (!isValidTransition) {
            throw new BusinessLogicException(String.format("Transición de estado inválida: de %s a %s", currentStatus, newStatus));
        }
    }

    private void updateAppointmentStatusAfterPayment(Appointment appointment) {
        // Si la cita está programada y el pago está completado, marcar como confirmada
        if (appointment.getStatus() == AppointmentStatus.SCHEDULED) {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appointment.setUpdatedAt(LocalDateTime.now());
            appointmentRepository.save(appointment);
            log.info("Cita {} marcada como confirmada después del pago", appointment.getAppointmentId());
        }
    }

    // ========== MÉTODOS DE AUTORIZACIÓN ==========

    private void validatePaymentCreationAccess(String token, String appointmentId) {
        String username = jwtService.getUsernameFromToken(token);
        String role = jwtService.extractRole(token);
        
        // Los administradores pueden crear pagos para cualquier cita
        if (RoleEnum.ROLE_ADMIN.name().equals(role)) {
            return;
        }
        
        // Los clientes solo pueden crear pagos para sus propias citas
        if (RoleEnum.ROLE_CLIENT.name().equals(role)) {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
            
            User client = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            if (!appointment.getClientId().equals(client.getUserId())) {
                throw new AccessDeniedException("No tienes permisos para crear pagos para esta cita");
            }
            return;
        }
        
        // Los barberos pueden crear pagos para citas que ellos atienden
        if (RoleEnum.ROLE_BARBER.name().equals(role)) {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
            
            User barber = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            if (!appointment.getBarberId().equals(barber.getUserId())) {
                throw new AccessDeniedException("No tienes permisos para crear pagos para esta cita");
            }
            return;
        }
        
        throw new AccessDeniedException("No tienes permisos para crear pagos");
    }

    private void validatePaymentAccess(String token, Payment payment) {
        String username = jwtService.getUsernameFromToken(token);
        String role = jwtService.extractRole(token);
        
        // Los administradores pueden ver cualquier pago
        if (RoleEnum.ROLE_ADMIN.name().equals(role)) {
            return;
        }
        
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        // Los clientes solo pueden ver pagos de sus propias citas
        if (RoleEnum.ROLE_CLIENT.name().equals(role)) {
            if (!payment.getAppointment().getClientId().equals(user.getUserId())) {
                throw new AccessDeniedException("No tienes permisos para ver este pago");
            }
            return;
        }
        
        // Los barberos pueden ver pagos de citas que ellos atienden
        if (RoleEnum.ROLE_BARBER.name().equals(role)) {
            if (!payment.getAppointment().getBarberId().equals(user.getUserId())) {
                throw new AccessDeniedException("No tienes permisos para ver este pago");
            }
            return;
        }
        
        throw new AccessDeniedException("No tienes permisos para ver este pago");
    }

    private void validatePaymentUpdateAccess(String token, Payment payment) {
        String role = jwtService.extractRole(token);
        
        // Solo administradores y barberos pueden actualizar pagos
        if (!RoleEnum.ROLE_ADMIN.name().equals(role) && !RoleEnum.ROLE_BARBER.name().equals(role)) {
            throw new AccessDeniedException("No tienes permisos para actualizar pagos");
        }
        
        // Los barberos solo pueden actualizar pagos de sus propias citas
        if (RoleEnum.ROLE_BARBER.name().equals(role)) {
            String username = jwtService.getUsernameFromToken(token);
            User barber = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            if (!payment.getAppointment().getBarberId().equals(barber.getUserId())) {
                throw new AccessDeniedException("No tienes permisos para actualizar este pago");
            }
        }
    }

    private void validateAppointmentAccess(String token, Appointment appointment) {
        String username = jwtService.getUsernameFromToken(token);
        String role = jwtService.extractRole(token);
        
        // Los administradores pueden acceder a cualquier cita
        if (RoleEnum.ROLE_ADMIN.name().equals(role)) {
            return;
        }
        
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        // Los clientes solo pueden acceder a sus propias citas
        if (RoleEnum.ROLE_CLIENT.name().equals(role)) {
            if (!appointment.getClientId().equals(user.getUserId())) {
                throw new AccessDeniedException("No tienes permisos para acceder a esta cita");
            }
            return;
        }
        
        // Los barberos pueden acceder a citas que ellos atienden
        if (RoleEnum.ROLE_BARBER.name().equals(role)) {
            if (!appointment.getBarberId().equals(user.getUserId())) {
                throw new AccessDeniedException("No tienes permisos para acceder a esta cita");
            }
            return;
        }
        
        throw new AccessDeniedException("No tienes permisos para acceder a esta cita");
    }

    private void validateAdminAccess(String token) {
        String role = jwtService.extractRole(token);
        if (!RoleEnum.ROLE_ADMIN.name().equals(role)) {
            throw new AccessDeniedException("Se requieren permisos de administrador para esta operación");
        }
    }

    private void validateClientOrAdminAccess(String token, String clientId) {
        String username = jwtService.getUsernameFromToken(token);
        String role = jwtService.extractRole(token);
        
        // Los administradores pueden acceder a cualquier cliente
        if (RoleEnum.ROLE_ADMIN.name().equals(role)) {
            return;
        }
        
        // Los clientes solo pueden acceder a su propia información
        if (RoleEnum.ROLE_CLIENT.name().equals(role)) {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            if (!user.getUserId().equals(clientId)) {
                throw new AccessDeniedException("No tienes permisos para acceder a esta información");
            }
            return;
        }
        
        throw new AccessDeniedException("No tienes permisos para acceder a esta información");
    }

    // ========== DTO PARA ESTADÍSTICAS ==========

    @lombok.Data
    @lombok.Builder
    public static class PaymentStatsDto {
        private BigDecimal totalCompletedAmount;
        private BigDecimal totalPendingAmount;
        private long completedPaymentsCount;
        private long pendingPaymentsCount;
        private long failedPaymentsCount;
    }
}