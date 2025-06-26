# Barbershop - Sistema de Gestión para Barberías

## Descripción General
Este proyecto es una aplicación de gestión para barberías, desarrollada como parte de un diplomado. La aplicación proporciona una plataforma para la administración de barberías, incluyendo gestión de usuarios, autenticación y autorización.

## Tecnologías Utilizadas
- **Java 21**: Lenguaje de programación principal
- **Spring Boot 3.5.0**: Framework para el desarrollo de aplicaciones Java
- **Spring Security**: Para la gestión de autenticación y autorización
- **Spring Data JPA**: Para la persistencia de datos
- **PostgreSQL**: Base de datos relacional
- **JWT (JSON Web Tokens)**: Para la autenticación basada en tokens
- **Swagger/OpenAPI**: Para la documentación de la API
- **Docker**: Para la contenerización de la aplicación
- **Maven**: Para la gestión de dependencias y construcción del proyecto

## Características Principales
- **Autenticación de Usuarios**: Registro e inicio de sesión de usuarios
- **Gestión de Usuarios**: Administración de usuarios con diferentes roles (Cliente, Barbero, Administrador)
- **API RESTful**: Interfaz de programación de aplicaciones siguiendo los principios REST
- **Documentación de API**: Documentación completa de la API utilizando Swagger

## Configuración del Proyecto
La aplicación está configurada para ejecutarse en el puerto 8080 por defecto y utiliza PostgreSQL como base de datos. La configuración se puede personalizar a través de variables de entorno.

## Requisitos
- Java 21 o superior
- PostgreSQL
- Docker (opcional, para contenerización)

## Ejecución del Proyecto
### Usando Maven
```bash
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

## Endpoints Principales
- **Autenticación**:
  - POST /api/v1/auth/signin: Inicio de sesión
  - POST /api/v1/auth/signup: Registro de nuevos usuarios
  - GET /api/v1/auth/check: Verificación de autenticación

- **Usuarios**:
  - GET /api/v1/users: Obtener todos los usuarios

## Estado del Proyecto
Este proyecto se encuentra en desarrollo activo como parte de un diplomado. Nuevas características y mejoras se añadirán continuamente.