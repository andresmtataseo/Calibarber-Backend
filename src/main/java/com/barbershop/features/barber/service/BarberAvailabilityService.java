package com.barbershop.features.barber.service;

import com.barbershop.features.auth.exception.UserNotFoundException;
import com.barbershop.features.barber.dto.BarberAvailabilityResponseDto;
import com.barbershop.features.barber.dto.request.CreateBarberAvailabilityRequestDto;
import com.barbershop.features.barber.mapper.BarberAvailabilityMapper;
import com.barbershop.features.barber.model.BarberAvailability;
import com.barbershop.features.barber.model.DayOfWeek;
import com.barbershop.features.barber.repository.BarberAvailabilityRepository;
import com.barbershop.features.barber.repository.BarberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BarberAvailabilityService {

    private final BarberAvailabilityRepository availabilityRepository;
    private final BarberRepository barberRepository;
    private final BarberAvailabilityMapper availabilityMapper;

    /**
     * Crea una nueva disponibilidad para un barbero
     */
    public BarberAvailabilityResponseDto createAvailability(CreateBarberAvailabilityRequestDto createDto) {
        log.info("Creando disponibilidad para barbero: {} en día: {}", 
                createDto.getBarberId(), createDto.getDayOfWeek());
        
        // Validar que el barbero existe y está activo
        if (!barberRepository.findByIdAndActive(createDto.getBarberId()).isPresent()) {
            throw new UserNotFoundException("Barbero no encontrado con ID: " + createDto.getBarberId());
        }
        
        // Verificar autorización
        if (!canModifyBarberAvailability(createDto.getBarberId())) {
            throw new AccessDeniedException("No tienes permisos para modificar la disponibilidad de este barbero");
        }
        
        // Validar horarios
        validateTimeRange(createDto.getStartTime(), createDto.getEndTime());
        
        // Verificar solapamientos
        List<BarberAvailability> overlapping = availabilityRepository.findOverlappingAvailabilityForNew(
                createDto.getBarberId(),
                createDto.getDayOfWeek(),
                createDto.getStartTime(),
                createDto.getEndTime()
        );
        
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Ya existe disponibilidad en ese horario para el barbero");
        }
        
        // Crear entidad
        BarberAvailability availability = availabilityMapper.toEntity(createDto);
        availability.setIsAvailable(true);
        availability.setCreatedAt(LocalDateTime.now());
        availability.setUpdatedAt(LocalDateTime.now());
        
        BarberAvailability savedAvailability = availabilityRepository.save(availability);
        log.info("Disponibilidad creada exitosamente con ID: {}", savedAvailability.getBarberAvailabilityId());
        
        return availabilityMapper.toResponseDto(savedAvailability);
    }

    /**
     * Obtiene todas las disponibilidades de un barbero
     */
    @Transactional(readOnly = true)
    public List<BarberAvailabilityResponseDto> getAvailabilitiesByBarber(String barberId) {
        log.info("Obteniendo disponibilidades del barbero: {}", barberId);
        
        // Verificar que el barbero existe
        if (!barberRepository.findByIdAndActive(barberId).isPresent()) {
            throw new UserNotFoundException("Barbero no encontrado con ID: " + barberId);
        }
        
        List<BarberAvailability> availabilities = availabilityRepository.findByBarberIdAndAvailable(barberId);
        return availabilityMapper.toResponseDtoList(availabilities);
    }

    /**
     * Obtiene disponibilidades de un barbero por día
     */
    @Transactional(readOnly = true)
    public List<BarberAvailabilityResponseDto> getAvailabilitiesByBarberAndDay(String barberId, DayOfWeek dayOfWeek) {
        log.info("Obteniendo disponibilidades del barbero: {} para el día: {}", barberId, dayOfWeek);
        
        // Verificar que el barbero existe
        if (!barberRepository.findByIdAndActive(barberId).isPresent()) {
            throw new UserNotFoundException("Barbero no encontrado con ID: " + barberId);
        }
        
        List<BarberAvailability> availabilities = availabilityRepository
                .findByBarberIdAndDayOfWeekAndAvailable(barberId, dayOfWeek);
        return availabilityMapper.toResponseDtoList(availabilities);
    }

    /**
     * Obtiene todas las disponibilidades paginadas de un barbero
     */
    @Transactional(readOnly = true)
    public Page<BarberAvailabilityResponseDto> getAvailabilitiesByBarberPaginated(String barberId, Pageable pageable) {
        log.info("Obteniendo disponibilidades paginadas del barbero: {}", barberId);
        
        // Verificar que el barbero existe
        if (!barberRepository.findByIdAndActive(barberId).isPresent()) {
            throw new UserNotFoundException("Barbero no encontrado con ID: " + barberId);
        }
        
        Page<BarberAvailability> availabilities = availabilityRepository.findByBarberId(barberId, pageable);
        return availabilities.map(availabilityMapper::toResponseDto);
    }

    /**
     * Obtiene disponibilidades por día de la semana
     */
    @Transactional(readOnly = true)
    public List<BarberAvailabilityResponseDto> getAvailabilitiesByDay(DayOfWeek dayOfWeek) {
        log.info("Obteniendo disponibilidades para el día: {}", dayOfWeek);
        
        List<BarberAvailability> availabilities = availabilityRepository.findByDayOfWeekAndAvailable(dayOfWeek);
        return availabilityMapper.toResponseDtoList(availabilities);
    }

    /**
     * Obtiene una disponibilidad por ID
     */
    @Transactional(readOnly = true)
    public BarberAvailabilityResponseDto getAvailabilityById(String availabilityId) {
        log.info("Obteniendo disponibilidad por ID: {}", availabilityId);
        
        BarberAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new UserNotFoundException("Disponibilidad no encontrada con ID: " + availabilityId));
        
        return availabilityMapper.toResponseDto(availability);
    }

    /**
     * Actualiza una disponibilidad existente
     */
    public BarberAvailabilityResponseDto updateAvailability(String availabilityId, CreateBarberAvailabilityRequestDto updateDto) {
        log.info("Actualizando disponibilidad con ID: {}", availabilityId);
        
        BarberAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new UserNotFoundException("Disponibilidad no encontrada con ID: " + availabilityId));
        
        // Verificar autorización
        if (!canModifyBarberAvailability(availability.getBarberId())) {
            throw new AccessDeniedException("No tienes permisos para modificar esta disponibilidad");
        }
        
        // Validar horarios
        validateTimeRange(updateDto.getStartTime(), updateDto.getEndTime());
        
        // Verificar solapamientos (excluyendo el registro actual)
        List<BarberAvailability> overlapping = availabilityRepository.findOverlappingAvailability(
                updateDto.getBarberId(),
                updateDto.getDayOfWeek(),
                updateDto.getStartTime(),
                updateDto.getEndTime(),
                availabilityId
        );
        
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Ya existe disponibilidad en ese horario para el barbero");
        }
        
        // Actualizar campos
        availability.setDayOfWeek(updateDto.getDayOfWeek());
        availability.setStartTime(updateDto.getStartTime());
        availability.setEndTime(updateDto.getEndTime());
        availability.setUpdatedAt(LocalDateTime.now());
        
        BarberAvailability updatedAvailability = availabilityRepository.save(availability);
        log.info("Disponibilidad actualizada exitosamente con ID: {}", updatedAvailability.getBarberAvailabilityId());
        
        return availabilityMapper.toResponseDto(updatedAvailability);
    }

    /**
     * Elimina una disponibilidad
     */
    public void deleteAvailability(String availabilityId) {
        log.info("Eliminando disponibilidad con ID: {}", availabilityId);
        
        BarberAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new UserNotFoundException("Disponibilidad no encontrada con ID: " + availabilityId));
        
        // Verificar autorización
        if (!canModifyBarberAvailability(availability.getBarberId())) {
            throw new AccessDeniedException("No tienes permisos para eliminar esta disponibilidad");
        }
        
        availabilityRepository.delete(availability);
        log.info("Disponibilidad eliminada exitosamente con ID: {}", availabilityId);
    }

    /**
     * Habilita o deshabilita una disponibilidad
     */
    public BarberAvailabilityResponseDto toggleAvailability(String availabilityId, boolean isAvailable) {
        log.info("Cambiando estado de disponibilidad {} a: {}", availabilityId, isAvailable);
        
        BarberAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new UserNotFoundException("Disponibilidad no encontrada con ID: " + availabilityId));
        
        // Verificar autorización
        if (!canModifyBarberAvailability(availability.getBarberId())) {
            throw new AccessDeniedException("No tienes permisos para modificar esta disponibilidad");
        }
        
        availability.setIsAvailable(isAvailable);
        availability.setUpdatedAt(LocalDateTime.now());
        
        BarberAvailability updatedAvailability = availabilityRepository.save(availability);
        log.info("Estado de disponibilidad actualizado exitosamente");
        
        return availabilityMapper.toResponseDto(updatedAvailability);
    }

    /**
     * Habilita o deshabilita todas las disponibilidades de un barbero
     */
    public void toggleBarberAvailability(String barberId, boolean isAvailable) {
        log.info("Cambiando estado de todas las disponibilidades del barbero {} a: {}", barberId, isAvailable);
        
        // Verificar que el barbero existe
        if (!barberRepository.findByIdAndActive(barberId).isPresent()) {
            throw new UserNotFoundException("Barbero no encontrado con ID: " + barberId);
        }
        
        // Verificar autorización
        if (!canModifyBarberAvailability(barberId)) {
            throw new AccessDeniedException("No tienes permisos para modificar las disponibilidades de este barbero");
        }
        
        availabilityRepository.updateAvailabilityByBarberId(barberId, isAvailable, LocalDateTime.now());
        log.info("Estado de disponibilidades del barbero actualizado exitosamente");
    }

    /**
     * Habilita o deshabilita disponibilidades de un barbero para un día específico
     */
    public void toggleBarberAvailabilityByDay(String barberId, DayOfWeek dayOfWeek, boolean isAvailable) {
        log.info("Cambiando estado de disponibilidades del barbero {} para el día {} a: {}", 
                barberId, dayOfWeek, isAvailable);
        
        // Verificar que el barbero existe
        if (!barberRepository.findByIdAndActive(barberId).isPresent()) {
            throw new UserNotFoundException("Barbero no encontrado con ID: " + barberId);
        }
        
        // Verificar autorización
        if (!canModifyBarberAvailability(barberId)) {
            throw new AccessDeniedException("No tienes permisos para modificar las disponibilidades de este barbero");
        }
        
        availabilityRepository.updateAvailabilityByBarberIdAndDayOfWeek(
                barberId, dayOfWeek, isAvailable, LocalDateTime.now());
        log.info("Estado de disponibilidades del barbero para el día actualizado exitosamente");
    }

    /**
     * Busca barberos disponibles en un horario específico
     */
    @Transactional(readOnly = true)
    public List<BarberAvailabilityResponseDto> findAvailableBarbersAtTime(
            String barbershopId, DayOfWeek dayOfWeek, LocalTime time) {
        log.info("Buscando barberos disponibles en barbería {} para día {} a las {}", 
                barbershopId, dayOfWeek, time);
        
        List<BarberAvailability> availabilities = availabilityRepository
                .findAvailableBarbersAtTime(barbershopId, dayOfWeek, time);
        return availabilityMapper.toResponseDtoList(availabilities);
    }

    /**
     * Obtiene el conteo de disponibilidades de un barbero
     */
    @Transactional(readOnly = true)
    public long getAvailabilityCountByBarber(String barberId) {
        return availabilityRepository.countByBarberIdAndAvailable(barberId);
    }

    /**
     * Verifica si un barbero tiene disponibilidad en un día específico
     */
    @Transactional(readOnly = true)
    public boolean hasAvailabilityOnDay(String barberId, DayOfWeek dayOfWeek) {
        return availabilityRepository.existsByBarberIdAndDayOfWeekAndAvailable(barberId, dayOfWeek);
    }

    // Métodos de validación y autorización
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Los horarios de inicio y fin son obligatorios");
        }
        
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("El horario de inicio debe ser anterior al horario de fin");
        }
        
        // Validar que los horarios estén dentro de un rango razonable (ej: 6:00 AM - 10:00 PM)
        LocalTime minTime = LocalTime.of(6, 0);
        LocalTime maxTime = LocalTime.of(22, 0);
        
        if (startTime.isBefore(minTime) || endTime.isAfter(maxTime)) {
            throw new IllegalArgumentException("Los horarios deben estar entre las 06:00 y las 22:00");
        }
    }

    private boolean canModifyBarberAvailability(String barberId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Los administradores pueden modificar cualquier disponibilidad
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }
        
        // Los barberos pueden modificar su propia disponibilidad
        if (hasRole(authentication, "ROLE_BARBER")) {
            String currentUserId = authentication.getName();
            return barberRepository.findByUserIdAndActive(currentUserId)
                    .map(barber -> barber.getBarberId().equals(barberId))
                    .orElse(false);
        }
        
        // Los propietarios pueden modificar disponibilidades de barberos de sus barberías
        if (hasRole(authentication, "ROLE_BARBERSHOP_OWNER")) {
            String currentUserId = authentication.getName();
            // Aquí se necesitaría lógica adicional para verificar la propiedad de la barbería
            return true; // Simplificado por ahora
        }
        
        return false;
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role));
    }
}