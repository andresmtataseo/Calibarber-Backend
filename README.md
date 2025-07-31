# Barbershop - Sistema de Gestión para Barberías

## Descripción General
Este proyecto es una aplicación de gestión para barberías, desarrollada como parte de un diplomado. La aplicación proporciona una plataforma completa para la administración de barberías, incluyendo gestión de usuarios, autenticación, citas, servicios y pagos.

## Tecnologías Utilizadas
- **Java 21**: Lenguaje de programación principal
- **Spring Boot 3.5.0**: Framework para el desarrollo de aplicaciones Java
- **Spring Security**: Para la gestión de autenticación y autorización
- **Spring Data JPA**: Para la persistencia de datos
- **PostgreSQL**: Base de datos relacional
- **JWT (JSON Web Tokens)**: Para la autenticación basada en tokens
- **Swagger/OpenAPI**: Para la documentación de la API
- **MapStruct**: Para el mapeo entre DTOs y entidades
- **Lombok**: Para reducir código boilerplate
- **Docker**: Para la contenerización de la aplicación
- **Maven**: Para la gestión de dependencias y construcción del proyecto

## Arquitectura del Proyecto
El proyecto sigue una arquitectura modular basada en características (feature-based), con los siguientes módulos:

### Módulos Implementados
- **Auth**: Sistema de autenticación y autorización
- **User**: Gestión de usuarios
- **Barbershop**: Gestión de barberías
- **Barber**: Gestión de barberos y disponibilidad
- **Service**: Gestión de servicios ofrecidos
- **Appointment**: Gestión de citas
- **Payment**: Gestión de pagos
- **Common**: Utilidades y componentes compartidos

### Características Implementadas
- ✅ **Autenticación JWT**: Registro, inicio de sesión y cambio de contraseña
- ✅ **Gestión de Usuarios**: Modelo de usuario con roles (Cliente, Barbero, Administrador)
- ✅ **Manejo Global de Excepciones**: Centralizado en GlobalExceptionHandler
- ✅ **Validación de Datos**: Validaciones personalizadas y estándar
- ✅ **Documentación API**: Swagger/OpenAPI completamente configurado
- ✅ **Configuración Multi-ambiente**: Perfiles dev y prod
- ✅ **Seguridad**: Configuración completa de Spring Security
- ✅ **Mapeo de Entidades**: MapStruct para conversión DTO-Entity

## Configuración del Proyecto
La aplicación está configurada para ejecutarse en el puerto 8080 por defecto y utiliza PostgreSQL como base de datos. La configuración se puede personalizar a través de variables de entorno.

### Perfiles de Configuración
- **dev**: Desarrollo local con base de datos PostgreSQL local
- **prod**: Producción con variables de entorno

## Requisitos
- Java 21 o superior
- PostgreSQL 13 o superior
- Maven 3.8 o superior
- Docker (opcional, para contenerización)

## Ejecución del Proyecto

### Configuración de Base de Datos
1. Instalar PostgreSQL
2. Crear base de datos: `calibarber_db`
3. Configurar credenciales en `application-dev.properties`

### Usando Maven
```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar pruebas
mvn test

# Ejecutar la aplicación
mvn spring-boot:run
```

### Usando Docker
```bash
docker-compose up
```

## Acceso a la Documentación de la API
Una vez que la aplicación esté en ejecución, puede acceder a la documentación de la API en:
```
http://localhost:8080/swagger-ui.html
```

## Endpoints Implementados

### Autenticación (/auth)
- ✅ `POST /auth/sign-in`: Inicio de sesión
- ✅ `POST /auth/sign-up`: Registro de nuevos usuarios
- ✅ `PUT /auth/change-password`: Cambio de contraseña
- ✅ `GET /auth/check-email`: Verificación de disponibilidad de email

### Usuarios (/user)
- ⚠️ `GET /user/findAll`: Obtener todos los usuarios (implementación básica)

## Funcionalidades Pendientes por Implementar

### 🔴 Críticas (Alta Prioridad)
1. **Repositorios Faltantes**
   - BarbershopRepository
   - BarberRepository
   - ServiceRepository
   - AppointmentRepository
   - PaymentRepository

2. **Servicios Faltantes**
   - UserService (CRUD completo)
   - BarbershopService
   - BarberService
   - ServiceService
   - AppointmentService
   - PaymentService

3. **Controladores Faltantes**
   - BarbershopController
   - BarberController
   - ServiceController
   - AppointmentController
   - PaymentController

### 🟡 Importantes (Media Prioridad)
4. **Funcionalidades de Autenticación**
   - Recuperación de contraseña (forgot-password)
   - Reset de contraseña (reset-password)
   - Verificación de autenticación (check-auth)
   - Refresh token

5. **Gestión de Usuarios**
   - CRUD completo de usuarios
   - Gestión de perfiles
   - Cambio de roles
   - Activación/desactivación de usuarios

6. **Gestión de Barberías**
   - CRUD de barberías
   - Gestión de horarios de operación
   - Subida de logos

### 🟢 Deseables (Baja Prioridad)
7. **Gestión de Barberos**
   - CRUD de barberos
   - Gestión de disponibilidad
   - Especialidades

8. **Gestión de Servicios**
   - CRUD de servicios
   - Categorización
   - Precios dinámicos

9. **Sistema de Citas**
   - Creación de citas
   - Cancelación y reprogramación
   - Notificaciones
   - Historial de citas

10. **Sistema de Pagos**
    - Procesamiento de pagos
    - Historial de transacciones
    - Reportes financieros

### 🔧 Mejoras Técnicas
11. **Testing**
    - Pruebas unitarias para servicios
    - Pruebas de integración
    - Pruebas de controladores

12. **Seguridad**
    - Rate limiting
    - Validación de entrada mejorada
    - Auditoría de acciones

13. **Performance**
    - Caché de datos
    - Paginación
    - Optimización de consultas

14. **Monitoreo**
    - Métricas de aplicación
    - Health checks
    - Logging estructurado

## Buenas Prácticas Implementadas
- ✅ Arquitectura modular por características
- ✅ Separación de responsabilidades (Controller-Service-Repository)
- ✅ DTOs para transferencia de datos
- ✅ Mappers para conversión de objetos
- ✅ Manejo centralizado de excepciones
- ✅ Validaciones de entrada
- ✅ Documentación de API
- ✅ Configuración por perfiles
- ✅ Uso de anotaciones Spring apropiadas
- ✅ Principios SOLID aplicados

## Estado del Proyecto
Este proyecto se encuentra en **desarrollo activo** como parte de un diplomado. La base arquitectónica está sólida y lista para la implementación de las funcionalidades pendientes. El sistema de autenticación y la estructura modular proporcionan una base robusta para el desarrollo continuo.