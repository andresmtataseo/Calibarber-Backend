package com.barbershop.features.appointment.service;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.common.exception.ResourceNotFoundException;
import com.barbershop.common.exception.BusinessLogicException;
import com.barbershop.common.service.EmailService;
import com.barbershop.features.auth.exception.InvalidCredentialsException;
import com.barbershop.features.appointment.dto.AppointmentResponseDto;
import com.barbershop.features.appointment.dto.BarberAvailabilityDto;
import com.barbershop.features.appointment.dto.BarbersAvailabilityResponseDto;
import com.barbershop.features.appointment.dto.request.CreateAppointmentRequestDto;
import com.barbershop.features.appointment.dto.request.UpdateAppointmentRequestDto;
import com.barbershop.features.appointment.mapper.AppointmentMapper;
import com.barbershop.features.appointment.dto.AvailabilityResponseDto;
import com.barbershop.features.appointment.dto.DayAvailabilityDto;
import com.barbershop.features.appointment.dto.DayAvailabilityResponseDto;
import com.barbershop.features.appointment.dto.DayAvailabilitySlotDto;
import com.barbershop.features.appointment.model.Appointment;
import com.barbershop.features.appointment.model.enums.AppointmentStatus;
import com.barbershop.features.appointment.model.enums.AvailabilityStatus;
import com.barbershop.features.appointment.repository.AppointmentRepository;
import com.barbershop.features.auth.security.JwtService;
import com.barbershop.features.barber.repository.BarberRepository;
import com.barbershop.features.barber.repository.BarberAvailabilityRepository;
import com.barbershop.features.barber.model.BarberAvailability;
import com.barbershop.features.barber.model.Barber;
import com.barbershop.features.barber.model.DayOfWeek;
import com.barbershop.features.barbershop.model.BarbershopOperatingHours;
import com.barbershop.features.barbershop.repository.BarbershopRepository;
import com.barbershop.features.barbershop.repository.BarbershopOperatingHoursRepository;
import com.barbershop.features.service.repository.ServiceRepository;
import com.barbershop.features.user.model.User;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final BarbershopRepository barbershopRepository;
    private final BarbershopOperatingHoursRepository operatingHoursRepository;
    private final BarberAvailabilityRepository barberAvailabilityRepository;
    private final EmailService emailService;

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
        
        // Enviar notificación por email al barbero
        try {
            enviarNotificacionEmailBarbero(savedAppointment, request);
        } catch (Exception e) {
            log.warn("Error al enviar notificación de email al barbero para la cita {}: {}", 
                    savedAppointment.getAppointmentId(), e.getMessage());
            // No fallar la creación de la cita por un error de email
        }
        
        log.info("Cita creada exitosamente con ID: {}", savedAppointment.getAppointmentId());
        return ApiResponseDto.<AppointmentResponseDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("Cita creada exitosamente")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Envía notificación por email al barbero cuando se crea una nueva cita
     * @param appointment Cita creada
     * @param request Datos de la solicitud original
     */
    private void enviarNotificacionEmailBarbero(Appointment appointment, CreateAppointmentRequestDto request) {
        try {
            log.debug("Iniciando proceso de envío de notificación para la cita: {}", appointment.getAppointmentId());
            
            // Obtener información del barbero
            Barber barber = barberRepository.findById(request.getBarberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado"));
            
            User barberUser = userRepository.findById(barber.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario del barbero no encontrado"));
            
            // Obtener información del cliente
            User clientUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario cliente no encontrado"));
            
            // Obtener información del servicio
            com.barbershop.features.service.model.Service service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
            
            // Preparar datos para el email
            String emailBarbero = barberUser.getEmail();
            String nombreBarbero = barberUser.getFirstName() + " " + barberUser.getLastName();
            String nombreCliente = clientUser.getFirstName() + " " + clientUser.getLastName();
            String emailCliente = clientUser.getEmail();
            String telefonoCliente = clientUser.getPhoneNumber();
            String fechaCita = appointment.getAppointmentDatetimeStart().toString();
            String nombreServicio = service.getName();
            Integer duracionMinutos = request.getDurationMinutes();
            String precio = request.getPrice().toString();
            String notas = request.getNotes();
            
            log.debug("Datos preparados para envío - Barbero: {}, Cliente: {}, Email barbero: {}", 
                     nombreBarbero, nombreCliente, emailBarbero);
            
            // Enviar el email
            emailService.enviarNotificacionCitaBarbero(
                    emailBarbero, nombreBarbero, nombreCliente, emailCliente, 
                    telefonoCliente, fechaCita, nombreServicio, duracionMinutos, 
                    precio, notas
            );
            
            log.info("Notificación de email enviada exitosamente al barbero {} para la cita {}", 
                    emailBarbero, appointment.getAppointmentId());
            
        } catch (ResourceNotFoundException e) {
            log.error("Error de datos al enviar notificación para la cita {}: {}", 
                    appointment.getAppointmentId(), e.getMessage());
            // No re-lanzar para evitar que falle la creación de la cita por un problema de notificación
            log.warn("La cita se creó correctamente pero no se pudo enviar la notificación al barbero");
        } catch (RuntimeException e) {
            log.error("Error del servicio de correo al enviar notificación para la cita {}: {}", 
                    appointment.getAppointmentId(), e.getMessage());
            // No re-lanzar para evitar que falle la creación de la cita por un problema de notificación
            log.warn("La cita se creó correctamente pero no se pudo enviar la notificación al barbero debido a un problema con el servicio de correo");
        } catch (Exception e) {
            log.error("Error inesperado al enviar notificación de email al barbero para la cita {}: {}", 
                    appointment.getAppointmentId(), e.getMessage(), e);
            // No re-lanzar para evitar que falle la creación de la cita por un problema de notificación
            log.warn("La cita se creó correctamente pero no se pudo enviar la notificación al barbero debido a un error inesperado");
        }
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
        // Solo administradores pueden ver todas las citas
        validateAdminAccess(token);
        
        // Mapear el campo de ordenamiento
        String mappedSortBy = mapSortField(sortBy);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(mappedSortBy).descending() : Sort.by(mappedSortBy).ascending();
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
     * Mapea los nombres de campos del DTO a los nombres de campos de la entidad
     */
    private String mapSortField(String sortBy) {
        switch (sortBy) {
            case "appointmentDateTime":
                return "appointmentDatetimeStart";
            case "appointmentDatetimeStart":
            case "appointmentDatetimeEnd":
            case "status":
            case "clientId":
            case "barberId":
            case "serviceId":
            case "barbershopId":
            case "priceAtBooking":
            case "notes":
            case "createdAt":
            case "updatedAt":
                return sortBy;
            default:
                log.warn("Campo de ordenamiento no válido: {}, usando appointmentDatetimeStart por defecto", sortBy);
                return "appointmentDatetimeStart";
        }
    }

    /**
     * Obtiene citas por cliente
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<AppointmentResponseDto>> getAppointmentsByClient(String clientId, int page, int size, String sortBy, String sortDir, String token) {
        log.info("Obteniendo citas del cliente: {}", clientId);
        
        // Validar autorización
        validateClientAccess(token, clientId);
        
        // Mapear el campo de ordenamiento
        String mappedSortBy = mapSortField(sortBy);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(mappedSortBy).descending() : Sort.by(mappedSortBy).ascending();
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
    public ApiResponseDto<Page<AppointmentResponseDto>> getAppointmentsByBarber(String barberId, String userId, int page, int size, String sortBy, String sortDir, String token) {

        // Determinar el barberId final basado en los parámetros y el rol del usuario
        String finalBarberId = resolveBarberId(barberId, userId, token);
        
        // Validar autorización usando el userId original si se proporcionó
        validateBarberAccess(token, finalBarberId, userId);
        
        // Mapear el campo de ordenamiento
        String mappedSortBy = mapSortField(sortBy);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(mappedSortBy).descending() : Sort.by(mappedSortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Appointment> appointmentsPage = appointmentRepository.findByBarberId(finalBarberId, pageable);
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
        
        // Mapear el campo de ordenamiento
        String mappedSortBy = mapSortField(sortBy);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(mappedSortBy).descending() : Sort.by(mappedSortBy).ascending();
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
    public ApiResponseDto<List<AppointmentResponseDto>> getUpcomingAppointmentsByBarber(String barberId, String userId, String token) {
        log.info("Obteniendo próximas citas del barbero - barberId: {}, userId: {}", barberId, userId);
        
        String finalBarberId = barberId;
        
        // Si no se proporciona barberId pero sí userId, obtener el barberId del usuario
        if (barberId == null && userId != null) {
            Barber barber = barberRepository.findByUserIdAndActive(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado para el usuario: " + userId));
            finalBarberId = barber.getBarberId();
        }
        
        // Validar que se tenga un barberId válido
        if (finalBarberId == null) {
            throw new BusinessLogicException("Se requiere barberId o userId para obtener las citas");
        }
        
        // Validar autorización
        validateBarberAccess(token, finalBarberId);
        
        List<Appointment> appointments = appointmentRepository.findUpcomingByBarberId(finalBarberId, LocalDateTime.now());
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
        String userEmail = jwtService.getUsernameFromToken(token); // Esto devuelve el email
        
        // Obtener el userId real usando el email
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
        String userId = currentUser.getUserId();
        
        // Los clientes solo pueden crear citas para sí mismos
        if ("ROLE_CLIENT".equals(userRole) && !userId.equals(clientId)) {
            throw new AccessDeniedException("Los clientes solo pueden crear citas para sí mismos");
        }
        
        // Administradores y barberos pueden crear citas para cualquier cliente
        if (!"ROLE_ADMIN".equals(userRole) && !"ROLE_BARBER".equals(userRole) && !"ROLE_CLIENT".equals(userRole)) {
            throw new AccessDeniedException("No tienes permisos para crear citas");
        }
    }
    
    private void validateAppointmentAccess(String token, Appointment appointment) {
        String userRole = jwtService.extractRole(token);
        String userEmail = jwtService.getUsernameFromToken(token); // Esto devuelve el email
        
        // Obtener el userId real usando el email
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
        String userId = currentUser.getUserId();
        
        // Administradores pueden ver todas las citas
        if ("ROLE_ADMIN".equals(userRole)) {
            return;
        }
        
        // Clientes solo pueden ver sus propias citas
        if ("ROLE_CLIENT".equals(userRole) && !userId.equals(appointment.getClientId())) {
            throw new AccessDeniedException("No tienes permisos para ver esta cita");
        }
        
        // Barberos solo pueden ver citas asignadas a ellos
        if ("ROLE_BARBER".equals(userRole) && !userId.equals(appointment.getBarberId())) {
            throw new AccessDeniedException("No tienes permisos para ver esta cita");
        }
    }
    
    private void validateAppointmentModificationAccess(String token, Appointment appointment) {
        String userRole = jwtService.extractRole(token);
        String userEmail = jwtService.getUsernameFromToken(token); // Esto devuelve el email
        
        // Obtener el userId real usando el email
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
        String userId = currentUser.getUserId();
        
        log.info("Validando acceso de modificación - userEmail: {}, userId: {}, role: {}", userEmail, userId, userRole);
        
        // Administradores pueden modificar todas las citas
        if ("ROLE_ADMIN".equals(userRole)) {
            return;
        }
        
        // Clientes solo pueden modificar sus propias citas
        if ("ROLE_CLIENT".equals(userRole)) {
            if (!userId.equals(appointment.getClientId())) {
                throw new AccessDeniedException("No tienes permisos para modificar esta cita");
            }
            return;
        }
        
        // Barberos solo pueden modificar citas asignadas a ellos
        if ("ROLE_BARBER".equals(userRole)) {
            // Obtener el barbero asociado al usuario actual del token
            Optional<Barber> currentBarber = barberRepository.findByUserIdAndActive(userId);
            log.info("Búsqueda de barbero por userId del token: {} - Encontrado: {}", userId, currentBarber.isPresent());
            
            if (currentBarber.isEmpty()) {
                log.error("No se encontró barbero activo para userId: {}", userId);
                throw new AccessDeniedException("No se encontró información del barbero para el usuario actual");
            }
            
            String currentBarberId = currentBarber.get().getBarberId();
            
            // Verificar que el barbero está intentando modificar sus propias citas
            if (!currentBarberId.equals(appointment.getBarberId())) {
                log.error("Acceso denegado - barberId del usuario: {} no coincide con barberId de la cita: {}", currentBarberId, appointment.getBarberId());
                throw new AccessDeniedException("No tienes permisos para modificar esta cita");
            }
            return;
        }
        
        throw new AccessDeniedException("No tienes permisos para realizar esta acción");
    }
    
    private void validateClientAccess(String token, String clientId) {
        String userRole = jwtService.extractRole(token);
        String userEmail = jwtService.getUsernameFromToken(token); // Esto devuelve el email
        
        // Obtener el userId real usando el email
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
        String userId = currentUser.getUserId();
        
        // Administradores pueden acceder a cualquier cliente
        if ("ROLE_ADMIN".equals(userRole)) {
            return;
        }
        
        // Clientes solo pueden acceder a sus propios datos
        if ("ROLE_CLIENT".equals(userRole) && !userId.equals(clientId)) {
            throw new AccessDeniedException("No tienes permisos para acceder a las citas de este cliente");
        }
        
        // Barberos no pueden acceder directamente a citas por cliente
        if ("ROLE_BARBER".equals(userRole)) {
            throw new AccessDeniedException("Los barberos no pueden filtrar citas por cliente");
        }
    }
    
    private void validateBarberAccess(String token, String barberId) {
        validateBarberAccess(token, barberId, null);
    }
    
    private void validateBarberAccess(String token, String barberId, String requestUserId) {
        String userRole = jwtService.extractRole(token);
        String userEmail = jwtService.getUsernameFromToken(token); // Esto devuelve el email
        
        // Obtener el userId real usando el email
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
        String userId = currentUser.getUserId();
        
        log.info("Validando acceso de barbero - userEmail: {}, userId: {}, barberId: {}, role: {}, requestUserId: {}", userEmail, userId, barberId, userRole, requestUserId);
        
        // Administradores pueden acceder a cualquier barbero
        if ("ROLE_ADMIN".equals(userRole)) {
            return;
        }
        
        // Barberos solo pueden acceder a sus propias citas
        if ("ROLE_BARBER".equals(userRole)) {
            // Obtener el barbero asociado al usuario actual del token
            Optional<Barber> currentBarber = barberRepository.findByUserIdAndActive(userId);
            log.info("Búsqueda de barbero por userId del token: {} - Encontrado: {}", userId, currentBarber.isPresent());
            
            if (currentBarber.isEmpty()) {
                log.error("No se encontró barbero activo para userId: {}", userId);
                throw new AccessDeniedException("No se encontró información del barbero para el usuario actual");
            }
            
            String currentBarberId = currentBarber.get().getBarberId();
            
            // Si se proporcionó un userId específico en la request, verificar que el barbero asociado a ese userId sea el mismo que el del token
            if (requestUserId != null && !requestUserId.trim().isEmpty()) {
                // Buscar el barbero asociado al requestUserId
                Optional<Barber> requestedBarber = barberRepository.findByUserIdAndActive(requestUserId);
                log.info("Búsqueda de barbero por requestUserId: {} - Encontrado: {}", requestUserId, requestedBarber.isPresent());
                
                if (requestedBarber.isEmpty()) {
                    log.error("No se encontró barbero activo para requestUserId: {}", requestUserId);
                    throw new AccessDeniedException("No se encontró información del barbero para el usuario solicitado");
                }
                
                String requestedBarberId = requestedBarber.get().getBarberId();
                
                // Verificar que el barbero del token coincida con el barbero del requestUserId
                if (!currentBarberId.equals(requestedBarberId)) {
                    log.error("Acceso denegado - barberId del token: {} no coincide con barberId del requestUserId: {}", currentBarberId, requestedBarberId);
                    throw new AccessDeniedException("No tienes permisos para acceder a las citas de otro usuario");
                }
                // Si los barberId coinciden, permitir el acceso
                return;
            }
            
            // Verificar que el barbero está intentando acceder a sus propias citas
            if (!currentBarberId.equals(barberId)) {
                log.error("Acceso denegado - barberId del usuario: {} no coincide con barberId solicitado: {}", currentBarberId, barberId);
                throw new AccessDeniedException("No tienes permisos para acceder a las citas de este barbero");
            }
            return;
        }
        
        // Clientes no pueden acceder directamente a citas por barbero
        if ("ROLE_CLIENT".equals(userRole)) {
            throw new AccessDeniedException("Los clientes no pueden filtrar citas por barbero");
        }
    }
    
    private void validateBarberOrAdminAccess(String token, String barberId) {
        String userRole = jwtService.extractRole(token);
        String userEmail = jwtService.getUsernameFromToken(token); // Esto devuelve el email
        
        // Obtener el userId real usando el email
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
        String userId = currentUser.getUserId();
        
        log.info("Validando acceso de barbero o admin - userEmail: {}, userId: {}, barberId: {}, role: {}", userEmail, userId, barberId, userRole);
        
        // Administradores pueden acceder
        if ("ROLE_ADMIN".equals(userRole)) {
            return;
        }
        
        // Barberos solo pueden acceder a sus propias citas
        if ("ROLE_BARBER".equals(userRole)) {
            // Obtener el barbero asociado al usuario actual del token
            Optional<Barber> currentBarber = barberRepository.findByUserIdAndActive(userId);
            log.info("Búsqueda de barbero por userId del token: {} - Encontrado: {}", userId, currentBarber.isPresent());
            
            if (currentBarber.isEmpty()) {
                log.error("No se encontró barbero activo para userId: {}", userId);
                throw new AccessDeniedException("No se encontró información del barbero para el usuario actual");
            }
            
            String currentBarberId = currentBarber.get().getBarberId();
            
            // Verificar que el barbero está intentando acceder a sus propias citas
            if (!currentBarberId.equals(barberId)) {
                log.error("Acceso denegado - barberId del usuario: {} no coincide con barberId solicitado: {}", currentBarberId, barberId);
                throw new AccessDeniedException("No tienes permisos para realizar esta acción");
            }
            return;
        }
        
        throw new AccessDeniedException("No tienes permisos para realizar esta acción");
    }
    
    private void validateAdminAccess(String token) {
        String userRole = jwtService.extractRole(token);
        
        if (!"ROLE_ADMIN".equals(userRole)) {
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

    /**
     * Obtiene el total de citas del día de hoy
     */
    @Transactional(readOnly = true)
    public Long getTodayAppointmentsCount() {
        log.info("Obteniendo total de citas del día de hoy");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return appointmentRepository.countTodayAppointments(startOfDay, endOfDay);
    }

    /**
     * Obtiene la disponibilidad de la barbería en un rango de fechas
     * Como el sistema maneja una sola barbería, no requiere barbershopId
     * 
     * @param startDate Fecha inicial del rango
     * @param endDate Fecha final del rango
     * @return DTO con la disponibilidad por días
     */
    public AvailabilityResponseDto getBarbershopAvailability(LocalDate startDate, LocalDate endDate) {
        log.info("Calculando disponibilidad desde {} hasta {}", startDate, endDate);
        
        // Validar parámetros
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final");
        }
        
        // Obtener la primera barbería activa (asumiendo que solo hay una)
        List<com.barbershop.features.barbershop.model.Barbershop> activeBarbershops = 
            barbershopRepository.findAllActive();
        
        if (activeBarbershops.isEmpty()) {
            throw new BusinessLogicException("No hay barberías activas en el sistema");
        }
        
        // Tomar la primera barbería (única en el sistema)
        String barbershopId = activeBarbershops.get(0).getBarbershopId();
        
        // Obtener horarios de operación de la barbería
        List<BarbershopOperatingHours> operatingHours = operatingHoursRepository
            .findByBarbershopIdOrderByDayOfWeek(barbershopId);
        
        // Crear mapa de horarios por día de la semana
        Map<java.time.DayOfWeek, BarbershopOperatingHours> operatingHoursMap = operatingHours.stream()
            .collect(Collectors.toMap(
                BarbershopOperatingHours::getDayOfWeek,
                Function.identity()
            ));
        
        // Obtener todos los barberos activos de la barbería
        List<String> barberIds = barberRepository.findByBarbershopIdAndActive(barbershopId)
            .stream()
            .map(barber -> barber.getBarberId())
            .collect(Collectors.toList());
        
        if (barberIds.isEmpty()) {
            log.warn("No se encontraron barberos activos para la barbería {}", barbershopId);
            // Si no hay barberos, todos los días están sin disponibilidad
            return createUnavailableResponse(startDate, endDate);
        }
        
        // Calcular disponibilidad para cada día en el rango
        List<DayAvailabilityDto> availabilityList = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            java.time.DayOfWeek javaDayOfWeek = currentDate.getDayOfWeek();
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(javaDayOfWeek.name());
            BarbershopOperatingHours dayOperatingHours = operatingHoursMap.get(javaDayOfWeek);
            
            AvailabilityStatus status = calculateDayAvailability(
                barbershopId, 
                currentDate, 
                dayOperatingHours, 
                barberIds
            );
            
            availabilityList.add(DayAvailabilityDto.builder()
                .date(currentDate)
                .status(status)
                .build());
            
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("Disponibilidad calculada para {} días", availabilityList.size());
        
        return AvailabilityResponseDto.builder()
            .availability(availabilityList)
            .build();
    }
    
    /**
     * Calcula la disponibilidad para un día específico
     */
    private AvailabilityStatus calculateDayAvailability(
            String barbershopId, 
            LocalDate date, 
            BarbershopOperatingHours operatingHours, 
            List<String> barberIds) {
        
        // Si la fecha es anterior a hoy, marcar como no disponible
        if (date.isBefore(LocalDate.now())) {
            return AvailabilityStatus.SIN_DISPONIBILIDAD;
        }
        
        // Si la barbería está cerrada ese día
        if (operatingHours == null || operatingHours.getIsClosed()) {
            return AvailabilityStatus.SIN_DISPONIBILIDAD;
        }
        
        LocalTime openingTime = operatingHours.getOpeningTime();
        LocalTime closingTime = operatingHours.getClosingTime();
        
        if (openingTime == null || closingTime == null) {
            return AvailabilityStatus.SIN_DISPONIBILIDAD;
        }
        
        // Obtener el día de la semana
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(date.getDayOfWeek().name());
        
        // Filtrar barberos que están disponibles ese día
        List<String> availableBarberIds = getAvailableBarbersForDay(barberIds, dayOfWeek);
        
        if (availableBarberIds.isEmpty()) {
            return AvailabilityStatus.SIN_DISPONIBILIDAD;
        }
        
        // Obtener solo las citas activas (excluyendo canceladas y no presentadas)
        LocalDateTime startOfDay = date.atTime(openingTime);
        LocalDateTime endOfDay = date.atTime(closingTime);
        
        List<Appointment> dayAppointments = appointmentRepository
            .findByBarberIdInAndAppointmentDatetimeStartBetween(
                availableBarberIds, 
                startOfDay, 
                endOfDay
            ).stream()
            .filter(appointment -> 
                appointment.getStatus() != AppointmentStatus.CANCELLED && 
                appointment.getStatus() != AppointmentStatus.NO_SHOW
            )
            .collect(Collectors.toList());
        
        // Calcular tiempo total disponible considerando horarios individuales de barberos
        long totalAvailableMinutes = calculateTotalAvailableMinutes(availableBarberIds, dayOfWeek, openingTime, closingTime);
        
        if (totalAvailableMinutes == 0) {
            return AvailabilityStatus.SIN_DISPONIBILIDAD;
        }
        
        // Calcular tiempo total ocupado
        long totalOccupiedMinutes = calculateOccupiedMinutes(dayAppointments);
        
        // Calcular porcentaje de ocupación
        double occupancyPercentage = (double) totalOccupiedMinutes / totalAvailableMinutes;
        
        // Determinar estado basado en ocupación
        if (occupancyPercentage >= 0.9) { // 90% o más ocupado
            return AvailabilityStatus.SIN_DISPONIBILIDAD;
        } else if (occupancyPercentage >= 0.5) { // 50% o más ocupado
            return AvailabilityStatus.PARCIALMENTE_DISPONIBLE;
        } else {
            return AvailabilityStatus.LIBRE;
        }
    }
    
    /**
     * Obtiene los barberos que están disponibles para un día específico
     */
    private List<String> getAvailableBarbersForDay(List<String> barberIds, DayOfWeek dayOfWeek) {
        return barberIds.stream()
            .filter(barberId -> {
                // Verificar si el barbero está activo
                Optional<Barber> barberOpt = barberRepository.findById(barberId);
                if (barberOpt.isEmpty() || !barberOpt.get().getIsActive()) {
                    return false;
                }
                
                // Verificar si el barbero tiene disponibilidad para ese día
                List<BarberAvailability> availabilities = barberAvailabilityRepository
                    .findByBarberIdAndDayOfWeekAndAvailable(barberId, dayOfWeek);
                
                return !availabilities.isEmpty();
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Calcula el tiempo total disponible considerando horarios individuales de barberos
     */
    private long calculateTotalAvailableMinutes(List<String> barberIds, DayOfWeek dayOfWeek, 
                                               LocalTime barbershopOpen, LocalTime barbershopClose) {
        long totalMinutes = 0;
        
        for (String barberId : barberIds) {
            List<BarberAvailability> availabilities = barberAvailabilityRepository
                .findByBarberIdAndDayOfWeekAndAvailable(barberId, dayOfWeek);
            
            for (BarberAvailability availability : availabilities) {
                // Usar el horario más restrictivo entre barbería y barbero
                LocalTime effectiveStart = availability.getStartTime().isBefore(barbershopOpen) 
                    ? barbershopOpen : availability.getStartTime();
                LocalTime effectiveEnd = availability.getEndTime().isAfter(barbershopClose) 
                    ? barbershopClose : availability.getEndTime();
                
                if (effectiveStart.isBefore(effectiveEnd)) {
                    totalMinutes += Duration.between(effectiveStart, effectiveEnd).toMinutes();
                }
            }
        }
        
        return totalMinutes;
    }
    private long calculateOccupiedMinutes(List<Appointment> appointments) {
        return appointments.stream()
            .mapToLong(appointment -> {
                LocalDateTime start = appointment.getAppointmentDatetimeStart();
                LocalDateTime end = appointment.getAppointmentDatetimeEnd();
                return Duration.between(start, end).toMinutes();
            })
            .sum();
    }
    
    /**
     * Crea una respuesta donde todos los días están sin disponibilidad
     */
    private AvailabilityResponseDto createUnavailableResponse(LocalDate startDate, LocalDate endDate) {
        List<DayAvailabilityDto> availabilityList = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            availabilityList.add(DayAvailabilityDto.builder()
                .date(currentDate)
                .status(AvailabilityStatus.SIN_DISPONIBILIDAD)
                .build());
            
            currentDate = currentDate.plusDays(1);
        }
        
        return AvailabilityResponseDto.builder()
            .availability(availabilityList)
            .build();
    }

    /**
     * Obtiene la disponibilidad detallada de un día en bloques de 30 minutos
     * 
     * @param date Fecha para consultar la disponibilidad
     * @param barbershopId ID de la barbería (se obtiene del primer barbero activo si no se especifica)
     * @return Respuesta con los bloques de 30 minutos y su disponibilidad
     */
    public ApiResponseDto<DayAvailabilityResponseDto> getDayAvailabilityBySlots(LocalDate date, String barbershopId) {
        log.info("Obteniendo disponibilidad por bloques para fecha: {} en barbería: {}", date, barbershopId);
        
        try {
            // Si no se especifica barbershopId, obtener el de la primera barbería activa
            if (barbershopId == null || barbershopId.isEmpty()) {
                barbershopId = getDefaultBarbershopId();
            }
            
            // Obtener horarios de operación de la barbería para el día
            java.time.DayOfWeek javaDayOfWeek = date.getDayOfWeek();
            
            Optional<BarbershopOperatingHours> operatingHoursOpt = operatingHoursRepository
                .findByBarbershop_BarbershopIdAndDayOfWeek(barbershopId, javaDayOfWeek);
            
            if (operatingHoursOpt.isEmpty() || operatingHoursOpt.get().getIsClosed()) {
                log.info("Barbería cerrada el día: {}", javaDayOfWeek);
                return ApiResponseDto.<DayAvailabilityResponseDto>builder()
                    .status(HttpStatus.OK.value())
                    .message("Disponibilidad obtenida exitosamente")
                    .data(DayAvailabilityResponseDto.builder()
                        .date(date)
                        .slots(new ArrayList<>())
                        .build())
                    .build();
            }
            
            BarbershopOperatingHours operatingHours = operatingHoursOpt.get();
            LocalTime openingTime = operatingHours.getOpeningTime();
            LocalTime closingTime = operatingHours.getClosingTime();
            
            if (openingTime == null || closingTime == null) {
                log.warn("Horarios de apertura/cierre no definidos para barbería: {} día: {}", barbershopId, javaDayOfWeek);
                return ApiResponseDto.<DayAvailabilityResponseDto>builder()
                    .status(HttpStatus.OK.value())
                    .message("Disponibilidad obtenida exitosamente")
                    .data(DayAvailabilityResponseDto.builder()
                        .date(date)
                        .slots(new ArrayList<>())
                        .build())
                    .build();
            }
            
            // Obtener todos los barberos activos de la barbería
             List<Barber> activeBarbers = barberRepository.findByBarbershopIdAndActive(barbershopId);
            
            if (activeBarbers.isEmpty()) {
                log.warn("No hay barberos activos en la barbería: {}", barbershopId);
                return ApiResponseDto.<DayAvailabilityResponseDto>builder()
                    .status(HttpStatus.OK.value())
                    .message("Disponibilidad obtenida exitosamente")
                    .data(DayAvailabilityResponseDto.builder()
                        .date(date)
                        .slots(new ArrayList<>())
                        .build())
                    .build();
            }
            
            List<String> barberIds = activeBarbers.stream()
                .map(Barber::getBarberId)
                .collect(Collectors.toList());
            
            // Obtener todas las citas activas del día para todos los barberos
            LocalDateTime startOfDay = date.atTime(openingTime);
            LocalDateTime endOfDay = date.atTime(closingTime);
            
            List<Appointment> dayAppointments = appointmentRepository
                .findByBarberIdInAndAppointmentDatetimeStartBetween(barberIds, startOfDay, endOfDay)
                .stream()
                .filter(appointment -> 
                    appointment.getStatus() != AppointmentStatus.CANCELLED && 
                    appointment.getStatus() != AppointmentStatus.NO_SHOW
                )
                .collect(Collectors.toList());
            
            // Generar bloques de 30 minutos
            List<DayAvailabilitySlotDto> slots = generateTimeSlots(
                openingTime, closingTime, barberIds, javaDayOfWeek, dayAppointments, date
            );
            
            DayAvailabilityResponseDto responseData = DayAvailabilityResponseDto.builder()
                .date(date)
                .slots(slots)
                .build();
            
            log.info("Disponibilidad por bloques generada exitosamente. Total de bloques: {}", slots.size());
            
            return ApiResponseDto.<DayAvailabilityResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("Disponibilidad obtenida exitosamente")
                .data(responseData)
                .build();
                
        } catch (Exception e) {
            log.error("Error al obtener disponibilidad por bloques para fecha: {}", date, e);
            throw new BusinessLogicException("Error al obtener la disponibilidad: " + e.getMessage());
        }
    }
    
    /**
     * Genera los bloques de tiempo de 30 minutos y determina su disponibilidad
     */
    private List<DayAvailabilitySlotDto> generateTimeSlots(
            LocalTime openingTime, 
            LocalTime closingTime, 
            List<String> barberIds, 
            java.time.DayOfWeek dayOfWeek,
            List<Appointment> dayAppointments,
            LocalDate consultedDate) {
        
        List<DayAvailabilitySlotDto> slots = new ArrayList<>();
        LocalTime currentTime = openingTime;
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        
        // Generar bloques de 30 minutos
        while (currentTime.isBefore(closingTime)) {
            LocalTime slotEndTime = currentTime.plusMinutes(30);
            
            // Si el bloque se extiende más allá del horario de cierre, ajustar
            if (slotEndTime.isAfter(closingTime)) {
                slotEndTime = closingTime;
            }
            
            boolean isAvailable = isSlotAvailable(currentTime, slotEndTime, barberIds, dayOfWeek, dayAppointments);
            
            // Si la fecha consultada es hoy, marcar como no disponible los slots anteriores a la hora actual
            if (consultedDate.equals(today) && currentTime.isBefore(now)) {
                isAvailable = false;
            }
            
            slots.add(DayAvailabilitySlotDto.builder()
                .time(currentTime)
                .available(isAvailable)
                .build());
            
            currentTime = currentTime.plusMinutes(30);
        }
        
        return slots;
    }
    
    /**
     * Determina si un bloque de tiempo específico está disponible
     * (al menos un barbero debe estar libre en ese horario)
     */
    private boolean isSlotAvailable(
            LocalTime slotStart, 
            LocalTime slotEnd, 
            List<String> barberIds, 
            java.time.DayOfWeek dayOfWeek,
            List<Appointment> dayAppointments) {
        
        // Verificar cada barbero
        for (String barberId : barberIds) {
            // Convertir java.time.DayOfWeek al enum personalizado
            com.barbershop.features.barber.model.DayOfWeek customDayOfWeek = 
                com.barbershop.features.barber.model.DayOfWeek.fromValue(dayOfWeek.getValue());
            
            // Verificar si el barbero está disponible ese día de la semana
            List<BarberAvailability> barberAvailabilities = barberAvailabilityRepository
                .findByBarberIdAndDayOfWeekAndAvailable(barberId, customDayOfWeek);
            
            if (barberAvailabilities.isEmpty()) {
                continue; // Este barbero no trabaja este día
            }
            
            // Verificar si el barbero está disponible en este horario específico
            boolean barberAvailableInSlot = barberAvailabilities.stream()
                .anyMatch(availability -> 
                    !slotStart.isBefore(availability.getStartTime()) && 
                    !slotEnd.isAfter(availability.getEndTime())
                );
            
            if (!barberAvailableInSlot) {
                continue; // Este barbero no está disponible en este horario
            }
            
            // Verificar si el barbero tiene citas que se solapen con este bloque
            boolean hasConflictingAppointment = dayAppointments.stream()
                .filter(appointment -> appointment.getBarberId().equals(barberId))
                .anyMatch(appointment -> {
                    LocalTime appointmentStart = appointment.getAppointmentDatetimeStart().toLocalTime();
                    LocalTime appointmentEnd = appointment.getAppointmentDatetimeEnd().toLocalTime();
                    
                    // Verificar solapamiento: el bloque se solapa si:
                    // - El inicio del bloque está antes del fin de la cita Y
                    // - El fin del bloque está después del inicio de la cita
                    return slotStart.isBefore(appointmentEnd) && slotEnd.isAfter(appointmentStart);
                });
            
            if (!hasConflictingAppointment) {
                return true; // Al menos un barbero está libre en este bloque
            }
        }
        
        return false; // Ningún barbero está disponible en este bloque
    }
    
    /**
     * Obtiene la disponibilidad de barberos con tiempo libre hasta su próxima cita
     * @param dateTime Fecha y hora para verificar disponibilidad
     * @return Lista de barberos con su disponibilidad y tiempo libre
     */
    public BarbersAvailabilityResponseDto getBarbersAvailabilityWithFreeTime(LocalDateTime dateTime) {
        log.info("Obteniendo disponibilidad de barberos para fecha: {}", dateTime);
        
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();
        
        // Obtener todos los barberos activos
        List<Barber> activeBarbers = barberRepository.findByBarbershopIdAndActiveWithUser(getDefaultBarbershopId());
        
        // Obtener todas las citas del día para optimizar consultas
        List<String> barberIds = activeBarbers.stream()
            .map(Barber::getBarberId)
            .collect(Collectors.toList());
            
        Map<String, List<Appointment>> appointmentsByBarber = appointmentRepository
            .findByBarberIdInAndAppointmentDatetimeStartBetween(
                barberIds,
                date.atStartOfDay(),
                date.atTime(23, 59, 59)
            )
            .stream()
            .filter(appointment -> List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.IN_PROGRESS).contains(appointment.getStatus()))
            .collect(Collectors.groupingBy(Appointment::getBarberId));
        
        // Obtener horarios de operación de la barbería para el día
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(date.getDayOfWeek().name());
        
        List<BarberAvailabilityDto> barberAvailabilities = activeBarbers.stream()
            .map(barber -> calculateBarberAvailability(barber, dateTime, appointmentsByBarber.getOrDefault(barber.getBarberId(), new ArrayList<>()), dayOfWeek))
            .collect(Collectors.toList());
        
        return BarbersAvailabilityResponseDto.builder()
            .dateTime(dateTime)
            .barbers(barberAvailabilities)
            .build();
    }
    
    /**
     * Calcula la disponibilidad de un barbero específico
     */
    private BarberAvailabilityDto calculateBarberAvailability(
            Barber barber, 
            LocalDateTime requestedDateTime, 
            List<Appointment> barberAppointments,
            DayOfWeek dayOfWeek) {
        
        LocalTime requestedTime = requestedDateTime.toLocalTime();
        LocalDate requestedDate = requestedDateTime.toLocalDate();
        
        // PRIMERO: Verificar si el barbero tiene disponibilidad configurada para este día
        List<BarberAvailability> dayAvailabilities = barberAvailabilityRepository
            .findByBarberIdAndDayOfWeekAndAvailable(barber.getBarberId(), dayOfWeek);
        
        if (dayAvailabilities.isEmpty()) {
            log.debug("Barbero {} no tiene disponibilidad configurada para {}", barber.getBarberId(), dayOfWeek);
            return BarberAvailabilityDto.builder()
                .id(barber.getBarberId())
                .name(getBarberName(barber))
                .available(false)
                .freeMinutes(0)
                .build();
        }
        
        // Verificar si el horario solicitado está dentro de algún rango de disponibilidad del barbero
        boolean isWithinAvailableHours = dayAvailabilities.stream()
            .anyMatch(availability -> 
                !requestedTime.isBefore(availability.getStartTime()) && 
                requestedTime.isBefore(availability.getEndTime())
            );
        
        if (!isWithinAvailableHours) {
            log.debug("Horario {} está fuera del rango de disponibilidad del barbero {} para {}", 
                requestedTime, barber.getBarberId(), dayOfWeek);
            return BarberAvailabilityDto.builder()
                .id(barber.getBarberId())
                .name(getBarberName(barber))
                .available(false)
                .freeMinutes(0)
                .build();
        }
        
        // Verificar si el barbero está ocupado en el horario solicitado
        boolean isOccupied = barberAppointments.stream()
            .anyMatch(appointment -> {
                LocalTime appointmentStart = appointment.getAppointmentDatetimeStart().toLocalTime();
                LocalTime appointmentEnd = appointment.getAppointmentDatetimeEnd().toLocalTime();
                return !requestedTime.isBefore(appointmentStart) && requestedTime.isBefore(appointmentEnd);
            });
        
        if (isOccupied) {
            return BarberAvailabilityDto.builder()
                .id(barber.getBarberId())
                .name(getBarberName(barber))
                .available(false)
                .freeMinutes(0)
                .build();
        }
        
        // Buscar la próxima cita del barbero después del horario solicitado
        Optional<Appointment> nextAppointment = barberAppointments.stream()
            .filter(appointment -> appointment.getAppointmentDatetimeStart().toLocalTime().isAfter(requestedTime))
            .min((a1, a2) -> a1.getAppointmentDatetimeStart().compareTo(a2.getAppointmentDatetimeStart()));
        
        int freeMinutes;
        if (nextAppointment.isPresent()) {
            // Calcular tiempo hasta la próxima cita
            LocalTime nextAppointmentTime = nextAppointment.get().getAppointmentDatetimeStart().toLocalTime();
            freeMinutes = (int) Duration.between(requestedTime, nextAppointmentTime).toMinutes();
        } else {
            // No hay más citas, calcular tiempo hasta el cierre de la barbería
            LocalTime closingTime = getBarbershopClosingTime(barber.getBarbershopId(), dayOfWeek);
            if (closingTime != null && requestedTime.isBefore(closingTime)) {
                freeMinutes = (int) Duration.between(requestedTime, closingTime).toMinutes();
            } else {
                // Si no hay horario de cierre definido o ya pasó, asumir hasta medianoche
                freeMinutes = (int) Duration.between(requestedTime, LocalTime.of(23, 59)).toMinutes();
            }
        }
        
        return BarberAvailabilityDto.builder()
            .id(barber.getBarberId())
            .name(getBarberName(barber))
            .available(true)
            .freeMinutes(Math.max(0, freeMinutes))
            .build();
    }
    
    /**
     * Resuelve el barberId basado en los parámetros proporcionados y el rol del usuario
     */
    private String resolveBarberId(String barberId, String userId, String token) {
        log.info("Resolviendo barberId - barberId: {}, userId: {}", barberId, userId);
        
        // Si se proporciona barberId directamente, usarlo
        if (barberId != null && !barberId.trim().isEmpty()) {
            log.info("Usando barberId proporcionado: {}", barberId);
            return barberId;
        }
        
        // Si se proporciona userId, buscar el barbero correspondiente
        if (userId != null && !userId.trim().isEmpty()) {
            log.info("Buscando barbero por userId: {}", userId);
            Optional<Barber> barber = barberRepository.findByUserIdAndActive(userId);
            if (barber.isPresent()) {
                String resolvedBarberId = barber.get().getBarberId();
                log.info("Barbero encontrado por userId - barberId: {}", resolvedBarberId);
                return resolvedBarberId;
            } else {
                log.warn("No se encontró barbero activo para userId: {}", userId);
                throw new InvalidCredentialsException("Usuario no es un barbero activo");
            }
        }
        
        // Si no se proporciona ninguno, extraer del token
        log.info("Extrayendo userId del token para buscar barbero");
        String tokenUserEmail = jwtService.getUsernameFromToken(token); // Esto devuelve el email
        
        // Obtener el userId real usando el email
        User tokenUser = userRepository.findByEmail(tokenUserEmail)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
        String tokenUserId = tokenUser.getUserId();
        
        Optional<Barber> barber = barberRepository.findByUserIdAndActive(tokenUserId);
        if (barber.isPresent()) {
            String resolvedBarberId = barber.get().getBarberId();
            log.info("Barbero encontrado por token userId - barberId: {}", resolvedBarberId);
            return resolvedBarberId;
        } else {
            log.warn("No se encontró barbero activo para token userId: {}", tokenUserId);
            throw new InvalidCredentialsException("Usuario del token no es un barbero activo");
        }
    }

    /**
     * Obtiene el nombre completo del barbero
     */
    private String getBarberName(Barber barber) {
        return userRepository.findById(barber.getUserId())
            .map(user -> String.format("%s %s", 
                user.getFirstName() != null ? user.getFirstName() : "",
                user.getLastName() != null ? user.getLastName() : "").trim())
            .orElse("Barbero " + barber.getBarberId());
    }
    
    /**
     * Obtiene la hora de cierre de la barbería para un día específico
     */
    private LocalTime getBarbershopClosingTime(String barbershopId, DayOfWeek dayOfWeek) {
        // Convertir el enum personalizado a java.time.DayOfWeek
        java.time.DayOfWeek javaDayOfWeek = mapToJavaDayOfWeek(dayOfWeek);
        
        return operatingHoursRepository.findByBarbershop_BarbershopIdAndDayOfWeek(barbershopId, javaDayOfWeek)
            .filter(hours -> !hours.getIsClosed())
            .map(BarbershopOperatingHours::getClosingTime)
            .orElse(LocalTime.of(18, 0)); // Hora por defecto: 18:00
    }

    /**
     * Convierte el enum personalizado DayOfWeek a java.time.DayOfWeek
     */
    private java.time.DayOfWeek mapToJavaDayOfWeek(DayOfWeek customDayOfWeek) {
        switch (customDayOfWeek) {
            case MONDAY: return java.time.DayOfWeek.MONDAY;
            case TUESDAY: return java.time.DayOfWeek.TUESDAY;
            case WEDNESDAY: return java.time.DayOfWeek.WEDNESDAY;
            case THURSDAY: return java.time.DayOfWeek.THURSDAY;
            case FRIDAY: return java.time.DayOfWeek.FRIDAY;
            case SATURDAY: return java.time.DayOfWeek.SATURDAY;
            case SUNDAY: return java.time.DayOfWeek.SUNDAY;
            default: throw new IllegalArgumentException("Día de la semana no válido: " + customDayOfWeek);
        }
    }

    /**
     * Obtiene el ID de la barbería por defecto (primera barbería activa)
     */
    private String getDefaultBarbershopId() {
        return barbershopRepository.findAll().stream()
            .findFirst()
            .map(barbershop -> barbershop.getBarbershopId())
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró ninguna barbería activa"));
    }
}