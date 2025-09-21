package com.barbershop.config;

import com.barbershop.features.user.dto.UserCreateDto;
import com.barbershop.features.user.model.enums.RoleEnum;
import com.barbershop.features.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Clase para cargar datos iniciales en la aplicación
 * Se ejecuta automáticamente al iniciar la aplicación Spring Boot
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando carga de datos por defecto...");
        
        try {
            createDefaultAdminUser();
            log.info("Carga de datos completada exitosamente");
        } catch (Exception e) {
            log.error("Error durante la carga de datos: {}", e.getMessage());
        }
    }

    /**
     * Crea el usuario administrador por defecto si no existe
     */
    private void createDefaultAdminUser() {
        final String ADMIN_EMAIL = "admin@mail.com";
        final String ADMIN_PASSWORD = "admin123456"; // Contraseña temporal - debe cambiarse en producción
        
        try {
            log.info("Verificando si existe el usuario administrador por defecto...");
            
            // Intentar crear el usuario administrador
            UserCreateDto adminUser = UserCreateDto.builder()
                    .email(ADMIN_EMAIL)
                    .password(ADMIN_PASSWORD)
                    .firstName("Administrador")
                    .lastName("Sistema")
                    .phoneNumber("+1234567890")
                    .role(RoleEnum.ROLE_ADMIN)
                    .build();
            
            userService.createUser(adminUser);
            log.info("✅ Usuario administrador creado exitosamente:");
            log.info("   📧 Email: {}", ADMIN_EMAIL);
            log.info("   🔑 Contraseña temporal: {}", ADMIN_PASSWORD);
            log.info("   ⚠️  IMPORTANTE: Cambiar la contraseña por defecto en producción");
            
        } catch (Exception e) {
            // Si el usuario ya existe, no es un error crítico
            if (e.getMessage().contains("Ya existe un usuario") || 
                e.getMessage().contains("already exists")) {
                log.info("ℹ️  El usuario administrador ya existe en el sistema");
            } else {
                log.error("❌ Error al crear el usuario administrador: {}", e.getMessage());
            }
        }
    }
}