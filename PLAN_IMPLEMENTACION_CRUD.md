# Plan de Implementación CRUD - Sistema de Barbería

## 1. Especificación de Entidades

### 1.1 User (Usuario)
**Tabla:** `users`

| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| userId | String (UUID) | PK, NOT NULL | Identificador único del usuario |
| email | String(255) | UNIQUE, NOT NULL | Email del usuario |
| passwordHash | String | NOT NULL | Hash de la contraseña |
| role | RoleEnum | NOT NULL | Rol del usuario (ADMIN, BARBER, CLIENT) |
| firstName | String(50) | NOT NULL | Nombre del usuario |
| lastName | String(50) | NOT NULL | Apellido del usuario |
| phoneNumber | String(20) | NULL | Número de teléfono |
| isActive | Boolean | NOT NULL, DEFAULT true | Estado activo del usuario |
| profilePictureUrl | String | NULL | URL de la foto de perfil |
| createdAt | LocalDateTime | NOT NULL | Fecha de creación |
| updatedAt | LocalDateTime | NOT NULL | Fecha de última actualización |

**Relaciones:**
- OneToMany con Barber (user -> barbers)
- OneToMany con Appointment (client -> clientAppointments)

### 1.2 Barbershop (Barbería)
**Tabla:** `barbershops`

| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| barbershopId | String (UUID) | PK, NOT NULL | Identificador único de la barbería |
| name | String(100) | NOT NULL | Nombre de la barbería |
| addressText | String | NOT NULL | Dirección de la barbería |
| phoneNumber | String(20) | NULL | Teléfono de la barbería |
| email | String(100) | NULL | Email de la barbería |
| operatingHours | String (JSON) | NULL | Horarios de operación |
| logoUrl | String | NULL | URL del logo |
| createdAt | LocalDateTime | NOT NULL | Fecha de creación |
| updatedAt | LocalDateTime | NOT NULL | Fecha de última actualización |

**Relaciones:**
- OneToMany con Barber (barbershop -> barbers)
- OneToMany con Service (barbershop -> services)

### 1.3 Barber (Barbero)
**Tabla:** `barbers`

| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| barberId | String (UUID) | PK, NOT NULL | Identificador único del barbero |
| userId | String (UUID) | FK, NOT NULL | Referencia al usuario |
| barbershopId | String (UUID) | FK, NOT NULL | Referencia a la barbería |
| specialization | String(100) | NULL | Especialización del barbero |
| isActive | Boolean | NOT NULL, DEFAULT true | Estado activo del barbero |
| createdAt | LocalDateTime | NOT NULL | Fecha de creación |
| updatedAt | LocalDateTime | NOT NULL | Fecha de última actualización |

**Relaciones:**
- ManyToOne con User (barber -> user)
- ManyToOne con Barbershop (barber -> barbershop)
- OneToMany con BarberAvailability (barber -> barberAvailabilities)
- OneToMany con Appointment (barber -> appointments)

### 1.4 BarberAvailability (Disponibilidad del Barbero)
**Tabla:** `barber_availability`

| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| barberAvailabilityId | String (UUID) | PK, NOT NULL | Identificador único |
| barberId | String (UUID) | FK, NOT NULL | Referencia al barbero |
| dayOfWeek | DayOfWeek | NOT NULL | Día de la semana (1-7) |
| startTime | LocalTime | NOT NULL | Hora de inicio |
| endTime | LocalTime | NOT NULL | Hora de fin |
| isAvailable | Boolean | NOT NULL, DEFAULT true | Disponibilidad |
| createdAt | LocalDateTime | NOT NULL | Fecha de creación |
| updatedAt | LocalDateTime | NOT NULL | Fecha de última actualización |

**Relaciones:**
- ManyToOne con Barber (availability -> barber)

### 1.5 Service (Servicio)
**Tabla:** `services`

| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| serviceId | String (UUID) | PK, NOT NULL | Identificador único del servicio |
| barbershopId | String (UUID) | FK, NOT NULL | Referencia a la barbería |
| name | String(100) | NOT NULL | Nombre del servicio |
| description | String | NULL | Descripción del servicio |
| durationMinutes | Integer | NOT NULL | Duración en minutos |
| price | BigDecimal(10,2) | NOT NULL | Precio del servicio |
| isActive | Boolean | NOT NULL, DEFAULT true | Estado activo del servicio |
| createdAt | LocalDateTime | NOT NULL | Fecha de creación |
| updatedAt | LocalDateTime | NOT NULL | Fecha de última actualización |

**Relaciones:**
- ManyToOne con Barbershop (service -> barbershop)
- OneToMany con Appointment (service -> appointments)

### 1.6 Appointment (Cita)
**Tabla:** `appointments`

| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| appointmentId | String (UUID) | PK, NOT NULL | Identificador único de la cita |
| barbershopId | String (UUID) | FK, NOT NULL | Referencia a la barbería |
| clientId | String (UUID) | FK, NOT NULL | Referencia al cliente |
| barberId | String (UUID) | FK, NOT NULL | Referencia al barbero |
| serviceId | String (UUID) | FK, NOT NULL | Referencia al servicio |
| appointmentDatetimeStart | LocalDateTime | NOT NULL | Fecha y hora de inicio |
| appointmentDatetimeEnd | LocalDateTime | NOT NULL | Fecha y hora de fin |
| status | AppointmentStatus | NOT NULL | Estado de la cita |
| notes | String | NULL | Notas adicionales |
| priceAtBooking | BigDecimal(10,2) | NOT NULL | Precio al momento de la reserva |
| createdAt | LocalDateTime | NOT NULL | Fecha de creación |
| updatedAt | LocalDateTime | NOT NULL | Fecha de última actualización |

