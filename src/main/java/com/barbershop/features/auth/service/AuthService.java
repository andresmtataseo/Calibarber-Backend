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
import com.barbershop.common.service.EmailRetryService;
import com.barbershop.common.exception.BusinessLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final EmailRetryService emailRetryService;

    /**
     * Autentica un usuario con email y contraseña
     * @param request Datos de inicio de sesión
     * @return Respuesta de autenticación con token JWT
     * @throws InvalidCredentialsException si las credenciales son inválidas
     */
    @Transactional(readOnly = true)
    public AuthResponseDto signIn(SignInRequestDto request) {
        try {
            // Normalizar email a minúsculas
            String normalizedEmail = authUtils.normalizeEmail(request.getEmail());            
            // Autenticar credenciales
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );
            
            // Buscar usuario
            User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));
            
            // Generar token
            String token = jwtService.getToken(user);
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = jwtService.getExpirationDateFromToken(token);
            
            log.info("Usuario autenticado exitosamente: {}", normalizedEmail);
            
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
            String normalizedEmail = authUtils.normalizeEmail(request.getEmail());
            log.warn("Intento de inicio de sesión fallido para email: {}", normalizedEmail);
            throw new InvalidCredentialsException("Email o contraseña incorrectos");
        } catch (AuthenticationException e) {
            String normalizedEmail = authUtils.normalizeEmail(request.getEmail());
            log.warn("Error de autenticación para email: {}", normalizedEmail);
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
        // Normalizar email a minúsculas
        String normalizedEmail = authUtils.normalizeEmail(request.getEmail());
        log.info("Intentando registrar nuevo usuario con email: {}", normalizedEmail);
        
        // Verificar si el usuario ya existe
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("Intento de registro con email ya existente: {}", normalizedEmail);
            throw new UserAlreadyExistsException(normalizedEmail);
        }
        
        // Crear nuevo usuario
        User user = authUserMapper.toUser(request);
        user.setEmail(normalizedEmail); // Asegurar que el email se guarde normalizado
        user.setPasswordHash(passwordEncoder.encode(user.getPassword()));
        user.setRole(RoleEnum.ROLE_CLIENT);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad de datos al registrar usuario: {}", e.getMessage());
            String message = "No se pudo registrar el usuario";
            
            if (e.getMessage().contains("email")) {
                message = "El email ya está registrado en el sistema";
            } else if (e.getMessage().contains("phone_number")) {
                message = "El número de teléfono ya está registrado en el sistema";
            }
            
            throw new BusinessLogicException(message);
        }
        
        // Generar token
        String token = jwtService.getToken(user);
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = jwtService.getExpirationDateFromToken(token);
        
        log.debug("Usuario registrado exitosamente: {}", request.getEmail());
        
        // Enviar email de bienvenida con reintentos
        try {
            emailRetryService.enviarEmailBienvenidaConReintentos(user.getEmail(), user.getFirstName());
        } catch (jakarta.mail.MessagingException e) {
            log.error("Error de mensajería al enviar email de bienvenida a {}: {}", user.getEmail(), e.getMessage());
            // El usuario se crea exitosamente aunque falle el email
        } catch (org.springframework.mail.MailException e) {
            log.error("Error del servidor de correo al enviar email de bienvenida a {}: {}", user.getEmail(), e.getMessage());
            // El usuario se crea exitosamente aunque falle el email
        } catch (Exception e) {
            emailRetryService.handleEmailFailure(e, user.getEmail());
            // El usuario se crea exitosamente aunque falle el email
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
        String normalizedEmail = authUtils.normalizeEmail(email);
        log.debug("Verificando si el email existe: {}", normalizedEmail);
        boolean exists = userRepository.existsByEmail(normalizedEmail);
        log.debug("Email {} existe: {}", normalizedEmail, exists);
        return exists;
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
        
        // Enviar token por correo electrónico con reintentos
        try {
            int tiempoExpiracionMinutos = (int) (authProperties.getResetToken().getExpirationTime() / 1000 / 60);
            emailRetryService.enviarTokenRecuperacionConReintentos(user.getEmail(), user.getFirstName(), token, tiempoExpiracionMinutos);
            
        } catch (jakarta.mail.MessagingException e) {
            log.error("Error de mensajería al enviar correo de recuperación a {}: {}", email, e.getMessage());
            // No lanzamos excepción para no revelar si el email existe o no
        } catch (org.springframework.mail.MailException e) {
            log.error("Error del servidor de correo al enviar correo de recuperación a {}: {}", email, e.getMessage());
            // No lanzamos excepción para no revelar si el email existe o no
        } catch (Exception e) {
            emailRetryService.handleEmailFailure(e, email);
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
     * Verifica la información del usuario autenticado por email
     * @param email Email del usuario autenticado
     * @return Información del usuario autenticado
     * @throws InvalidTokenException si el usuario no es encontrado
     */
    @Transactional(readOnly = true)
    public CheckAuthResponseDto checkAuthByEmail(String email) {
        try {
            // Buscar usuario por email
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Usuario no encontrado"));
            
            return CheckAuthResponseDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .isActive(user.getIsActive())
                .isTokenValid(true)
                .build();
                
        } catch (Exception e) {
            log.warn("Error al verificar usuario por email: {}", e.getMessage());
            throw new InvalidTokenException("Usuario no válido");
        }
    }

    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Limpiando tokens de restablecimiento expirados");
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

}