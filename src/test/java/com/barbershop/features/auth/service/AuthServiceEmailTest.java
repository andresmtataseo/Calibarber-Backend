package com.barbershop.features.auth.service;

import com.barbershop.common.service.EmailService;
import com.barbershop.features.auth.config.AuthProperties;
import com.barbershop.features.auth.dto.ForgotPasswordRequestDto;
import com.barbershop.features.auth.model.PasswordResetToken;
import com.barbershop.features.auth.repository.PasswordResetTokenRepository;
import com.barbershop.features.auth.util.AuthUtils;
import com.barbershop.features.user.model.User;
import com.barbershop.features.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para verificar la funcionalidad de envío de correos
 * en el proceso de recuperación de contraseña
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceEmailTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private AuthProperties authProperties;

    @Mock
    private AuthProperties.ResetToken resetTokenProperties;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private ForgotPasswordRequestDto forgotPasswordRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Juan");
        testUser.setLastName("Pérez");

        forgotPasswordRequest = ForgotPasswordRequestDto.builder()
                .email("test@example.com")
                .build();

        // Configurar mocks
        when(authProperties.getResetToken()).thenReturn(resetTokenProperties);
        when(resetTokenProperties.getExpirationTime()).thenReturn(900000L); // 15 minutos
    }

    @Test
    void forgotPassword_DeberiaEnviarCorreoExitosamente() throws Exception {
        // Arrange
        String expectedToken = "ABC123XYZ";
        String normalizedEmail = "test@example.com";

        when(authUtils.normalizeEmail(anyString())).thenReturn(normalizedEmail);
        when(userRepository.findByEmail(normalizedEmail)).thenReturn(Optional.of(testUser));
        when(authUtils.generateResetToken()).thenReturn(expectedToken);
        
        PasswordResetToken mockToken = new PasswordResetToken();
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenReturn(mockToken);

        // Act
        authService.forgotPassword(forgotPasswordRequest);

        // Assert
        verify(emailService, times(1)).enviarTokenRecuperacion(
                eq(testUser.getEmail()),
                eq(testUser.getFirstName()),
                eq(expectedToken),
                eq(15) // 15 minutos
        );

        verify(passwordResetTokenRepository, times(1)).markAllUserTokensAsUsed(
                eq(testUser),
                any(LocalDateTime.class)
        );

        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    void forgotPassword_DeberiaManejearErrorDeCorreoSinLanzarExcepcion() throws Exception {
        // Arrange
        String expectedToken = "ABC123XYZ";
        String normalizedEmail = "test@example.com";

        when(authUtils.normalizeEmail(anyString())).thenReturn(normalizedEmail);
        when(userRepository.findByEmail(normalizedEmail)).thenReturn(Optional.of(testUser));
        when(authUtils.generateResetToken()).thenReturn(expectedToken);
        
        PasswordResetToken mockToken = new PasswordResetToken();
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenReturn(mockToken);

        // Simular error en el envío de correo
        doThrow(new RuntimeException("Error de conexión SMTP"))
                .when(emailService).enviarTokenRecuperacion(anyString(), anyString(), anyString(), anyInt());

        // Act & Assert - No debería lanzar excepción
        authService.forgotPassword(forgotPasswordRequest);

        // Verificar que el token se guardó a pesar del error de correo
        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).enviarTokenRecuperacion(anyString(), anyString(), anyString(), anyInt());
    }
}