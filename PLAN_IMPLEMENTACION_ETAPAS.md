# Plan de Implementación por Etapas - Sistema de Barbería

## Resumen Ejecutivo

Este documento establece un plan de implementación progresiva del sistema CRUD de barbería, dividido en 4 fases principales con puntos de control específicos para validar cada componente antes de avanzar a la siguiente etapa.

## Metodología de Implementación

### Principios Fundamentales
- **Desarrollo Incremental**: Cada fase construye sobre la anterior
- **Validación Continua**: Puntos de control obligatorios entre fases
- **Rollback Seguro**: Capacidad de revertir cambios si una fase falla
- **Testing Progresivo**: Pruebas unitarias e integración en cada etapa

### Criterios de Avance
Para avanzar a la siguiente fase, se debe cumplir:
1. ✅ Todos los tests unitarios pasan
2. ✅ Tests de integración exitosos
3. ✅ Documentación actualizada
4. ✅ Code review aprobado
5. ✅ Validación funcional completada

---

## FASE 1: INFRAESTRUCTURA BASE Y CONFIGURACIÓN
**Duración Estimada:** 3-5 días
**Prioridad:** CRÍTICA

### Objetivos
- Establecer la base técnica del proyecto
- Configurar herramientas de desarrollo y CI/CD
- Implementar estructura de respuestas unificada

### Componentes a Implementar

#### 1.1 Configuración del Proyecto
```
✓ Configuración de Spring Boot 3.x
✓ Dependencias Maven (pom.xml)
✓ Profiles de entorno (dev, test, prod)
✓ Configuración de base de datos (PostgreSQL)
✓ Configuración de logging (Logback)
```

#### 1.2 Estructura Base de Respuestas
```java
// ApiResponseDto.java - Respuesta unificada
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDto<T> {
    private Integer status;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, Object> errors;
}
```

#### 1.3 Configuración de Seguridad Base
```java
// SecurityConfig.java - Configuración inicial
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Configuración básica sin JWT (se implementa en Fase 2)
}
```

#### 1.4 Global Exception Handler
```java
// GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Manejo de excepciones con ApiResponseDto
}
```

### Puntos de Control - Fase 1

#### ✅ Checkpoint 1.1: Configuración del Proyecto
**Criterios de Validación:**
- [ ] Aplicación Spring Boot inicia correctamente
- [ ] Conexión a base de datos establecida
- [ ] Profiles funcionando (dev, test)
- [ ] Logs configurados y funcionando

**Comando de Validación:**
```bash
mvn clean compile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### ✅ Checkpoint 1.2: Estructura de Respuestas
**Criterios de Validación:**
- [ ] ApiResponseDto compilando correctamente
- [ ] GlobalExceptionHandler manejando errores básicos
- [ ] Endpoint de health check retornando ApiResponseDto

**Test de Validación:**
```java
@Test
void testApiResponseStructure() {
    ApiResponseDto<String> response = ApiResponseDto.<String>builder()
        .status(200)
        .message("Test")
        .data("Success")
        .timestamp(LocalDateTime.now())
        .build();
    
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getMessage()).isEqualTo("Test");
}
```

#### ✅ Checkpoint 1.3: Health Check Endpoint
**Criterios de Validación:**
- [ ] GET /api/v1/health retorna 200
- [ ] Respuesta en formato ApiResponseDto
- [ ] Información de estado de la aplicación

**Endpoint de Prueba:**
```java
@RestController
@RequestMapping("/api/v1")
public class HealthController {
    
    @GetMapping("/health")
    public ApiResponseDto<Map<String, String>> health(HttpServletRequest request) {
        Map<String, String> healthInfo = Map.of(
            "status", "UP",
            "version", "1.0.0",
            "environment", environment.getActiveProfiles()[0]
        );
        
        return ApiResponseDto.<Map<String, String>>builder()
            .status(200)
            .message("Aplicación funcionando correctamente")
            .data(healthInfo)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();
    }
}
```

---

## FASE 2: ENTIDADES CORE Y AUTENTICACIÓN
**Duración Estimada:** 5-7 días
**Prioridad:** CRÍTICA
**Dependencias:** Fase 1 completada

### Objetivos
- Implementar entidades fundamentales del dominio
- Establecer sistema de autenticación JWT
- Crear operaciones CRUD básicas para User

### Componentes a Implementar

#### 2.1 Entidades del Dominio
```java
// User.java - Entidad principal
@Entity
@Table(name = "users")
public class User {
    @Id
    private String userId;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    private RoleEnum role;
    
    // Otros campos según especificación
}

