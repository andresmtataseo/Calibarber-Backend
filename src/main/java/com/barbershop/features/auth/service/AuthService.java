package com.barbershop.features.auth.service;

import com.barbershop.features.auth.dto.*;
import com.barbershop.features.auth.exception.*;
import com.barbershop.features.auth.model.PasswordResetToken;
import com.barbershop.features.auth.repository.PasswordResetTokenRepository;
import com.barbershop.features.auth.security.JwtService;
import com.barbershop.features.auth.util.AuthUtils;
import com.barbershop.features.auth.config.AuthProperties;
import com.barbershop.features.auth.AuthUserMapper;
import com.barbershop.features.user.model.enums.RoleEnum;
import com.barbershop.features.user.repository.UserRepository;
import com.barbershop.features.user.model.User;
import com.barbershop.common.service.EmailService;
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
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthUserMapper authUserMapper;
    private final AuthUtils authUtils;
    private final AuthProperties authProperties;
    private final EmailService emailService;

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
        
        // Enviar email de bienvenida
        try {
            emailService.enviarEmailBienvenida(user.getEmail(), user.getFirstName());
            log.info("Email de bienvenida enviado exitosamente a: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de bienvenida a {}: {}", user.getEmail(), e.getMessage());
            // No lanzamos excepción para no afectar el registro del usuario
        }
        
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

    /**
     * Inicia el proceso de restablecimiento de contraseña
     * @param request Datos de solicitud de restablecimiento
     * @throws UserNotFoundException si el email no está registrado
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDto request) {
        String email = authUtils.normalizeEmail(request.getEmail());
        log.info("Iniciando proceso de restablecimiento de contraseña para: {}", email);
        
        // Verificar que el email existe
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("No se encontró un usuario con el email proporcionado"));
        
        // Invalidar tokens existentes del usuario
        passwordResetTokenRepository.markAllUserTokensAsUsed(user, LocalDateTime.now());
        
        // Generar nuevo token
        String token = authUtils.generateResetToken();
        LocalDateTime expiresAt = LocalDateTime.now()
            .plusSeconds(authProperties.getResetToken().getExpirationTime() / 1000);
        
        // Crear y guardar token de restablecimiento
        PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(token)
            .user(user)
            .expiresAt(expiresAt)
            .used(false)
            .build();
        
        passwordResetTokenRepository.save(resetToken);
        
        log.info("Token de restablecimiento generado para usuario: {}", email);
        
        // Enviar token por correo electrónico
        try {
            int tiempoExpiracionMinutos = (int) (authProperties.getResetToken().getExpirationTime() / 1000 / 60);
            emailService.enviarTokenRecuperacion(user.getEmail(), user.getFirstName(), token, tiempoExpiracionMinutos);
            log.info("Correo de recuperación enviado exitosamente a: {}", email);
            
        } catch (Exception e) {
            log.error("Error al enviar correo de recuperación a {}: {}", email, e.getMessage());
            // No lanzamos excepción para no revelar si el email existe o no
            // El token se guarda de todas formas para que funcione si el usuario lo tiene
        }
    }

    /**
     * Restablece la contraseña usando un token válido
     * @param request Datos de restablecimiento de contraseña
     * @throws InvalidResetTokenException si el token no es válido
     */
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        log.info("Intentando restablecer contraseña con token");
        
        // Buscar token válido
        PasswordResetToken resetToken = passwordResetTokenRepository
            .findValidTokenByToken(request.getToken(), LocalDateTime.now())
            .orElseThrow(() -> new InvalidResetTokenException("Token de restablecimiento inválido o expirado"));
        
        User user = resetToken.getUser();
        
        // Validar nueva contraseña
        if (!authUtils.isValidPassword(request.getNewPassword())) {
            throw new PasswordMismatchException(authUtils.getPasswordRequirementsMessage());
        }
        
        // Actualizar contraseña
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Marcar token como usado
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);
        
        // Invalidar todos los demás tokens del usuario
        passwordResetTokenRepository.markAllUserTokensAsUsed(user, LocalDateTime.now());
        
        log.info("Contraseña restablecida exitosamente para usuario: {}", user.getEmail());
    }

    /**
     * Verifica si el token JWT es válido y devuelve información del usuario
     * @param token Token JWT a verificar
     * @return Información del usuario autenticado
     * @throws InvalidTokenException si el token no es válido
     */
    @Transactional(readOnly = true)
    public CheckAuthResponseDto checkAuth(String token) {
        try {
            // Extraer email del token
            String email = jwtService.getUsernameFromToken(token);
            
            // Buscar usuario
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Usuario no encontrado"));
            
            // Verificar si el token es válido
            boolean isTokenValid = jwtService.isTokenValid(token, user);
            
            if (!isTokenValid) {
                throw new InvalidTokenException("Token inválido o expirado");
            }
            
            return CheckAuthResponseDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .isActive(user.getIsActive())
                .isTokenValid(true)
                .build();
                
        } catch (Exception e) {
            log.warn("Error al verificar token: {}", e.getMessage());
            throw new InvalidTokenException("Token inválido");
        }
    }

    /**
     * Limpia tokens de restablecimiento expirados
     * Este método debería ser llamado periódicamente por un scheduler
     */
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Limpiando tokens de restablecimiento expirados");
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

}