**Relaciones:**
- ManyToOne con User (appointment -> client)
- ManyToOne con Barber (appointment -> barber)
- ManyToOne con Service (appointment -> service)
- OneToMany con Payment (appointment -> payments)

### 1.7 Payment (Pago)
**Tabla:** `payments`

| Atributo | Tipo | Restricciones | Descripción |
|----------|------|---------------|-------------|
| paymentId | String (UUID) | PK, NOT NULL | Identificador único del pago |
| appointmentId | String (UUID) | FK, NOT NULL | Referencia a la cita |
| amount | BigDecimal(10,2) | NOT NULL | Monto del pago |
| paymentMethod | PaymentMethod | NOT NULL | Método de pago |
| paymentStatus | PaymentStatus | NOT NULL | Estado del pago |
| paymentDate | LocalDateTime | NULL | Fecha del pago |
| createdAt | LocalDateTime | NOT NULL | Fecha de creación |
| updatedAt | LocalDateTime | NOT NULL | Fecha de última actualización |

**Relaciones:**
- ManyToOne con Appointment (payment -> appointment)

## 2. Diseño de Endpoints RESTful

### 2.1 User Endpoints

#### POST /api/v1/users
**Descripción:** Crear un nuevo usuario
**Autenticación:** No requerida (registro público)
**Request Body:**
```json
{
  "email": "usuario@email.com",
  "password": "password123",
  "firstName": "Juan",
  "lastName": "Pérez",
  "phoneNumber": "+1234567890",
  "role": "CLIENT"
}
```
**Response:** 201 Created
```json
{
  "status": 201,
  "message": "Usuario creado exitosamente",
  "data": {
    "userId": "uuid",
    "email": "usuario@email.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "CLIENT",
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/users"
}
```

#### GET /api/v1/users?page=0&size=10&sort=createdAt,desc&role=CLIENT&isActive=true
**Descripción:** Obtener lista de usuarios (paginada)
**Autenticación:** JWT (ADMIN)
**Query Parameters:**
- page (default: 0)
- size (default: 10)
- sort (default: createdAt,desc)
- role (opcional)
- isActive (opcional)

**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Usuarios obtenidos exitosamente",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/users"
}
```

#### GET /api/v1/users?id={userId}
**Descripción:** Obtener usuario por ID
**Autenticación:** JWT (ADMIN o propio usuario)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Usuario obtenido exitosamente",
  "data": {
    "userId": "uuid",
    "email": "usuario@email.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "CLIENT",
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/users"
}
```

#### PUT /api/v1/users?id={userId}
**Descripción:** Actualizar usuario completo
**Autenticación:** JWT (ADMIN o propio usuario)
**Request Body:** Usuario completo
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Usuario actualizado exitosamente",
  "data": {
    "userId": "uuid",
    "email": "usuario@email.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "CLIENT",
    "isActive": true,
    "updatedAt": "2024-01-01T10:30:00"
  },
  "timestamp": "2024-01-01T10:30:00",
  "path": "/api/v1/users"
}
```

#### PATCH /api/v1/users?id={userId}
**Descripción:** Actualización parcial de usuario
**Autenticación:** JWT (ADMIN o propio usuario)
**Request Body:** Campos a actualizar
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Usuario actualizado parcialmente",
  "data": {
    "userId": "uuid",
    "firstName": "Juan Carlos",
    "updatedAt": "2024-01-01T10:45:00"
  },
  "timestamp": "2024-01-01T10:45:00",
  "path": "/api/v1/users"
}
```

#### DELETE /api/v1/users?id={userId} (Soft Delete)
**Descripción:** Desactivar usuario (soft delete)
**Autenticación:** JWT (ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Usuario desactivado exitosamente",
  "data": null,
  "timestamp": "2024-01-01T11:00:00",
  "path": "/api/v1/users"
}
```

### 2.2 Barbershop Endpoints

#### POST /api/v1/barbershops
**Descripción:** Crear nueva barbería
**Autenticación:** JWT (ADMIN)
**Request Body:**
```json
{
  "name": "Barbería El Corte",
  "addressText": "Calle 123 #45-67",
  "phoneNumber": "+1234567890",
  "email": "contacto@barberia.com",
  "operatingHours": {
    "monday": {"open": "08:00", "close": "18:00"},
    "tuesday": {"open": "08:00", "close": "18:00"}
  }
}
```

#### GET /api/v1/barbershops?page=0&size=10&sort=name,asc&name=Barbería
**Descripción:** Obtener lista de barberías
**Autenticación:** No requerida
**Query Parameters:** page, size, sort, name
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Barberías obtenidas exitosamente",
  "data": {
    "content": [...],
    "totalElements": 50,
    "totalPages": 5,
    "size": 10,
    "number": 0
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/barbershops"
}
```

#### GET /api/v1/barbershops?id={barbershopId}
**Descripción:** Obtener barbería por ID
**Autenticación:** No requerida
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Barbería obtenida exitosamente",
  "data": {
    "barbershopId": "uuid",
    "name": "Barbería El Corte",
    "addressText": "Calle 123 #45-67",
    "phoneNumber": "+1234567890",
    "email": "contacto@barberia.com",
    "operatingHours": {...},
    "createdAt": "2024-01-01T08:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/barbershops"
}
```

#### PUT /api/v1/barbershops?id={barbershopId}
**Descripción:** Actualizar barbería completa
**Autenticación:** JWT (ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Barbería actualizada exitosamente",
  "data": {
    "barbershopId": "uuid",
    "name": "Barbería El Corte Renovado",
    "addressText": "Calle 123 #45-67",
    "updatedAt": "2024-01-01T11:00:00"
  },
  "timestamp": "2024-01-01T11:00:00",
  "path": "/api/v1/barbershops"
}
```

