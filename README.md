# Calibarber Backend — Sistema de Gestión para Barberías

<p align="center">
  <img src="./logo.png" alt="Calibarber" width="180" />
  
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-007396?logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.0-6DB33F?logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Maven-3.x-C71A36?logo=apachemaven&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-13%2B-4169E1?logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/OpenAPI-3.x-6BA539?logo=openapiinitiative&logoColor=white" />
  <img src="https://img.shields.io/badge/JWT-Auth-000000?logo=jsonwebtokens&logoColor=white" />
</p>

Calibarber Backend es la API REST para gestionar barberías, usuarios, barberos, servicios, citas y pagos. Forma parte del proyecto completo Calibarber junto con el frontend en Angular: https://github.com/andresmtataseo/Calibarber-Frontend.

## Tabla de Contenidos
- Descripción General
- Tecnologías Utilizadas
- Arquitectura y Módulos
- Características Clave
- Configuración y Perfiles
- Requisitos
- Instalación y Ejecución
- Documentación de la API
- Endpoints Principales
- Sistema de Soft Delete
- Integración con el Frontend
- Despliegue con Docker
- Buenas Prácticas

## Descripción General
Esta API está construida con Spring Boot 3 para ofrecer un backend robusto, seguro y escalable. Proporciona autenticación basada en JWT, gestión de usuarios y roles, administración de barberías, disponibilidad de barberos, catálogo de servicios, programación de citas y registro de pagos.

## Tecnologías Utilizadas
- Java 21
- Spring Boot 3.5.0
- Spring Security (JWT)
- Spring Data JPA (Hibernate)
- PostgreSQL
- MapStruct
- Lombok
- Swagger/OpenAPI (Springdoc)
- Maven
- Docker
- SLF4J + Logback (logging)

## Arquitectura y Módulos
Arquitectura modular basada en características (feature-based), con separación por capas: Controller → Service → Repository → Model/DTO.

Módulos:
- Auth: autenticación y autorización
- User: gestión de usuarios y roles
- Barbershop: gestión de barberías
- Barber: gestión de barberos y disponibilidad
- Service: gestión de servicios
- Appointment: sistema de citas
- Payment: registro de pagos
- Common: utilidades compartidas (excepciones, validaciones, respuestas estándar)

## Características Clave
- Autenticación JWT: registro, inicio de sesión, cambio de contraseña y verificación de email
- Seguridad: configuración completa de Spring Security y roles (Cliente, Barbero, Admin)
- Soft Delete: preserva integridad y permite restauración de registros
- Validación: Bean Validation y validaciones personalizadas
- Excepciones: manejo global con `@ControllerAdvice`
- Documentación: Swagger/OpenAPI totalmente configurado
- Logging: trazas estructuradas en `logs/calibarber-dev.log`

## Configuración y Perfiles
- dev: desarrollo local con PostgreSQL
- prod: producción con variables de entorno

Variables de entorno típicas:
- `SPRING_PROFILES_ACTIVE=dev`
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`

## Requisitos
- Java 21+
- PostgreSQL 13+
- Maven 3.8+
- Docker (opcional)

## Instalación y Ejecución

### Base de Datos
1. Instala PostgreSQL
2. Crea la base de datos: `calibarber_db`
3. Configura credenciales en `application-dev.properties`

Ejemplo `src/main/resources/application-dev.properties` (orientativo):
```
spring.datasource.url=jdbc:postgresql://localhost:5432/calibarber_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
springdoc.api-docs.enabled=true
```

### Con Maven
- Compilar: `mvn clean compile`
- Pruebas: `mvn test`
- Ejecutar: `mvn spring-boot:run`

En Windows puedes usar el wrapper: `mvnw.cmd spring-boot:run`

## Documentación de la API
Una vez en ejecución, accede a Swagger UI:
- `http://localhost:8080/swagger-ui.html`

## Endpoints Principales

### Autenticación (`/auth`)
- `POST /auth/sign-in`: inicio de sesión
- `POST /auth/sign-up`: registro
- `PUT /auth/change-password`: cambio de contraseña
- `GET /auth/check-email`: verificación de disponibilidad de email

### Usuarios (`/api/v1/users`)
- `GET /api/v1/users`: listado básico (implementación inicial)

## Sistema de Soft Delete

Entidades:
- Usuarios: conservan historial sin perder referencias
- Barberías: útiles para reportes y auditorías

Características:
- Campos: `isDeleted` y `deletedAt`
- Filtro automático en consultas
- Restauración de registros
- Endpoints administrativos para inspección

Endpoints:
- `DELETE /api/v1/users?id={userId}`
- `POST /api/v1/users/restore?id={userId}`
- `GET /api/v1/users/deleted`
- `DELETE /api/v1/barbershops?id={barbershopId}`
- `POST /api/v1/barbershops/restore?id={barbershopId}`
- `GET /api/v1/barbershops/deleted`

## Integración con el Frontend
Este backend se integra con el cliente Angular del proyecto Calibarber: https://github.com/andresmtataseo/Calibarber-Frontend. Asegúrate de habilitar CORS según sea necesario y mantener consistentes las rutas y contratos de la API.

## Despliegue con Docker
Arranca servicios con:
```
docker-compose up
```

## Buenas Prácticas
- Arquitectura modular por características
- Separación de responsabilidades (Controller–Service–Repository)
- DTOs y mapeo con MapStruct
- Excepciones centralizadas
- Validaciones de entrada
- Documentación con OpenAPI
- Perfiles y configuración por entorno
- Anotaciones Spring apropiadas
- Principios SOLID