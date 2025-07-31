package com.barbershop.features.auth.service;

import com.barbershop.features.auth.dto.AuthResponseDto;
import com.barbershop.features.auth.dto.SignInRequestDto;
import com.barbershop.features.auth.dto.SignUpRequestDto;
import com.barbershop.features.auth.dto.ChangePasswordRequestDto;
import com.barbershop.features.auth.exception.InvalidCredentialsException;
import com.barbershop.features.auth.exception.UserAlreadyExistsException;
import com.barbershop.features.auth.exception.PasswordMismatchException;
import com.barbershop.features.auth.security.JwtService;
import com.barbershop.features.auth.AuthUserMapper;
import com.barbershop.features.user.model.enums.RoleEnum;
import com.barbershop.features.user.repository.UserRepository;
import com.barbershop.features.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthUserMapper authUserMapper;

    /**
     * Autentica un usuario con email y contraseña
     * @param request Datos de inicio de sesión
     * @return Respuesta de autenticación con token JWT
     * @throws InvalidCredentialsException si las credenciales son inválidas
     */
    @Transactional(readOnly = true)
    public AuthResponseDto signIn(SignInRequestDto request) {
        try {
            log.info("Intentando autenticar usuario con email: {}", request.getEmail());
            
            // Autenticar credenciales
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            // Buscar usuario
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));
            
            // Generar token
            String token = jwtService.getToken(user);
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = jwtService.getExpirationDateFromToken(token);
            
            log.info("Usuario autenticado exitosamente: {}", request.getEmail());
            
            return AuthResponseDto.builder()
                    .token(token)
                    .type("Bearer")
                    .issuedAt(issuedAt)
                    .expiresAt(expiresAt)
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .fullName(user.getFirstName() + " " + user.getLastName())
                    .build();
                    
        } catch (BadCredentialsException e) {
            log.warn("Intento de inicio de sesión fallido para email: {}", request.getEmail());
            throw new InvalidCredentialsException("Email o contraseña incorrectos");
        } catch (AuthenticationException e) {
            log.warn("Error de autenticación para email: {}", request.getEmail());
            throw new InvalidCredentialsException("Error en la autenticación");
        }
    }

    /**
     * Registra un nuevo usuario en el sistema
     * @param request Datos de registro
     * @return Respuesta de autenticación con token JWT
     * @throws UserAlreadyExistsException si el email ya está registrado
     */
    @Transactional
    public AuthResponseDto signUp(SignUpRequestDto request) {
        log.info("Intentando registrar nuevo usuario con email: {}", request.getEmail());
        
        // Verificar si el usuario ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Intento de registro con email ya existente: {}", request.getEmail());
            throw new UserAlreadyExistsException(request.getEmail());
        }
        
        // Crear nuevo usuario
        User user = authUserMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(user.getPassword()));
        user.setRole(RoleEnum.CLIENT);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        user = userRepository.save(user);
        
        // Generar token
        String token = jwtService.getToken(user);
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = jwtService.getExpirationDateFromToken(token);
        
        log.info("Usuario registrado exitosamente: {}", request.getEmail());
        
        return AuthResponseDto.builder()
                .token(token)
                .type("Bearer")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .build();
    }

    /**
     * Cambia la contraseña del usuario autenticado
     * @param request Datos para cambio de contraseña
     * @param userEmail Email del usuario autenticado
     * @throws InvalidCredentialsException si la contraseña actual es incorrecta
     * @throws PasswordMismatchException si las nuevas contraseñas no coinciden
     */
    @Transactional
    public void changePassword(ChangePasswordRequestDto request, String userEmail) {
        log.info("Intentando cambiar contraseña para usuario: {}", userEmail);
        
        // Verificar que las nuevas contraseñas coincidan
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new PasswordMismatchException("La nueva contraseña y su confirmación no coinciden");
        }
        
        // Buscar usuario
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        
        // Verificar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            log.warn("Intento de cambio de contraseña con contraseña actual incorrecta para: {}", userEmail);
            throw new InvalidCredentialsException("La contraseña actual es incorrecta");
        }
        
        // Actualizar contraseña
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("Contraseña cambiada exitosamente para usuario: {}", userEmail);
    }

    /**
     * Verifica si un email ya está registrado en el sistema
     * @param email Email a verificar
     * @return true si el email existe, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

}