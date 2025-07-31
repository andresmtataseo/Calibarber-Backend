# Mejores Prácticas de Configuración

## Configuración de Autenticación

### Refactorización de Variables Sensibles

Se ha refactorizado el sistema de configuración para seguir las mejores prácticas de seguridad, moviendo todas las variables sensibles desde código hardcodeado hacia archivos de configuración.

### Cambios Implementados

#### 1. AuthProperties.java
- **Antes**: Variables con valores por defecto hardcodeados
- **Después**: Variables sin valores por defecto, configuradas desde `application.properties`

```java
// ANTES (❌ Mala práctica)
private String secretKey = "defaultSecretKey";
private long expirationTime = 86400000L;

// DESPUÉS (✅ Buena práctica)
private String secretKey;  // Configurado desde properties
private long expirationTime;  // Configurado desde properties
```

#### 2. JwtService.java
- **Antes**: Uso de `@Value` para configuraciones
- **Después**: Uso de `AuthProperties` centralizado

```java
// ANTES (❌ Mala práctica)
@Value("${jwt.secret}")
private String SECRET_KEY;

// DESPUÉS (✅ Buena práctica)
private final AuthProperties authProperties;
// Uso: authProperties.getJwt().getSecretKey()
```

### Configuraciones por Ambiente

#### Desarrollo (`application-dev.properties`)
```properties
# Configuración de Autenticación
app.auth.jwt.secret-key=UnSecretoSimpleParaDesarrolloNoUsarEnProduccion12345
app.auth.jwt.expiration-time=86400000
app.auth.jwt.issuer=Calibarber-Backend
app.auth.reset-token.expiration-time=3600000
app.auth.reset-token.token-length=32
```

#### Producción (`application-prod.properties`)
```properties
# Configuración de Autenticación (desde variables de entorno)
app.auth.jwt.secret-key=${JWT_SECRET}
app.auth.jwt.expiration-time=${JWT_EXPIRATION_TIME:86400000}
app.auth.jwt.issuer=${JWT_ISSUER:Calibarber-Backend}
app.auth.reset-token.expiration-time=${RESET_TOKEN_EXPIRATION_TIME:3600000}
app.auth.reset-token.token-length=${RESET_TOKEN_LENGTH:32}
```

### Variables de Entorno para Producción

Para el ambiente de producción, configure las siguientes variables de entorno:

```bash
# JWT Configuration
JWT_SECRET=tu_clave_secreta_super_segura_de_al_menos_256_bits
JWT_EXPIRATION_TIME=86400000  # 24 horas en milisegundos
JWT_ISSUER=Calibarber-Backend

# Reset Token Configuration
RESET_TOKEN_EXPIRATION_TIME=3600000  # 1 hora en milisegundos
RESET_TOKEN_LENGTH=32
```

### Beneficios de esta Implementación

1. **Seguridad**: No hay secretos hardcodeados en el código
2. **Flexibilidad**: Diferentes configuraciones por ambiente
3. **Mantenibilidad**: Configuración centralizada en `AuthProperties`
4. **Escalabilidad**: Fácil adición de nuevas configuraciones
5. **Buenas Prácticas**: Sigue los estándares de Spring Boot

### Recomendaciones Adicionales

1. **Generación de Secretos JWT**:
   ```bash
   # Generar una clave segura de 256 bits
   openssl rand -base64 32
   ```

2. **Rotación de Claves**: Implementar rotación periódica de claves JWT

3. **Validación**: Agregar validaciones para asegurar que las configuraciones críticas estén presentes

4. **Logging**: No loggear nunca las claves secretas en los logs

### Próximos Pasos

1. Implementar validación de configuraciones al inicio de la aplicación
2. Agregar métricas para monitorear el uso de tokens
3. Implementar refresh tokens para mayor seguridad
4. Considerar el uso de Spring Cloud Config para configuración centralizada