#### PATCH /api/v1/barbershops?id={barbershopId}
**Descripción:** Actualización parcial de barbería
**Autenticación:** JWT (ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Barbería actualizada parcialmente",
  "data": {
    "barbershopId": "uuid",
    "name": "Nuevo Nombre",
    "updatedAt": "2024-01-01T11:15:00"
  },
  "timestamp": "2024-01-01T11:15:00",
  "path": "/api/v1/barbershops"
}
```

#### DELETE /api/v1/barbershops?id={barbershopId}
**Descripción:** Eliminar barbería
**Autenticación:** JWT (ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Barbería eliminada exitosamente",
  "data": null,
  "timestamp": "2024-01-01T11:30:00",
  "path": "/api/v1/barbershops"
}
```

### 2.3 Barber Endpoints

#### POST /api/v1/barbers
**Descripción:** Crear nuevo barbero
**Autenticación:** JWT (ADMIN)
**Request Body:**
```json
{
  "userId": "user-uuid",
  "barbershopId": "barbershop-uuid",
  "specialization": "Cortes clásicos y modernos"
}
```
**Response:** 201 Created
```json
{
  "status": 201,
  "message": "Barbero creado exitosamente",
  "data": {
    "barberId": "uuid",
    "userId": "user-uuid",
    "barbershopId": "barbershop-uuid",
    "specialization": "Cortes clásicos y modernos",
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/barbers"
}
```

#### GET /api/v1/barbers?page=0&size=10&barbershopId=uuid&isActive=true
**Descripción:** Obtener lista de barberos
**Autenticación:** No requerida
**Query Parameters:** page, size, barbershopId, isActive
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Barberos obtenidos exitosamente",
  "data": {
    "content": [...],
    "totalElements": 25,
    "totalPages": 3,
    "size": 10,
    "number": 0
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/barbers"
}
```

#### GET /api/v1/barbers?id={barberId}
**Descripción:** Obtener barbero por ID
**Autenticación:** No requerida
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Barbero obtenido exitosamente",
  "data": {
    "barberId": "uuid",
    "userId": "user-uuid",
    "barbershopId": "barbershop-uuid",
    "specialization": "Cortes clásicos y modernos",
    "isActive": true,
    "createdAt": "2024-01-01T08:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/barbers"
}
```

#### PUT /api/v1/barbers?id={barberId}
**Descripción:** Actualizar barbero completo
**Autenticación:** JWT (ADMIN o propio barbero)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Barbero actualizado exitosamente",
  "data": {
    "barberId": "uuid",
    "specialization": "Cortes modernos y barba",
    "updatedAt": "2024-01-01T11:00:00"
  },
  "timestamp": "2024-01-01T11:00:00",
  "path": "/api/v1/barbers"
}
```

#### PATCH /api/v1/barbers?id={barberId}
**Descripción:** Actualización parcial de barbero
**Autenticación:** JWT (ADMIN o propio barbero)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Barbero actualizado parcialmente",
  "data": {
    "barberId": "uuid",
    "specialization": "Nueva especialización",
    "updatedAt": "2024-01-01T11:15:00"
  },
  "timestamp": "2024-01-01T11:15:00",
  "path": "/api/v1/barbers"
}
```

#### DELETE /api/v1/barbers?id={barberId} (Soft Delete)
**Descripción:** Desactivar barbero
**Autenticación:** JWT (ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Barbero desactivado exitosamente",
  "data": null,
  "timestamp": "2024-01-01T11:30:00",
  "path": "/api/v1/barbers"
}
```

### 2.4 BarberAvailability Endpoints

#### POST /api/v1/barber-availability
**Descripción:** Crear disponibilidad de barbero
**Autenticación:** JWT (ADMIN o BARBER)
**Request Body:**
```json
{
  "barberId": "barber-uuid",
  "dayOfWeek": "MONDAY",
  "startTime": "08:00:00",
  "endTime": "17:00:00",
  "isAvailable": true
}
```
**Response:** 201 Created
```json
{
  "status": 201,
  "message": "Disponibilidad creada exitosamente",
  "data": {
    "barberAvailabilityId": "uuid",
    "barberId": "barber-uuid",
    "dayOfWeek": "MONDAY",
    "startTime": "08:00:00",
    "endTime": "17:00:00",
    "isAvailable": true,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/barber-availability"
}
```

#### GET /api/v1/barber-availability?barberId=uuid&dayOfWeek=MONDAY&isAvailable=true
**Descripción:** Obtener disponibilidades
**Query Parameters:** barberId, dayOfWeek, isAvailable
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Disponibilidades obtenidas exitosamente",
  "data": [
    {
      "barberAvailabilityId": "uuid",
      "barberId": "barber-uuid",
      "dayOfWeek": "MONDAY",
      "startTime": "08:00:00",
      "endTime": "17:00:00",
      "isAvailable": true
    }
  ],
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/barber-availability"
}
```

#### GET /api/v1/barber-availability?id={availabilityId}
**Descripción:** Obtener disponibilidad por ID
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Disponibilidad obtenida exitosamente",
  "data": {
    "barberAvailabilityId": "uuid",
    "barberId": "barber-uuid",
    "dayOfWeek": "MONDAY",
    "startTime": "08:00:00",
    "endTime": "17:00:00",
    "isAvailable": true,
    "createdAt": "2024-01-01T08:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/barber-availability"
}
```

#### PUT /api/v1/barber-availability?id={availabilityId}
**Descripción:** Actualizar disponibilidad completa
**Autenticación:** JWT (ADMIN o BARBER propietario)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Disponibilidad actualizada exitosamente",
  "data": {
    "barberAvailabilityId": "uuid",
    "startTime": "09:00:00",
    "endTime": "18:00:00",
    "updatedAt": "2024-01-01T11:00:00"
  },
  "timestamp": "2024-01-01T11:00:00",
  "path": "/api/v1/barber-availability"
}
```

#### PATCH /api/v1/barber-availability?id={availabilityId}
**Descripción:** Actualización parcial de disponibilidad
**Autenticación:** JWT (ADMIN o BARBER propietario)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Disponibilidad actualizada parcialmente",
  "data": {
    "barberAvailabilityId": "uuid",
    "isAvailable": false,
    "updatedAt": "2024-01-01T11:15:00"
  },
  "timestamp": "2024-01-01T11:15:00",
  "path": "/api/v1/barber-availability"
}
```

#### DELETE /api/v1/barber-availability?id={availabilityId}
**Descripción:** Eliminar disponibilidad
**Autenticación:** JWT (ADMIN o BARBER propietario)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Disponibilidad eliminada exitosamente",
  "data": null,
  "timestamp": "2024-01-01T11:30:00",
  "path": "/api/v1/barber-availability"
}
```

