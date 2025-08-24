package com.barbershop.features.user.service;

import com.barbershop.features.auth.exception.UserAlreadyExistsException;
import com.barbershop.features.auth.exception.UserNotFoundException;
import com.barbershop.features.user.dto.UserCreateDto;
import com.barbershop.features.user.dto.UserResponseDto;
import com.barbershop.features.user.dto.UserUpdateDto;
import com.barbershop.features.user.mapper.UserMapper;
import com.barbershop.features.user.model.User;
import com.barbershop.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Crea un nuevo usuario
     */
    public UserResponseDto createUser(UserCreateDto createDto) {
        log.info("Creando nuevo usuario con email: {}", createDto.getEmail());
        
        // Verificar si el email ya existe
        if (userRepository.existsByEmail(createDto.getEmail())) {
            throw new UserAlreadyExistsException(createDto.getEmail());
        }
        
        // Crear entidad User
        User user = User.builder()
                .email(createDto.getEmail())
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
     * Actualiza un usuario existente
     */
    public UserResponseDto updateUser(String userId, UserUpdateDto updateDto) {
        log.info("Actualizando usuario con ID: {}", userId);
        
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        
        // Verificar si el email ya existe para otro usuario
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndUserIdNot(updateDto.getEmail(), userId)) {
                throw new UserAlreadyExistsException(updateDto.getEmail());
            }
        }
        
        // Actualizar campos
        if (updateDto.getEmail() != null) {
            user.setEmail(updateDto.getEmail());
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
        if (updateDto.getRole() != null) {
            user.setRole(updateDto.getRole());
        }
        if (updateDto.getIsActive() != null) {
            user.setIsActive(updateDto.getIsActive());
        }
        if (updateDto.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(updateDto.getProfilePictureUrl());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Usuario actualizado exitosamente con ID: {}", updatedUser.getUserId());
        
        return userMapper.toResponseDto(updatedUser);
    }

    /**
     * Elimina un usuario por ID usando soft delete
     */
    public void deleteUser(String userId) {
        log.info("Eliminando usuario con ID: {} (soft delete)", userId);
        
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        
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


}