// RoleEnum.java
public enum RoleEnum {
    ADMIN, BARBER, CLIENT
}
```

#### 2.2 Sistema de Autenticación JWT
```java
// JwtTokenProvider.java
@Component
public class JwtTokenProvider {
    public String generateToken(UserDetails userDetails) { /* ... */ }
    public boolean validateToken(String token) { /* ... */ }
    public String getUsernameFromToken(String token) { /* ... */ }
}

// JwtAuthenticationFilter.java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Filtro de autenticación JWT
}
```

#### 2.3 Repositorio y Servicio de Usuario
```java
// UserRepository.java
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Page<User> findByRoleAndIsActive(RoleEnum role, Boolean isActive, Pageable pageable);
}

// UserService.java
@Service
@Transactional
public class UserService {
    public ApiResponseDto<UserResponseDto> createUser(CreateUserRequestDto request);
    public ApiResponseDto<Page<UserResponseDto>> getUsers(UserFilterDto filter, Pageable pageable);
    public ApiResponseDto<UserResponseDto> getUserById(String userId);
    // Otros métodos CRUD
}
```

#### 2.4 Controlador de Usuario
```java
// UserController.java
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {
    
    @PostMapping
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createUser(
            @Valid @RequestBody CreateUserRequestDto request,
            HttpServletRequest httpRequest) {
        // Implementación
    }
    
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            HttpServletRequest httpRequest) {
        // Implementación
    }
    
    // Otros endpoints según especificación
}
```

### Puntos de Control - Fase 2

#### ✅ Checkpoint 2.1: Entidades y Base de Datos
**Criterios de Validación:**
- [ ] Entidad User creada y mapeada correctamente
- [ ] Tablas generadas automáticamente por JPA
- [ ] Relaciones básicas funcionando
- [ ] Migraciones de BD ejecutándose

**Test de Validación:**
```java
@DataJpaTest
class UserRepositoryTest {
    
    @Test
    void testSaveAndFindUser() {
        User user = User.builder()
            .userId(UUID.randomUUID().toString())
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .role(RoleEnum.CLIENT)
            .firstName("Test")
            .lastName("User")
            .isActive(true)
            .build();
            
        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findByEmail("test@example.com");
        
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
}
```

#### ✅ Checkpoint 2.2: Autenticación JWT
**Criterios de Validación:**
- [ ] JwtTokenProvider genera tokens válidos
- [ ] JwtAuthenticationFilter valida tokens correctamente
- [ ] Endpoints protegidos requieren autenticación
- [ ] Roles y permisos funcionando

**Test de Validación:**
```java
@SpringBootTest
@AutoConfigureTestDatabase
class JwtAuthenticationTest {
    
    @Test
    void testTokenGeneration() {
        UserDetails userDetails = createTestUserDetails();
        String token = jwtTokenProvider.generateToken(userDetails);
        
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token))
            .isEqualTo(userDetails.getUsername());
    }
}
```

#### ✅ Checkpoint 2.3: CRUD de Usuario
**Criterios de Validación:**
- [ ] POST /api/v1/users crea usuario correctamente
- [ ] GET /api/v1/users retorna lista paginada
- [ ] GET /api/v1/users?id={userId} retorna usuario específico
- [ ] PUT/PATCH /api/v1/users?id={userId} actualiza usuario
- [ ] DELETE /api/v1/users?id={userId} desactiva usuario (soft delete)

**Test de Integración:**
```java
@SpringBootTest
@AutoConfigureTestDatabase
@TestMethodOrder(OrderAnnotation.class)
class UserIntegrationTest {
    
    @Test
    @Order(1)
    void testCreateUser() throws Exception {
        CreateUserRequestDto request = CreateUserRequestDto.builder()
            .email("integration@test.com")
            .password("password123")
            .firstName("Integration")
            .lastName("Test")
            .role("CLIENT")
            .build();
            
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.message").value("Usuario creado exitosamente"))
            .andExpect(jsonPath("$.data.email").value("integration@test.com"));
    }
}
```

---

## FASE 3: SERVICIOS DE NEGOCIO Y VALIDACIONES
**Duración Estimada:** 6-8 días
**Prioridad:** ALTA
**Dependencias:** Fase 2 completada

### Objetivos
- Implementar entidades de negocio (Barbershop, Barber, Service)
- Establecer validaciones robustas
- Crear relaciones entre entidades

### Componentes a Implementar

#### 3.1 Entidades de Negocio
```java
// Barbershop.java
@Entity
@Table(name = "barbershops")
public class Barbershop {
    @Id
    private String barbershopId;
    