### 2.5 Service Endpoints

#### POST /api/v1/services
**Descripción:** Crear nuevo servicio
**Autenticación:** JWT (ADMIN)
**Request Body:**
```json
{
  "barbershopId": "barbershop-uuid",
  "name": "Corte de cabello",
  "description": "Corte profesional con lavado",
  "durationMinutes": 30,
  "price": 25000.00
}
```
**Response:** 201 Created
```json
{
  "status": 201,
  "message": "Servicio creado exitosamente",
  "data": {
    "serviceId": "uuid",
    "barbershopId": "barbershop-uuid",
    "name": "Corte de cabello",
    "description": "Corte profesional con lavado",
    "durationMinutes": 30,
    "price": 25000.00,
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/services"
}
```

#### GET /api/v1/services?page=0&size=10&barbershopId=uuid&isActive=true&name=Corte
**Descripción:** Obtener lista de servicios
**Query Parameters:** page, size, barbershopId, isActive, name
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Servicios obtenidos exitosamente",
  "data": {
    "content": [...],
    "totalElements": 15,
    "totalPages": 2,
    "size": 10,
    "number": 0
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/services"
}
```

#### GET /api/v1/services?id={serviceId}
**Descripción:** Obtener servicio por ID
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Servicio obtenido exitosamente",
  "data": {
    "serviceId": "uuid",
    "barbershopId": "barbershop-uuid",
    "name": "Corte de cabello",
    "description": "Corte profesional con lavado",
    "durationMinutes": 30,
    "price": 25000.00,
    "isActive": true,
    "createdAt": "2024-01-01T08:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/services"
}
```

#### PUT /api/v1/services?id={serviceId}
**Descripción:** Actualizar servicio completo
**Autenticación:** JWT (ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Servicio actualizado exitosamente",
  "data": {
    "serviceId": "uuid",
    "name": "Corte y Barba",
    "description": "Corte profesional con arreglo de barba",
    "durationMinutes": 45,
    "price": 35000.00,
    "updatedAt": "2024-01-01T11:00:00"
  },
  "timestamp": "2024-01-01T11:00:00",
  "path": "/api/v1/services"
}
```

#### PATCH /api/v1/services?id={serviceId}
**Descripción:** Actualización parcial de servicio
**Autenticación:** JWT (ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Servicio actualizado parcialmente",
  "data": {
    "serviceId": "uuid",
    "price": 30000.00,
    "updatedAt": "2024-01-01T11:15:00"
  },
  "timestamp": "2024-01-01T11:15:00",
  "path": "/api/v1/services"
}
```

