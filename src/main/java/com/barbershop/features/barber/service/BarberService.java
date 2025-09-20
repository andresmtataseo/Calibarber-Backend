package com.barbershop.features.barber.service;

import com.barbershop.features.auth.exception.UserNotFoundException;
import com.barbershop.features.barber.dto.BarberResponseDto;
import com.barbershop.features.barber.dto.request.CreateBarberRequestDto;
import com.barbershop.features.barber.dto.request.UpdateBarberRequestDto;
import com.barbershop.features.barber.mapper.BarberMapper;
import com.barbershop.features.barber.model.Barber;
import com.barbershop.features.barber.repository.BarberRepository;
import com.barbershop.features.user.service.UserService;
import com.barbershop.features.user.model.enums.RoleEnum;
import com.barbershop.features.user.dto.UserUpdateDto;
import com.barbershop.common.exception.BusinessLogicException;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import com.barbershop.shared.util.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BarberService {

    private final BarberRepository barberRepository;
    private final BarberMapper barberMapper;
    private final UserService userService;

    /**
     * Crea un nuevo barbero y actualiza automáticamente el rol del usuario a BARBER
     */
    @Transactional
    public BarberResponseDto createBarber(CreateBarberRequestDto createDto) {
        log.info("Creando nuevo barbero para usuario: {} en barbería: {}", 
                createDto.getUserId(), createDto.getBarbershopId());
        
        // Validar que el usuario existe y está activo
        if (!userService.existsAndActive(createDto.getUserId())) {
            throw new UserNotFoundException("Usuario no encontrado con ID: " + createDto.getUserId());
        }
        
        // Validar que el usuario no sea ya barbero en otra barbería
        if (barberRepository.existsByUserIdAndActive(createDto.getUserId())) {
            throw new IllegalArgumentException("El usuario ya está registrado como barbero");
        }
        
        try {
            // Crear entidad Barber
            Barber barber = barberMapper.toEntity(createDto);
            barber.setIsActive(true);
            barber.setCreatedAt(LocalDateTime.now());
            barber.setUpdatedAt(LocalDateTime.now());
            
            Barber savedBarber = barberRepository.save(barber);
            log.info("Barbero creado exitosamente con ID: {}", savedBarber.getBarberId());
            
            UserUpdateDto userUpdateDto = new UserUpdateDto();
            userUpdateDto.setRole(RoleEnum.ROLE_BARBER);
            userService.updateUser(createDto.getUserId(), userUpdateDto);
            log.info("Rol del usuario {} actualizado automáticamente a BARBER", createDto.getUserId());
            
            return barberMapper.toResponseDto(savedBarber);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMessage().toLowerCase();
            if (errorMessage.contains("barbershop_id") || errorMessage.contains("barbershops")) {
                throw new BusinessLogicException("La barbería especificada no existe o no está disponible");
            } else if (errorMessage.contains("user_id") || errorMessage.contains("users")) {
                throw new BusinessLogicException("El usuario especificado no existe o no está disponible");
            } else {
                throw new BusinessLogicException("Error al crear el barbero: datos inválidos o duplicados");
            }
        }
    }

    /**
     * Obtiene todos los barberos activos paginados
     */
    @Transactional(readOnly = true)
    public Page<BarberResponseDto> getAllBarbers(int page, int size, String sortBy, String sortDir) {
        log.info("Obteniendo barberos paginados: página {}, tamaño {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Barber> barbers = barberRepository.findAllActive(pageable);
        return barbers.map(barberMapper::toResponseDto);
    }

    /**
     * Obtiene barberos por barbería
     */
    @Transactional(readOnly = true)
    public Page<BarberResponseDto> getBarbersByBarbershop(String barbershopId, int page, int size, String sortBy, String sortDir) {
        log.info("Obteniendo barberos de la barbería: {}", barbershopId);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Barber> barbers = barberRepository.findByBarbershopIdAndActive(barbershopId, pageable);
        return barbers.map(barberMapper::toResponseDto);
    }

    /**
     * Obtiene barberos por especialización
     */
    @Transactional(readOnly = true)
    public Page<BarberResponseDto> getBarbersBySpecialization(String specialization, int page, int size, String sortBy, String sortDir) {
        log.info("Obteniendo barberos con especialización: {}", specialization);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Barber> barbers = barberRepository.findBySpecializationContainingIgnoreCaseAndActive(specialization, pageable);
        return barbers.map(barberMapper::toResponseDto);
    }

    /**
     * Obtiene un barbero por ID
     */
    @Transactional(readOnly = true)
    public BarberResponseDto getBarberById(String barberId) {
        log.info("Obteniendo barbero por ID: {}", barberId);
        
        Barber barber = barberRepository.findByIdAndActive(barberId)
                .orElseThrow(() -> new UserNotFoundException("Barbero no encontrado con ID: " + barberId));
        
        // Verificar autorización
        if (!canAccessBarber(barberId)) {
            throw new AccessDeniedException("No tienes permisos para acceder a este barbero");
        }
        
        return barberMapper.toResponseDto(barber);
    }

    /**
     * Obtiene un barbero por ID de usuario
     */
    @Transactional(readOnly = true)
    public BarberResponseDto getBarberByUserId(String userId) {
        log.info("Obteniendo barbero por ID de usuario: {}", userId);
        
        Barber barber = barberRepository.findByUserIdAndActive(userId)
                .orElseThrow(() -> new UserNotFoundException("Barbero no encontrado para el usuario con ID: " + userId));
        
        // Verificar autorización
        if (!canAccessBarber(barber.getBarberId())) {
            throw new AccessDeniedException("No tienes permisos para acceder a este barbero");
        }
        
        return barberMapper.toResponseDto(barber);
    }

    /**
     * Actualiza un barbero existente
     */
    public BarberResponseDto updateBarber(String barberId, UpdateBarberRequestDto updateDto) {
        log.info("Actualizando barbero con ID: {}", barberId);
        
        Barber barber = barberRepository.findByIdAndActive(barberId)
                .orElseThrow(() -> new UserNotFoundException("Barbero no encontrado con ID: " + barberId));
        
        // Verificar autorización
        if (!canModifyBarber(barberId)) {
            throw new AccessDeniedException("No tienes permisos para modificar este barbero");
        }
        
        try {
            // Actualizar campos
            barberMapper.updateEntity(barber, updateDto);
            barber.setUpdatedAt(LocalDateTime.now());
            
            Barber updatedBarber = barberRepository.save(barber);
            log.info("Barbero actualizado exitosamente con ID: {}", updatedBarber.getBarberId());
            
            return barberMapper.toResponseDto(updatedBarber);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMessage().toLowerCase();
            if (errorMessage.contains("barbershop_id") || errorMessage.contains("barbershops")) {
                throw new BusinessLogicException("La barbería especificada no existe o no está disponible");
            } else if (errorMessage.contains("user_id") || errorMessage.contains("users")) {
                throw new BusinessLogicException("El usuario especificado no existe o no está disponible");
            } else {
                throw new BusinessLogicException("Error al actualizar el barbero: datos inválidos o duplicados");
            }
        }
    }

    /**
     * Elimina un barbero (soft delete)
     */
    public void deleteBarber(String barberId) {
        log.info("Eliminando barbero con ID: {}", barberId);
        
        Barber barber = barberRepository.findByIdAndActive(barberId)
                .orElseThrow(() -> new UserNotFoundException("Barbero no encontrado con ID: " + barberId));
        
        // Verificar autorización
        if (!canModifyBarber(barberId)) {
            throw new AccessDeniedException("No tienes permisos para eliminar este barbero");
        }
        
        barberRepository.softDeleteById(barberId, LocalDateTime.now());
        log.info("Barbero eliminado exitosamente con ID: {}", barberId);
    }

    /**
     * Restaura un barbero eliminado
     */
    public BarberResponseDto restoreBarber(String barberId) {
        log.info("Restaurando barbero con ID: {}", barberId);
        
        // Verificar que solo los administradores puedan restaurar
        if (!SecurityUtils.isCurrentUserAdmin()) {
            throw new AccessDeniedException("Solo los administradores pueden restaurar barberos");
        }
        
        Barber barber = barberRepository.findByIdAndInactive(barberId)
                .orElseThrow(() -> new UserNotFoundException("Barbero eliminado no encontrado con ID: " + barberId));
        
        barberRepository.restoreById(barberId, LocalDateTime.now());
        
        Barber restoredBarber = barberRepository.findById(barberId)
                .orElseThrow(() -> new UserNotFoundException("Error al restaurar barbero con ID: " + barberId));
        
        log.info("Barbero restaurado exitosamente con ID: {}", barberId);
        return barberMapper.toResponseDto(restoredBarber);
    }

    /**
     * Obtiene barberos eliminados (solo administradores)
     */
    @Transactional(readOnly = true)
    public Page<BarberResponseDto> getDeletedBarbers(int page, int size, String sortBy, String sortDir) {
        log.info("Obteniendo barberos eliminados");
        
        if (!SecurityUtils.isCurrentUserAdmin()) {
            throw new AccessDeniedException("Solo los administradores pueden ver barberos eliminados");
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Barber> deletedBarbers = barberRepository.findAllInactive(pageable);
        return deletedBarbers.map(barberMapper::toResponseDto);
    }

    /**
     * Obtiene el conteo de barberos por barbería
     */
    @Transactional(readOnly = true)
    public long getBarberCountByBarbershop(String barbershopId) {
        return barberRepository.countByBarbershopIdAndActive(barbershopId);
    }

    /**
     * Obtiene barberos con información completa (con usuario)
     */
    @Transactional(readOnly = true)
    public List<BarberResponseDto> getBarbersWithUserInfo() {
        log.info("Obteniendo barberos con información de usuario");
        
        List<Barber> barbers = barberRepository.findAllActiveWithUser();
        return barberMapper.toResponseDtoList(barbers);
    }

    /**
     * Obtiene barberos de una barbería con información completa
     */
    @Transactional(readOnly = true)
    public List<BarberResponseDto> getBarbersByBarbershopWithUserInfo(String barbershopId) {
        log.info("Obteniendo barberos de barbería {} con información de usuario", barbershopId);
        
        List<Barber> barbers = barberRepository.findByBarbershopIdAndActiveWithUser(barbershopId);
        return barberMapper.toResponseDtoList(barbers);
    }

    // Métodos de autorización
    private boolean canAccessBarber(String targetBarberId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Los administradores pueden acceder a cualquier barbero
        if (SecurityUtils.hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }
        
        // Los barberos solo pueden acceder a su propia información
        if (SecurityUtils.hasRole(authentication, "ROLE_BARBER")) {
            String currentUserId = authentication.getName();
            return barberRepository.findByUserIdAndActive(currentUserId)
                    .map(barber -> barber.getBarberId().equals(targetBarberId))
                    .orElse(false);
        }
        
        return false;
    }

    private boolean canModifyBarber(String targetBarberId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Los administradores pueden modificar cualquier barbero
        if (SecurityUtils.hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }
        
        return false;
    }

    /**
     * Obtiene el total de barberos activos
     */
    @Transactional(readOnly = true)
    public long getTotalActiveBarbers() {
        log.info("Obteniendo total de barberos activos");
        return barberRepository.countByIsActiveTrueAndIsDeletedFalse();
    }

}