    @Column(nullable = false)
    private String name;
    
    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL)
    private List<Barber> barbers;
    
    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL)
    private List<Service> services;
    
    // Otros campos
}

// Barber.java
@Entity
@Table(name = "barbers")
public class Barber {
    @Id
    private String barberId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id")
    private Barbershop barbershop;
    
    // Otros campos
}

// Service.java
@Entity
@Table(name = "services")
public class Service {
    @Id
    private String serviceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id")
    private Barbershop barbershop;
    
    // Otros campos
}
```

#### 3.2 DTOs y Validaciones
```java
// CreateBarbershopRequestDto.java
@Data
@Builder
public class CreateBarbershopRequestDto {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;
    
    @NotBlank(message = "La dirección es obligatoria")
    private String addressText;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Formato de teléfono inválido")
    private String phoneNumber;
    
    @Email(message = "Formato de email inválido")
    private String email;
    
    @Valid
    private Map<String, OperatingHoursDto> operatingHours;
}

// Validador personalizado
@Component
public class BarbershopValidator {
    
    public void validateOperatingHours(Map<String, OperatingHoursDto> hours) {
        // Lógica de validación personalizada
    }
    
    public void validateUniqueEmail(String email, String barbershopId) {
        // Validación de email único
    }
}
```

#### 3.3 Servicios de Negocio
```java
// BarbershopService.java
@Service
@Transactional
public class BarbershopService {
    
    private final BarbershopRepository barbershopRepository;
    private final BarbershopValidator barbershopValidator;
    private final BarbershopMapper barbershopMapper;
    
    public ApiResponseDto<BarbershopResponseDto> createBarbershop(
            CreateBarbershopRequestDto request, HttpServletRequest httpRequest) {
        
        // Validaciones
        barbershopValidator.validateUniqueEmail(request.getEmail(), null);
        barbershopValidator.validateOperatingHours(request.getOperatingHours());
        
        // Mapeo y persistencia
        Barbershop barbershop = barbershopMapper.toEntity(request);
        barbershop.setBarbershopId(UUID.randomUUID().toString());
        barbershop.setCreatedAt(LocalDateTime.now());
        barbershop.setUpdatedAt(LocalDateTime.now());
        
        Barbershop saved = barbershopRepository.save(barbershop);
        BarbershopResponseDto response = barbershopMapper.toResponseDto(saved);
        
        return ApiResponseDto.<BarbershopResponseDto>builder()
            .status(201)
            .message("Barbería creada exitosamente")
            .data(response)
            .timestamp(LocalDateTime.now())
            .path(httpRequest.getRequestURI())
            .build();
    }
    
    // Otros métodos CRUD
}
```

### Puntos de Control - Fase 3

#### ✅ Checkpoint 3.1: Entidades de Negocio
**Criterios de Validación:**
- [ ] Entidades Barbershop, Barber, Service creadas
- [ ] Relaciones JPA funcionando correctamente
- [ ] Migraciones de BD ejecutadas
- [ ] Constraints de BD aplicados

#### ✅ Checkpoint 3.2: Validaciones
**Criterios de Validación:**
- [ ] Validaciones Bean Validation funcionando
- [ ] Validadores personalizados implementados
- [ ] Mensajes de error en español
- [ ] GlobalExceptionHandler capturando errores de validación

#### ✅ Checkpoint 3.3: CRUD Completo
**Criterios de Validación:**
- [ ] Todos los endpoints de Barbershop funcionando
- [ ] Todos los endpoints de Barber funcionando
- [ ] Todos los endpoints de Service funcionando
- [ ] Paginación y filtros implementados
- [ ] Soft delete funcionando donde corresponde

---

## FASE 4: SISTEMA DE CITAS Y PAGOS
**Duración Estimada:** 7-10 días
**Prioridad:** ALTA
**Dependencias:** Fase 3 completada

### Objetivos
- Implementar sistema completo de citas
- Gestión de disponibilidad de barberos
- Sistema de pagos
- Lógica de negocio compleja

### Componentes a Implementar

#### 4.1 Entidades Finales
```java
// BarberAvailability.java
@Entity
@Table(name = "barber_availability")
public class BarberAvailability {
    @Id
    private String barberAvailabilityId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id")
    private Barber barber;
    
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    
    // Otros campos
}

// Appointment.java
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    private String appointmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id")
    private Barber barber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;
    
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
    
    // Otros campos
}

// Payment.java
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    private String paymentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    
    // Otros campos
}
```

#### 4.2 Lógica de Negocio Compleja
```java
// AppointmentService.java
@Service
@Transactional
public class AppointmentService {
    