#### DELETE /api/v1/services?id={serviceId} (Soft Delete)
**Descripción:** Desactivar servicio
**Autenticación:** JWT (ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Servicio desactivado exitosamente",
  "data": null,
  "timestamp": "2024-01-01T11:30:00",
  "path": "/api/v1/services"
}
```

### 2.6 Appointment Endpoints

#### POST /api/v1/appointments
**Descripción:** Crear nueva cita
**Autenticación:** JWT (CLIENT, BARBER, ADMIN)
**Request Body:**
```json
{
  "barbershopId": "barbershop-uuid",
  "clientId": "client-uuid",
  "barberId": "barber-uuid",
  "serviceId": "service-uuid",
  "appointmentDatetimeStart": "2024-01-15T10:00:00",
  "notes": "Corte específico solicitado"
}
```
**Response:** 201 Created
```json
{
  "status": 201,
  "message": "Cita creada exitosamente",
  "data": {
    "appointmentId": "uuid",
    "barbershopId": "barbershop-uuid",
    "clientId": "client-uuid",
    "barberId": "barber-uuid",
    "serviceId": "service-uuid",
    "appointmentDatetimeStart": "2024-01-15T10:00:00",
    "appointmentDatetimeEnd": "2024-01-15T10:30:00",
    "status": "SCHEDULED",
    "notes": "Corte específico solicitado",
    "priceAtBooking": 25000.00,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/appointments"
}
```

#### GET /api/v1/appointments?page=0&size=10&clientId=uuid&barberId=uuid&status=SCHEDULED&date=2024-01-15
**Descripción:** Obtener lista de citas
**Autenticación:** JWT
**Query Parameters:** page, size, clientId, barberId, status, date
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Citas obtenidas exitosamente",
  "data": {
    "content": [...],
    "totalElements": 30,
    "totalPages": 3,
    "size": 10,
    "number": 0
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/appointments"
}
```

#### GET /api/v1/appointments?id={appointmentId}
**Descripción:** Obtener cita por ID
**Autenticación:** JWT (participantes de la cita o ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Cita obtenida exitosamente",
  "data": {
    "appointmentId": "uuid",
    "barbershopId": "barbershop-uuid",
    "clientId": "client-uuid",
    "barberId": "barber-uuid",
    "serviceId": "service-uuid",
    "appointmentDatetimeStart": "2024-01-15T10:00:00",
    "appointmentDatetimeEnd": "2024-01-15T10:30:00",
    "status": "SCHEDULED",
    "notes": "Corte específico solicitado",
    "priceAtBooking": 25000.00,
    "createdAt": "2024-01-01T08:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/appointments"
}
```

#### PUT /api/v1/appointments?id={appointmentId}
**Descripción:** Actualizar cita completa
**Autenticación:** JWT (participantes de la cita o ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Cita actualizada exitosamente",
  "data": {
    "appointmentId": "uuid",
    "appointmentDatetimeStart": "2024-01-15T11:00:00",
    "appointmentDatetimeEnd": "2024-01-15T11:30:00",
    "notes": "Cambio de horario solicitado",
    "updatedAt": "2024-01-01T11:00:00"
  },
  "timestamp": "2024-01-01T11:00:00",
  "path": "/api/v1/appointments"
}
```

#### PATCH /api/v1/appointments?id={appointmentId}
**Descripción:** Actualización parcial de cita
**Autenticación:** JWT (participantes de la cita o ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Cita actualizada parcialmente",
  "data": {
    "appointmentId": "uuid",
    "status": "CONFIRMED",
    "updatedAt": "2024-01-01T11:15:00"
  },
  "timestamp": "2024-01-01T11:15:00",
  "path": "/api/v1/appointments"
}
```

#### DELETE /api/v1/appointments?id={appointmentId}
**Descripción:** Cancelar cita (cambiar status a CANCELLED)
**Autenticación:** JWT (participantes de la cita o ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Cita cancelada exitosamente",
  "data": {
    "appointmentId": "uuid",
    "status": "CANCELLED",
    "updatedAt": "2024-01-01T11:30:00"
  },
  "timestamp": "2024-01-01T11:30:00",
  "path": "/api/v1/appointments"
}
```

### 2.7 Payment Endpoints

#### POST /api/v1/payments
**Descripción:** Crear nuevo pago
**Autenticación:** JWT (CLIENT, BARBER, ADMIN)
**Request Body:**
```json
{
  "appointmentId": "appointment-uuid",
  "amount": 25000.00,
  "paymentMethod": "CREDIT_CARD"
}
```
**Response:** 201 Created
```json
{
  "status": 201,
  "message": "Pago creado exitosamente",
  "data": {
    "paymentId": "uuid",
    "appointmentId": "appointment-uuid",
    "amount": 25000.00,
    "paymentMethod": "CREDIT_CARD",
    "paymentStatus": "PENDING",
    "paymentDate": null,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/payments"
}
```

#### GET /api/v1/payments?page=0&size=10&appointmentId=uuid&paymentStatus=COMPLETED
**Descripción:** Obtener lista de pagos
**Autenticación:** JWT (ADMIN o propios pagos)
**Query Parameters:** page, size, appointmentId, paymentStatus
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Pagos obtenidos exitosamente",
  "data": {
    "content": [...],
    "totalElements": 20,
    "totalPages": 2,
    "size": 10,
    "number": 0
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/payments"
}
```

#### GET /api/v1/payments?id={paymentId}
**Descripción:** Obtener pago por ID
**Autenticación:** JWT (propietario del pago o ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Pago obtenido exitosamente",
  "data": {
    "paymentId": "uuid",
    "appointmentId": "appointment-uuid",
    "amount": 25000.00,
    "paymentMethod": "CREDIT_CARD",
    "paymentStatus": "COMPLETED",
    "paymentDate": "2024-01-15T10:30:00",
    "createdAt": "2024-01-01T08:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/payments"
}
```

#### PUT /api/v1/payments?id={paymentId}
**Descripción:** Actualizar pago completo
**Autenticación:** JWT (ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Pago actualizado exitosamente",
  "data": {
    "paymentId": "uuid",
    "amount": 30000.00,
    "paymentMethod": "CASH",
    "paymentStatus": "COMPLETED",
    "paymentDate": "2024-01-15T11:00:00",
    "updatedAt": "2024-01-01T11:00:00"
  },
  "timestamp": "2024-01-01T11:00:00",
  "path": "/api/v1/payments"
}
```

#### PATCH /api/v1/payments?id={paymentId}
**Descripción:** Actualización parcial de pago
**Autenticación:** JWT (ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Pago actualizado parcialmente",
  "data": {
    "paymentId": "uuid",
    "paymentStatus": "COMPLETED",
    "paymentDate": "2024-01-15T11:15:00",
    "updatedAt": "2024-01-01T11:15:00"
  },
  "timestamp": "2024-01-01T11:15:00",
  "path": "/api/v1/payments"
}
```

#### DELETE /api/v1/payments?id={paymentId}
**Descripción:** Eliminar pago (solo si está en estado PENDING)
**Autenticación:** JWT (ADMIN)
**Response:** 200 OK
```json
{
  "status": 200,
  "message": "Pago eliminado exitosamente",
  "data": null,
  "timestamp": "2024-01-01T11:30:00",
  "path": "/api/v1/payments"
}
```

## 3. Diagrama de Base de Datos

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     USERS       │    │   BARBERSHOPS   │    │    BARBERS      │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ user_id (PK)    │◄──┐│ barbershop_id   │◄──┐│ barber_id (PK)  │
│ email (UQ)      │   ││ (PK)            │   ││ user_id (FK)    │
│ password_hash   │   ││ name            │   ││ barbershop_id   │
│ role            │   ││ address_text    │   ││ (FK)            │
│ first_name      │   ││ phone_number    │   ││ specialization  │
│ last_name       │   ││ email           │   ││ is_active       │
│ phone_number    │   ││ operating_hours │   ││ created_at      │
│ is_active       │   ││ logo_url        │   ││ updated_at      │
│ profile_picture │   ││ created_at      │   │└─────────────────┘
│ created_at      │   ││ updated_at      │   │         │
│ updated_at      │   │└─────────────────┘   │         │
└─────────────────┘   │                      │         ▼
         │             │                      │┌─────────────────┐
         │             │                      ││BARBER_AVAILABIL.│
         │             │                      │├─────────────────┤
         │             │                      ││ availability_id │
         │             │                      ││ (PK)            │
         │             │                      ││ barber_id (FK)  │
         │             │                      ││ day_of_week     │
         │             │                      ││ start_time      │
         │             │                      ││ end_time        │
         │             │                      ││ is_available    │
         │             │                      ││ created_at      │
         │             │                      ││ updated_at      │
         │             │                      │└─────────────────┘
         │             │                      │
         │             │              ┌─────────────────┐
         │             │              │    SERVICES     │
         │             │              ├─────────────────┤
         │             │              │ service_id (PK) │
         │             └─────────────►│ barbershop_id   │
         │                            │ (FK)            │
         │                            │ name            │
         │                            │ description     │
         │                            │ duration_minutes│
         │                            │ price           │
         │                            │ is_active       │
         │                            │ created_at      │
         │                            │ updated_at      │
         │                            └─────────────────┘
         │                                     │
         │                                     │
         ▼                                     ▼
┌─────────────────┐                  ┌─────────────────┐
│  APPOINTMENTS   │                  │    PAYMENTS     │
├─────────────────┤                  ├─────────────────┤
│ appointment_id  │◄─────────────────┤ payment_id (PK) │
│ (PK)            │                  │ appointment_id  │
│ barbershop_id   │                  │ (FK)            │
│ client_id (FK)  │                  │ amount          │
│ barber_id (FK)  │                  │ payment_method  │
│ service_id (FK) │                  │ payment_status  │
│ datetime_start  │                  │ payment_date    │
│ datetime_end    │                  │ created_at      │
│ status          │                  │ updated_at      │
│ notes           │                  └─────────────────┘
│ price_at_booking│
│ created_at      │
│ updated_at      │
└─────────────────┘
```

## 4. Validaciones de Datos

### 4.1 User Validations
```java
public class CreateUserDto {
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "La contraseña debe contener al menos una minúscula, una mayúscula y un número")
    private String password;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String firstName;
    
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres")
    private String lastName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Formato de teléfono inválido")
    private String phoneNumber;
    
    @NotNull(message = "El rol es obligatorio")
    @ValidEnum(enumClass = RoleEnum.class, message = "Rol inválido")
    private String role;
}
```

### 4.2 Appointment Validations
```java
public class CreateAppointmentDto {
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha de la cita debe ser futura")
    private LocalDateTime appointmentDatetimeStart;
    
    @NotBlank(message = "El ID del barbero es obligatorio")
    @ValidUUID(message = "Formato de UUID inválido para barbero")
    private String barberId;
    
    @NotBlank(message = "El ID del servicio es obligatorio")
    @ValidUUID(message = "Formato de UUID inválido para servicio")
    private String serviceId;
    
    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;
}
```

### 4.3 Custom Validators
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UUIDValidator.class)
public @interface ValidUUID {
    String message() default "Formato de UUID inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
public @interface ValidEnum {
    String message() default "Valor de enum inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<? extends Enum<?>> enumClass();
}
```

## 5. Manejo de Errores

### 5.1 Códigos de Estado HTTP

| Código | Descripción | Uso |
|--------|-------------|-----|
| 200 | OK | Operación exitosa (GET, PUT, PATCH) |
| 201 | Created | Recurso creado exitosamente (POST) |
| 204 | No Content | Eliminación exitosa (DELETE) |
| 400 | Bad Request | Datos de entrada inválidos |
| 401 | Unauthorized | No autenticado |
| 403 | Forbidden | No autorizado para la operación |
| 404 | Not Found | Recurso no encontrado |
| 409 | Conflict | Conflicto (ej: email duplicado) |
| 422 | Unprocessable Entity | Errores de validación |
| 500 | Internal Server Error | Error interno del servidor |

### 5.2 Estructura de Respuesta de Error
Todas las respuestas de error seguirán el formato de `ApiResponseDto`:

#### Error de Validación (422 Unprocessable Entity)
```json
{
  "status": 422,
  "message": "Error de validación en los datos enviados",
  "data": null,
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/users",
  "errors": {
    "email": "El formato del email no es válido",
    "password": "La contraseña debe tener entre 8 y 100 caracteres",
    "firstName": "El nombre es obligatorio"
  }
}
```

#### Error de Recurso No Encontrado (404 Not Found)
```json
{
  "status": 404,
  "message": "Usuario no encontrado",
  "data": null,
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/users",
  "errors": null
}
```

#### Error de Autenticación (401 Unauthorized)
```json
{
  "status": 401,
  "message": "Token de autenticación inválido o expirado",
  "data": null,
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/users",
  "errors": null
}
```

#### Error de Autorización (403 Forbidden)
```json
{
  "status": 403,
  "message": "No tienes permisos para realizar esta operación",
  "data": null,
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/users",
  "errors": null
}
```

#### Error de Conflicto (409 Conflict)
```json
{
  "status": 409,
  "message": "El email ya está registrado en el sistema",
  "data": null,
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/users",
  "errors": {
    "email": "Este email ya está en uso"
  }
}
```

#### Error Interno del Servidor (500 Internal Server Error)
```json
{
  "status": 500,
  "message": "Error interno del servidor",
  "data": null,
  "timestamp": "2024-01-01T10:00:00",
  "path": "/api/v1/users",
  "errors": null
}
```

### 5.2.1 Mejora del Campo de Errores
El campo `errors` en `ApiResponseDto` se mejorará para manejar errores de validación más detallados:

```java
@Schema(description = "Detalles específicos de errores, como errores de validación de campos")
private Map<String, Object> errors;
```

Esto permitirá incluir tanto errores simples como objetos `FieldError` más complejos:

```json
{
  "errors": {
    "email": "El formato del email no es válido",
    "password": {
      "field": "password",
      "rejectedValue": "123",
      "message": "La contraseña debe tener entre 8 y 100 caracteres",
      "code": "Size"
    },
    "phoneNumber": {
      "field": "phoneNumber",
      "rejectedValue": "invalid-phone",
      "message": "Formato de teléfono inválido",
      "code": "Pattern"
    }
  }
}
```

### 5.3 Global Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiResponseDto<Object> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        });
            
        return ApiResponseDto.builder()
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .message("Error de validación en los datos enviados")
            .data(null)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .errors(fieldErrors)
            .build();
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponseDto<Object> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ApiResponseDto.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .data(null)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .errors(null)
            .build();
    }
    
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponseDto<Object> handleResourceAlreadyExists(ResourceAlreadyExistsException ex, HttpServletRequest request) {
        return ApiResponseDto.builder()
            .status(HttpStatus.CONFLICT.value())
            .message(ex.getMessage())
            .data(null)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .errors(null)
            .build();
    }
    
    @ExceptionHandler(BusinessLogicException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDto<Object> handleBusinessLogic(BusinessLogicException ex, HttpServletRequest request) {
        return ApiResponseDto.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(ex.getMessage())
            .data(null)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .errors(null)
            .build();
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponseDto<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ApiResponseDto.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .message("No tienes permisos para realizar esta operación")
            .data(null)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .errors(null)
            .build();
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseDto<Object> handleGenericError(Exception ex, HttpServletRequest request) {
        return ApiResponseDto.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("Ha ocurrido un error interno del servidor")
            .data(null)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .errors(null)
            .build();
    }
}
```

## 6. Seguridad y Autenticación

### 6.1 Configuración JWT
```java
@Component
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration}")
    private int jwtExpirationInMs;
    
    public String generateToken(UserDetails userDetails) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
            .setIssuedAt(new Date())
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
}
```

### 6.2 Control de Acceso por Roles
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/barbershops/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/services/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/barbers/**").permitAll()
                
                // Endpoints de administrador
                .requestMatchers(HttpMethod.POST, "/api/v1/barbershops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/barbershops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/barbershops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasRole("ADMIN")
                
                // Endpoints de barbero
                .requestMatchers(HttpMethod.POST, "/api/v1/barber-availability/**").hasAnyRole("ADMIN", "BARBER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/barber-availability/**").hasAnyRole("ADMIN", "BARBER")
                
                // Endpoints de cliente
                .requestMatchers(HttpMethod.POST, "/api/v1/appointments/**").hasAnyRole("CLIENT", "BARBER", "ADMIN")
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}
```

