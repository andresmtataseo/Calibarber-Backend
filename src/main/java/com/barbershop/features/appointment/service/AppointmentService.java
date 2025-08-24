package com.barbershop.features.appointment.service;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.common.exception.ResourceNotFoundException;
import com.barbershop.common.exception.BusinessLogicException;
import com.barbershop.features.appointment.dto.AppointmentResponseDto;
import com.barbershop.features.appointment.dto.request.CreateAppointmentRequestDto;
import com.barbershop.features.appointment.dto.request.UpdateAppointmentRequestDto;
import com.barbershop.features.appointment.mapper.AppointmentMapper;
import com.barbershop.features.appointment.model.Appointment;
import com.barbershop.features.appointment.model.enums.AppointmentStatus;
import com.barbershop.features.appointment.repository.AppointmentRepository;
import com.barbershop.features.auth.security.JwtService;
import com.barbershop.features.barber.repository.BarberRepository;
import com.barbershop.features.service.repository.ServiceRepository;
import com.barbershop.features.user.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;

    /**
     * Crea una nueva cita
     */
    public ApiResponseDto<AppointmentResponseDto> createAppointment(CreateAppointmentRequestDto request, String token) {
        log.info("Creando nueva cita para usuario: {} con barbero: {}", request.getUserId(), request.getBarberId());
        
        // Validar autorización
        validateAppointmentCreationAccess(token, request.getUserId());
        
        // Validar que existan las entidades relacionadas
        validateRelatedEntities(request);
        
        // Validar disponibilidad del barbero
        validateBarberAvailability(request);
        
        Appointment appointment = appointmentMapper.toEntity(request);
        
        // Establecer campos calculados
        appointment.setAppointmentDatetimeEnd(request.getAppointmentDateTime().plusMinutes(request.getDurationMinutes()));
        appointment.setPriceAtBooking(request.getPrice());
        
        // Obtener barbershopId del barbero
        String barbershopId = barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado"))
                .getBarbershopId();
        appointment.setBarbershopId(barbershopId);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        AppointmentResponseDto responseDto = appointmentMapper.toResponseDto(savedAppointment);
        
        log.info("Cita creada exitosamente con ID: {}", savedAppointment.getAppointmentId());
        return ApiResponseDto.<AppointmentResponseDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("Cita creada exitosamente")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene una cita por ID
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<AppointmentResponseDto> getAppointmentById(String appointmentId, String token) {
        log.info("Obteniendo cita por ID: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));
        
        // Validar autorización para ver la cita
        validateAppointmentAccess(token, appointment);
        
        AppointmentResponseDto responseDto = appointmentMapper.toResponseDto(appointment);
        
        return ApiResponseDto.<AppointmentResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("Cita obtenida exitosamente")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene todas las citas con paginación
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<AppointmentResponseDto>> getAllAppointments(int page, int size, String sortBy, String sortDir, String token) {
        log.info("Obteniendo citas - Página: {}, Tamaño: {}, Ordenar por: {}, Dirección: {}", page, size, sortBy, sortDir);
        
        // Solo administradores pueden ver todas las citas
        validateAdminAccess(token);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Appointment> appointmentsPage = appointmentRepository.findAll(pageable);
        Page<AppointmentResponseDto> responsePage = appointmentsPage.map(appointmentMapper::toResponseDto);
        
        return ApiResponseDto.<Page<AppointmentResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Citas obtenidas exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene citas por cliente
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<AppointmentResponseDto>> getAppointmentsByClient(String clientId, int page, int size, String sortBy, String sortDir, String token) {
        log.info("Obteniendo citas del cliente: {}", clientId);
        
        // Validar autorización
        validateClientAccess(token, clientId);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Appointment> appointmentsPage = appointmentRepository.findByClientId(clientId, pageable);
        Page<AppointmentResponseDto> responsePage = appointmentsPage.map(appointmentMapper::toResponseDto);
        
        return ApiResponseDto.<Page<AppointmentResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Citas del cliente obtenidas exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene citas por barbero
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<AppointmentResponseDto>> getAppointmentsByBarber(String barberId, int page, int size, String sortBy, String sortDir, String token) {
        log.info("Obteniendo citas del barbero: {}", barberId);
        
        // Validar autorización
        validateBarberAccess(token, barberId);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Appointment> appointmentsPage = appointmentRepository.findByBarberId(barberId, pageable);
        Page<AppointmentResponseDto> responsePage = appointmentsPage.map(appointmentMapper::toResponseDto);
        
        return ApiResponseDto.<Page<AppointmentResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Citas del barbero obtenidas exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene citas por estado
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<AppointmentResponseDto>> getAppointmentsByStatus(AppointmentStatus status, int page, int size, String sortBy, String sortDir, String token) {
        log.info("Obteniendo citas con estado: {}", status);
        
        // Solo administradores pueden filtrar por estado
        validateAdminAccess(token);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Appointment> appointmentsPage = appointmentRepository.findByStatus(status, pageable);
        Page<AppointmentResponseDto> responsePage = appointmentsPage.map(appointmentMapper::toResponseDto);
        
        return ApiResponseDto.<Page<AppointmentResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Citas filtradas por estado obtenidas exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene próximas citas de un cliente
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AppointmentResponseDto>> getUpcomingAppointmentsByClient(String clientId, String token) {
        log.info("Obteniendo próximas citas del cliente: {}", clientId);
        
        // Validar autorización
        validateClientAccess(token, clientId);
        
        List<Appointment> appointments = appointmentRepository.findUpcomingByClientId(clientId, LocalDateTime.now());
        List<AppointmentResponseDto> responseList = appointmentMapper.toResponseDtoList(appointments);
        
        return ApiResponseDto.<List<AppointmentResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Próximas citas obtenidas exitosamente")
                .data(responseList)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene próximas citas de un barbero
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<List<AppointmentResponseDto>> getUpcomingAppointmentsByBarber(String barberId, String token) {
        log.info("Obteniendo próximas citas del barbero: {}", barberId);
        
        // Validar autorización
        validateBarberAccess(token, barberId);
        
        List<Appointment> appointments = appointmentRepository.findUpcomingByBarberId(barberId, LocalDateTime.now());
        List<AppointmentResponseDto> responseList = appointmentMapper.toResponseDtoList(appointments);
        
        return ApiResponseDto.<List<AppointmentResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Próximas citas obtenidas exitosamente")
                .data(responseList)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Actualiza una cita
     */
    public ApiResponseDto<AppointmentResponseDto> updateAppointment(String appointmentId, UpdateAppointmentRequestDto request, String token) {
        log.info("Actualizando cita con ID: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));
        
        // Validar autorización
        validateAppointmentModificationAccess(token, appointment);
        
        // Validar que la cita se pueda modificar
        validateAppointmentCanBeModified(appointment);
        
        // Si se cambia la fecha/hora, validar disponibilidad
        if (request.getAppointmentDateTime() != null && !request.getAppointmentDateTime().equals(appointment.getAppointmentDatetimeStart())) {
            validateBarberAvailabilityForUpdate(appointment.getBarberId(), appointmentId, request.getAppointmentDateTime(), 
                    request.getDurationMinutes() != null ? request.getDurationMinutes() : 
                    (int) java.time.Duration.between(appointment.getAppointmentDatetimeStart(), appointment.getAppointmentDatetimeEnd()).toMinutes());
        }
        
        appointmentMapper.updateEntity(appointment, request);
        
        // Recalcular fecha de fin si cambió la duración o fecha de inicio
        if (request.getAppointmentDateTime() != null || request.getDurationMinutes() != null) {
            LocalDateTime startTime = request.getAppointmentDateTime() != null ? request.getAppointmentDateTime() : appointment.getAppointmentDatetimeStart();
            int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : 
                    (int) java.time.Duration.between(appointment.getAppointmentDatetimeStart(), appointment.getAppointmentDatetimeEnd()).toMinutes();
            appointment.setAppointmentDatetimeEnd(startTime.plusMinutes(duration));
        }
        
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        AppointmentResponseDto responseDto = appointmentMapper.toResponseDto(updatedAppointment);
        
        log.info("Cita actualizada exitosamente con ID: {}", appointmentId);
        return ApiResponseDto.<AppointmentResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("Cita actualizada exitosamente")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Cancela una cita
     */
    public ApiResponseDto<AppointmentResponseDto> cancelAppointment(String appointmentId, String token) {
        log.info("Cancelando cita con ID: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));
        
        // Validar autorización
        validateAppointmentModificationAccess(token, appointment);
        
        // Validar que la cita se pueda cancelar
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || 
            appointment.getStatus() == AppointmentStatus.CANCELLED ||
            appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BusinessLogicException("No se puede cancelar una cita que ya está " + appointment.getStatus().name().toLowerCase());
        }
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment cancelledAppointment = appointmentRepository.save(appointment);
        
        AppointmentResponseDto responseDto = appointmentMapper.toResponseDto(cancelledAppointment);
        
        log.info("Cita cancelada exitosamente con ID: {}", appointmentId);
        return ApiResponseDto.<AppointmentResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("Cita cancelada exitosamente")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Confirma una cita
     */
    public ApiResponseDto<AppointmentResponseDto> confirmAppointment(String appointmentId, String token) {
        log.info("Confirmando cita con ID: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));
        
        // Validar autorización (solo barberos y administradores pueden confirmar)
        validateBarberOrAdminAccess(token, appointment.getBarberId());
        
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessLogicException("Solo se pueden confirmar citas programadas");
        }
        
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment confirmedAppointment = appointmentRepository.save(appointment);
        
        AppointmentResponseDto responseDto = appointmentMapper.toResponseDto(confirmedAppointment);
        
        log.info("Cita confirmada exitosamente con ID: {}", appointmentId);
        return ApiResponseDto.<AppointmentResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("Cita confirmada exitosamente")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Marca una cita como completada
     */
    public ApiResponseDto<AppointmentResponseDto> completeAppointment(String appointmentId, String token) {
        log.info("Completando cita con ID: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));
        
        // Validar autorización (solo barberos y administradores pueden completar)
        validateBarberOrAdminAccess(token, appointment.getBarberId());
        
        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessLogicException("Solo se pueden completar citas confirmadas o en progreso");
        }
        
        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment completedAppointment = appointmentRepository.save(appointment);
        
        AppointmentResponseDto responseDto = appointmentMapper.toResponseDto(completedAppointment);
        
        log.info("Cita completada exitosamente con ID: {}", appointmentId);
        return ApiResponseDto.<AppointmentResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("Cita completada exitosamente")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Elimina una cita (solo administradores)
     */
    public ApiResponseDto<Void> deleteAppointment(String appointmentId, String token) {
        log.info("Eliminando cita con ID: {}", appointmentId);
        
        // Solo administradores pueden eliminar citas
        validateAdminAccess(token);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));
        
        appointmentRepository.delete(appointment);
        
        log.info("Cita eliminada exitosamente con ID: {}", appointmentId);
        return ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Cita eliminada exitosamente")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Métodos de validación privados
    
    private void validateAppointmentCreationAccess(String token, String clientId) {
        String userRole = jwtService.extractRole(token);
        String userId = jwtService.getUsernameFromToken(token);
        
        // Los clientes solo pueden crear citas para sí mismos
        if ("CLIENT".equals(userRole) && !userId.equals(clientId)) {
            throw new AccessDeniedException("Los clientes solo pueden crear citas para sí mismos");
        }
        
        // Administradores y barberos pueden crear citas para cualquier cliente
        if (!"ADMIN".equals(userRole) && !"BARBER".equals(userRole) && !"CLIENT".equals(userRole)) {
            throw new AccessDeniedException("No tienes permisos para crear citas");
        }
    }
    
    private void validateAppointmentAccess(String token, Appointment appointment) {
        String userRole = jwtService.extractRole(token);
        String userId = jwtService.getUsernameFromToken(token);
        
        // Administradores pueden ver todas las citas
        if ("ADMIN".equals(userRole)) {
            return;
        }
        
        // Clientes solo pueden ver sus propias citas
        if ("CLIENT".equals(userRole) && !userId.equals(appointment.getClientId())) {
            throw new AccessDeniedException("No tienes permisos para ver esta cita");
        }
        
        // Barberos solo pueden ver citas asignadas a ellos
        if ("BARBER".equals(userRole) && !userId.equals(appointment.getBarberId())) {
            throw new AccessDeniedException("No tienes permisos para ver esta cita");
        }
    }
    
    private void validateAppointmentModificationAccess(String token, Appointment appointment) {
        String userRole = jwtService.extractRole(token);
        String userId = jwtService.getUsernameFromToken(token);
        
        // Administradores pueden modificar todas las citas
        if ("ADMIN".equals(userRole)) {
            return;
        }
        
        // Clientes solo pueden modificar sus propias citas
        if ("CLIENT".equals(userRole) && !userId.equals(appointment.getClientId())) {
            throw new AccessDeniedException("No tienes permisos para modificar esta cita");
        }
        
        // Barberos solo pueden modificar citas asignadas a ellos
        if ("BARBER".equals(userRole) && !userId.equals(appointment.getBarberId())) {
            throw new AccessDeniedException("No tienes permisos para modificar esta cita");
        }
    }
    
    private void validateClientAccess(String token, String clientId) {
        String userRole = jwtService.extractRole(token);
        String userId = jwtService.getUsernameFromToken(token);
        
        // Administradores pueden acceder a cualquier cliente
        if ("ADMIN".equals(userRole)) {
            return;
        }
        
        // Clientes solo pueden acceder a sus propios datos
        if ("CLIENT".equals(userRole) && !userId.equals(clientId)) {
            throw new AccessDeniedException("No tienes permisos para acceder a las citas de este cliente");
        }
        
        // Barberos no pueden acceder directamente a citas por cliente
        if ("BARBER".equals(userRole)) {
            throw new AccessDeniedException("Los barberos no pueden filtrar citas por cliente");
        }
    }
    
    private void validateBarberAccess(String token, String barberId) {
        String userRole = jwtService.extractRole(token);
        String userId = jwtService.getUsernameFromToken(token);
        
        // Administradores pueden acceder a cualquier barbero
        if ("ADMIN".equals(userRole)) {
            return;
        }
        
        // Barberos solo pueden acceder a sus propias citas
        if ("BARBER".equals(userRole) && !userId.equals(barberId)) {
            throw new AccessDeniedException("No tienes permisos para acceder a las citas de este barbero");
        }
        
        // Clientes no pueden acceder directamente a citas por barbero
        if ("CLIENT".equals(userRole)) {
            throw new AccessDeniedException("Los clientes no pueden filtrar citas por barbero");
        }
    }
    
    private void validateBarberOrAdminAccess(String token, String barberId) {
        String userRole = jwtService.extractRole(token);
        String userId = jwtService.getUsernameFromToken(token);
        
        // Administradores pueden acceder
        if ("ADMIN".equals(userRole)) {
            return;
        }
        
        // Barberos solo pueden acceder a sus propias citas
        if ("BARBER".equals(userRole) && userId.equals(barberId)) {
            return;
        }
        
        throw new AccessDeniedException("No tienes permisos para realizar esta acción");
    }
    
    private void validateAdminAccess(String token) {
        String userRole = jwtService.extractRole(token);
        
        if (!"ADMIN".equals(userRole)) {
            throw new AccessDeniedException("Solo los administradores pueden realizar esta acción");
        }
    }
    
    private void validateRelatedEntities(CreateAppointmentRequestDto request) {
        // Validar que existe el usuario
        if (!userRepository.existsById(request.getUserId())) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + request.getUserId());
        }
        
        // Validar que existe el barbero
        if (!barberRepository.existsById(request.getBarberId())) {
            throw new ResourceNotFoundException("Barbero no encontrado con ID: " + request.getBarberId());
        }
        
        // Validar que existe el servicio
        if (!serviceRepository.existsById(request.getServiceId())) {
            throw new ResourceNotFoundException("Servicio no encontrado con ID: " + request.getServiceId());
        }
    }
    
    private void validateBarberAvailability(CreateAppointmentRequestDto request) {
        LocalDateTime startTime = request.getAppointmentDateTime();
        LocalDateTime endTime = startTime.plusMinutes(request.getDurationMinutes());
        
        List<Appointment> conflictingAppointments = appointmentRepository.findConflictingAppointments(
                request.getBarberId(), startTime, endTime);
        
        if (!conflictingAppointments.isEmpty()) {
            throw new BusinessLogicException("El barbero no está disponible en el horario solicitado");
        }
    }
    
    private void validateBarberAvailabilityForUpdate(String barberId, String appointmentId, LocalDateTime startTime, int durationMinutes) {
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
        
        List<Appointment> conflictingAppointments = appointmentRepository.findConflictingAppointmentsExcluding(
                barberId, appointmentId, startTime, endTime);
        
        if (!conflictingAppointments.isEmpty()) {
            throw new BusinessLogicException("El barbero no está disponible en el horario solicitado");
        }
    }
    
    private void validateAppointmentCanBeModified(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || 
            appointment.getStatus() == AppointmentStatus.CANCELLED ||
            appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BusinessLogicException("No se puede modificar una cita que está " + appointment.getStatus().name().toLowerCase());
        }
    }
}