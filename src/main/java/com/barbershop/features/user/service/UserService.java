package com.barbershop.features.user.service;

import com.barbershop.features.auth.exception.UserAlreadyExistsException;
import com.barbershop.features.auth.exception.UserNotFoundException;
import com.barbershop.features.auth.util.AuthUtils;
import com.barbershop.common.exception.BusinessLogicException;
import org.springframework.dao.DataIntegrityViolationException;
import com.barbershop.features.user.dto.UserCreateDto;
import com.barbershop.features.user.dto.UserResponseDto;
import com.barbershop.features.user.dto.UserUpdateDto;
import com.barbershop.features.user.mapper.UserMapper;
import com.barbershop.features.user.model.User;
import com.barbershop.features.user.repository.UserRepository;
import com.barbershop.features.user.exception.UserHasActiveRecordsException;
import com.barbershop.features.appointment.repository.AppointmentRepository;
import com.barbershop.features.barber.repository.BarberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.barbershop.shared.util.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtils authUtils;
    private final AppointmentRepository appointmentRepository;
    private final BarberRepository barberRepository;

    /**
     * Crea un nuevo usuario
     */
    public UserResponseDto createUser(UserCreateDto createDto) {
        log.info("Creando nuevo usuario con email: {}", createDto.getEmail());
        
        // Normalizar email a minúsculas
        String normalizedEmail = authUtils.normalizeEmail(createDto.getEmail());
        
        // Verificar si el email ya existe
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException(normalizedEmail);
        }
        
        try {
            // Crear entidad User
            User user = User.builder()
                    .email(normalizedEmail)
                    .passwordHash(passwordEncoder.encode(createDto.getPassword()))
                    .firstName(createDto.getFirstName())
                    .lastName(createDto.getLastName())
                    .phoneNumber(createDto.getPhoneNumber())
                    .role(createDto.getRole())
                    .isActive(true)
                    .build();
            
            User savedUser = userRepository.save(user);
            log.info("Usuario creado exitosamente con ID: {}", savedUser.getUserId());
            
            return userMapper.toResponseDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad de datos al crear usuario: {}", e.getMessage());
            String message = "No se pudo crear el usuario";
            
            if (e.getMessage().contains("email")) {
                message = "Ya existe un usuario con este email";
            } else if (e.getMessage().contains("phone_number")) {
                message = "Ya existe un usuario con este número de teléfono";
            }
            
            throw new BusinessLogicException(message);
        }
    }

    /**
     * Obtiene todos los usuarios paginados
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        log.info("Obteniendo usuarios paginados: página {}, tamaño {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<User> users = userRepository.findAllActive(pageable);
        return users.map(userMapper::toResponseDto);
    }

    /**
     * Obtiene todos los usuarios paginados con parámetros individuales
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(int page, int size, String sortBy, String sortDir) {
        log.info("Obteniendo usuarios paginados: página {}, tamaño {}, ordenado por {} {}", 
                page, size, sortBy, sortDir);
        
        // Crear el objeto Sort
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        
        // Crear el objeto Pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        return getAllUsers(pageable);
    }

    /**
     * Obtiene un usuario por ID
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(String userId) {
        log.info("Obteniendo usuario por ID: {}", userId);
        
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        
        return userMapper.toResponseDto(user);
    }

    /**
     * Valida que un usuario existe y está activo (sin restricciones de autorización)
     * Método útil para validaciones internas entre servicios
     */
    @Transactional(readOnly = true)
    public boolean existsAndActive(String userId) {
        return userRepository.findByIdAndNotDeleted(userId).isPresent();
    }

    /**
     * Actualiza un usuario existente
     */
    public UserResponseDto updateUser(String userId, UserUpdateDto updateDto) {
        log.info("Actualizando usuario con ID: {}", userId);
        
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        
        // Verificar autorización
        if (!canAccessUser(userId)) {
            throw new AccessDeniedException("No tienes permisos para modificar este usuario");
        }
        
        // Verificar si el email ya existe para otro usuario
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            String normalizedEmail = authUtils.normalizeEmail(updateDto.getEmail());
            if (userRepository.existsByEmailAndUserIdNot(normalizedEmail, userId)) {
                throw new UserAlreadyExistsException(normalizedEmail);
            }
        }
        
        try {
            // Actualizar campos
            if (updateDto.getEmail() != null) {
                String normalizedEmail = authUtils.normalizeEmail(updateDto.getEmail());
                user.setEmail(normalizedEmail);
            }
            if (updateDto.getFirstName() != null) {
                user.setFirstName(updateDto.getFirstName());
            }
            if (updateDto.getLastName() != null) {
                user.setLastName(updateDto.getLastName());
            }
            if (updateDto.getPhoneNumber() != null) {
                user.setPhoneNumber(updateDto.getPhoneNumber());
            }
            
            // Validación de seguridad: Solo administradores pueden cambiar roles
            if (updateDto.getRole() != null) {
                if (!SecurityUtils.isCurrentUserAdmin()) {
                    throw new AccessDeniedException("Solo los administradores pueden modificar roles de usuario");
                }
                user.setRole(updateDto.getRole());
            }
            
            // Validación de seguridad: Solo administradores pueden cambiar el estado activo
            if (updateDto.getIsActive() != null) {
                if (!SecurityUtils.isCurrentUserAdmin()) {
                    throw new AccessDeniedException("Solo los administradores pueden modificar el estado activo de usuario");
                }
                user.setIsActive(updateDto.getIsActive());
            }
            
            if (updateDto.getProfilePictureUrl() != null) {
                user.setProfilePictureUrl(updateDto.getProfilePictureUrl());
            }
            
            User updatedUser = userRepository.save(user);
            log.info("Usuario actualizado exitosamente con ID: {}", updatedUser.getUserId());
            
            return userMapper.toResponseDto(updatedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad de datos al actualizar usuario: {}", e.getMessage());
            String message = "No se pudo actualizar el usuario";
            
            if (e.getMessage().contains("email")) {
                message = "Ya existe un usuario con este email";
            } else if (e.getMessage().contains("phone_number")) {
                message = "Ya existe un usuario con este número de teléfono";
            }
            
            throw new BusinessLogicException(message);
        }
    }

    /**
     * Elimina un usuario por ID usando soft delete
     * Verifica que el usuario no tenga registros activos antes de eliminar
     */
    public void deleteUser(String userId) {
        log.info("Eliminando usuario con ID: {} (soft delete)", userId);
        
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        
        // Verificar si el usuario tiene citas activas
        long activeAppointments = appointmentRepository.countActiveAppointmentsByClientId(userId);
        if (activeAppointments > 0) {
            log.warn("No se puede eliminar el usuario {} porque tiene {} citas activas", userId, activeAppointments);
            throw new UserHasActiveRecordsException(userId, "citas", activeAppointments);
        }
        
        // Verificar si el usuario es un barbero activo
        boolean isActiveBarber = barberRepository.existsByUserIdAndActive(userId);
        if (isActiveBarber) {
            log.warn("No se puede eliminar el usuario {} porque es un barbero activo", userId);
            throw new UserHasActiveRecordsException(userId, "perfil de barbero");
        }
        
        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("Usuario eliminado exitosamente con ID: {} (soft delete)", userId);
    }

    /**
     * Restaura un usuario eliminado por ID
     */
    public UserResponseDto restoreUser(String userId) {
        log.info("Restaurando usuario con ID: {}", userId);
        
        // Verificar que el usuario existe y está eliminado
        User user = userRepository.findByIdAndDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario eliminado no encontrado con ID: " + userId));
        
        user.setIsDeleted(false);
        user.setDeletedAt(null);
        User restoredUser = userRepository.save(user);
        
        log.info("Usuario restaurado exitosamente con ID: {}", userId);
        return userMapper.toResponseDto(restoredUser);
    }

    /**
     * Obtiene todos los usuarios eliminados paginados
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getDeletedUsers(Pageable pageable) {
        log.info("Obteniendo usuarios eliminados paginados: página {}, tamaño {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<User> deletedUsers = userRepository.findAllDeleted(pageable);
        return deletedUsers.map(userMapper::toResponseDto);
    }

    /**
     * Obtiene usuarios eliminados paginados con parámetros individuales
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getDeletedUsers(int page, int size, String sortBy, String sortDir) {
        log.info("Obteniendo usuarios eliminados paginados: página {}, tamaño {}, ordenado por {} {}", 
                page, size, sortBy, sortDir);
        
        // Crear el objeto Sort
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        
        // Crear el objeto Pageable
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Llamar al método existente
        return getDeletedUsers(pageable);
    }

    /**
     * Obtiene el total de usuarios activos
     */
    @Transactional(readOnly = true)
    public long getTotalActiveUsers() {
        log.info("Obteniendo total de usuarios activos");
        return userRepository.countByIsActiveTrueAndIsDeletedFalse();
    }

    /**
     * Verifica si el usuario autenticado puede acceder a los datos del usuario especificado
     */
    private boolean canAccessUser(String targetUserId) {
        // Los administradores pueden acceder a cualquier usuario
        if (SecurityUtils.isCurrentUserAdmin()) {
            return true;
        }
        
        // Los usuarios solo pueden acceder a su propia información
        String currentUserId = SecurityUtils.getCurrentUserId();
        return currentUserId != null && currentUserId.equals(targetUserId);
    }


}