### 6.3 Autorización a Nivel de Método
```java
@Service
public class AppointmentService {
    
    @PreAuthorize("hasRole('ADMIN') or #clientId == authentication.principal.userId")
    public List<AppointmentDto> getAppointmentsByClient(String clientId) {
        // Implementación
    }
    
    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isOwnerOrParticipant(#appointmentId, authentication.principal.userId)")
    public AppointmentDto updateAppointment(String appointmentId, UpdateAppointmentDto dto) {
        // Implementación
    }
    
    public boolean isOwnerOrParticipant(String appointmentId, String userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        return appointment.getClientId().equals(userId) || 
               appointment.getBarber().getUserId().equals(userId);
    }
}
```

## 7. Documentación API

### 7.1 Configuración Swagger/OpenAPI
```java
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Barbershop Management API",
        version = "1.0",
        description = "API para la gestión de barberías, citas y servicios",
        contact = @Contact(
            name = "Equipo de Desarrollo",
            email = "dev@barbershop.com"
        )
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class SwaggerConfig {
}
```

### 7.2 Documentación de Endpoints
```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Gestión de usuarios del sistema")
public class UserController {
    
    @Operation(
        summary = "Crear nuevo usuario",
        description = "Registra un nuevo usuario en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuario creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "El email ya está registrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponseDto<UserDto>> createUser(
        @Valid @RequestBody CreateUserDto createUserDto
    ) {
        // Implementación
    }
    
    @Operation(
        summary = "Obtener usuario por ID",
        description = "Retorna la información de un usuario específico"
    )
    @Parameter(
        name = "userId",
        description = "ID único del usuario",
        required = true,
        schema = @Schema(type = "string", format = "uuid")
    )
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ApiResponseDto<UserDto>> getUserById(
        @PathVariable String userId
    ) {
        // Implementación
    }
}
```