    public ApiResponseDto<AppointmentResponseDto> createAppointment(
            CreateAppointmentRequestDto request, HttpServletRequest httpRequest) {
        
        // Validaciones complejas
        validateAppointmentAvailability(request);
        validateBusinessRules(request);
        
        // Lógica de creación
        Appointment appointment = createAppointmentEntity(request);
        Appointment saved = appointmentRepository.save(appointment);
        
        // Notificaciones (si aplica)
        notificationService.sendAppointmentConfirmation(saved);
        
        return buildSuccessResponse(saved, httpRequest);
    }
    
    private void validateAppointmentAvailability(CreateAppointmentRequestDto request) {
        // Verificar disponibilidad del barbero
        // Verificar que no haya conflictos de horario
        // Verificar horarios de operación de la barbería
    }
    
    private void validateBusinessRules(CreateAppointmentRequestDto request) {
        // Reglas de negocio específicas
        // Tiempo mínimo de anticipación
        // Límites de citas por cliente
        // etc.
    }
}

// AvailabilityService.java
@Service
public class AvailabilityService {
    
    public List<AvailableSlotDto> getAvailableSlots(
            String barberId, LocalDate date, String serviceId) {
        
        // Obtener disponibilidad del barbero
        List<BarberAvailability> availability = getBarberAvailability(barberId, date);
        
        // Obtener citas existentes
        List<Appointment> existingAppointments = getExistingAppointments(barberId, date);
        
        // Calcular slots disponibles
        return calculateAvailableSlots(availability, existingAppointments, serviceId);
    }
}
```

### Puntos de Control - Fase 4

#### ✅ Checkpoint 4.1: Sistema de Disponibilidad
**Criterios de Validación:**
- [ ] BarberAvailability CRUD funcionando
- [ ] Cálculo de slots disponibles correcto
- [ ] Validación de horarios de operación
- [ ] API de consulta de disponibilidad

#### ✅ Checkpoint 4.2: Sistema de Citas
**Criterios de Validación:**
- [ ] Creación de citas con validaciones
- [ ] Estados de cita funcionando
- [ ] Cancelación y reprogramación
- [ ] Historial de citas por cliente/barbero

#### ✅ Checkpoint 4.3: Sistema de Pagos
**Criterios de Validación:**
- [ ] Registro de pagos
- [ ] Estados de pago
- [ ] Integración con citas
- [ ] Reportes básicos de pagos

---

## CRONOGRAMA DE IMPLEMENTACIÓN

### Semana 1
- **Días 1-3**: Fase 1 - Infraestructura base
- **Días 4-5**: Checkpoints y correcciones Fase 1

### Semana 2
- **Días 1-4**: Fase 2 - Entidades core y autenticación
- **Día 5**: Checkpoints y correcciones Fase 2

### Semana 3
- **Días 1-4**: Fase 3 - Servicios de negocio
- **Día 5**: Checkpoints y correcciones Fase 3

### Semana 4
- **Días 1-5**: Fase 4 - Sistema de citas y pagos

### Semana 5
- **Días 1-2**: Checkpoints finales Fase 4
- **Días 3-5**: Testing integral y documentación

## CRITERIOS DE CALIDAD

### Cobertura de Tests
- **Mínimo 80%** de cobertura de código
- **100%** de cobertura en servicios críticos
- Tests unitarios para toda la lógica de negocio
- Tests de integración para todos los endpoints

### Performance
- Tiempo de respuesta < 200ms para operaciones simples
- Tiempo de respuesta < 500ms para operaciones complejas
- Paginación eficiente para listas grandes

### Seguridad
- Todos los endpoints protegidos apropiadamente
- Validación de entrada en todos los niveles
- Logs de auditoría para operaciones críticas

## PLAN DE ROLLBACK

Cada fase debe tener un plan de rollback:

1. **Git Tags**: Crear tag al completar cada fase
2. **Database Migrations**: Reversibles con Flyway
3. **Feature Flags**: Para funcionalidades nuevas
4. **Monitoring**: Alertas para detectar problemas

## DOCUMENTACIÓN REQUERIDA

Por cada fase:
- [ ] README actualizado
- [ ] Documentación de API (Swagger)
- [ ] Guía de instalación y configuración
- [ ] Casos de prueba documentados
- [ ] Troubleshooting guide

---

**Nota**: Este plan debe ser revisado y aprobado antes de iniciar la implementación. Cada checkpoint es obligatorio y debe ser validado antes de continuar con la siguiente fase.