### 7.3 Ejemplos de Request/Response

#### Crear Usuario
**Request:**
```json
POST /api/v1/users
Content-Type: application/json

{
  "email": "juan.perez@email.com",
  "password": "MiPassword123",
  "firstName": "Juan",
  "lastName": "Pérez",
  "phoneNumber": "+573001234567",
  "role": "CLIENT"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Usuario creado exitosamente",
  "data": {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "email": "juan.perez@email.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "phoneNumber": "+573001234567",
    "role": "CLIENT",
    "isActive": true,
    "profilePictureUrl": null,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

#### Error de Validación
**Response (422 Unprocessable Entity):**
```json
{
  "success": false,
  "message": "Errores de validación en los datos enviados",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/v1/users",
  "fieldErrors": [
    {
      "field": "email",
      "message": "El formato del email no es válido",
      "rejectedValue": "email-invalido"
    },
    {
      "field": "password",
      "message": "La contraseña debe tener entre 8 y 100 caracteres",
      "rejectedValue": "123"
    }
  ]
}
```

## 8. Implementación por Fases

### Fase 1: Entidades Base (Semana 1-2)
1. **User Management**
   - Implementar CRUD completo de usuarios
   - Configurar autenticación JWT
   - Implementar validaciones y manejo de errores
   - Pruebas unitarias e integración

2. **Barbershop Management**
   - Implementar CRUD de barberías
   - Configurar relaciones con usuarios
   - Documentación API

### Fase 2: Servicios y Barberos (Semana 3-4)
1. **Barber Management**
   - Implementar CRUD de barberos
   - Configurar relaciones con usuarios y barberías
   - Implementar BarberAvailability CRUD

2. **Service Management**
   - Implementar CRUD de servicios
   - Configurar relaciones con barberías
   - Validaciones de negocio

### Fase 3: Citas y Pagos (Semana 5-6)
1. **Appointment Management**
   - Implementar CRUD de citas
   - Lógica de validación de disponibilidad
   - Estados de citas y transiciones

2. **Payment Management**
   - Implementar CRUD de pagos
   - Integración con citas
   - Estados de pago

### Fase 4: Optimización y Testing (Semana 7-8)
1. **Performance y Seguridad**
   - Optimización de consultas
   - Implementación de caché
   - Auditoría de seguridad

2. **Testing y Documentación**
   - Pruebas de integración completas
   - Documentación final
   - Preparación para producción

## 9. Consideraciones Técnicas

### 9.1 Paginación Estándar
```java
@GetMapping
public ResponseEntity<ApiResponseDto<Page<UserDto>>> getUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "createdAt,desc") String[] sort
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(createSortOrders(sort)));
    Page<UserDto> users = userService.getAllUsers(pageable);
    return ResponseEntity.ok(ApiResponseDto.success(users));
}
```

### 9.2 Soft Delete Implementation
```java
@Entity
@SQLDelete(sql = "UPDATE users SET is_active = false WHERE user_id = ?")
@Where(clause = "is_active = true")
public class User {
    // Campos de la entidad
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}
```

### 9.3 Auditoría
```java
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseEntity {
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
}
```

## 10. Checklist de Implementación

### ✅ Preparación
- [ ] Revisar y aprobar especificaciones de entidades
- [ ] Configurar entorno de desarrollo
- [ ] Configurar base de datos PostgreSQL
- [ ] Configurar herramientas de testing

### ✅ Desarrollo por Entidad
Para cada entidad:
- [ ] Crear modelo JPA con validaciones
- [ ] Crear DTOs (Create, Update, Response)
- [ ] Implementar Repository con consultas personalizadas
- [ ] Implementar Service con lógica de negocio
- [ ] Implementar Controller con endpoints RESTful
- [ ] Configurar MapStruct mappers
- [ ] Implementar validaciones personalizadas
- [ ] Configurar seguridad y autorización
- [ ] Documentar endpoints con OpenAPI
- [ ] Escribir pruebas unitarias
- [ ] Escribir pruebas de integración

### ✅ Testing y Calidad
- [ ] Pruebas de endpoints con diferentes roles
- [ ] Pruebas de validación de datos
- [ ] Pruebas de manejo de errores
- [ ] Pruebas de rendimiento básicas
- [ ] Revisión de código
- [ ] Análisis de cobertura de pruebas

### ✅ Documentación
- [ ] Documentación API completa
- [ ] Ejemplos de uso
- [ ] Guía de instalación
- [ ] Guía de despliegue

### ✅ Despliegue
- [ ] Configuración de perfiles de entorno
- [ ] Scripts de migración de base de datos
- [ ] Configuración de Docker
- [ ] Configuración de CI/CD
- [ ] Monitoreo y logging

---

**Nota:** Este plan está sujeto a revisión y aprobación. Una vez aprobado, se procederá con la implementación siguiendo las fases establecidas y manteniendo los estándares de calidad definidos.