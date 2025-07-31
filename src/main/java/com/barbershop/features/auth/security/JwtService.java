package com.barbershop.features.auth.security;

import com.barbershop.features.auth.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final AuthProperties authProperties;

    /**
     * Genera un token JWT para el usuario
     * @param user Detalles del usuario
     * @return Token JWT
     */
    public String getToken(UserDetails user) {
        return getToken(new HashMap<>(), user);
    }

    /**
     * Genera un token JWT con claims adicionales
     * @param extraClaims Claims adicionales
     * @param user Detalles del usuario
     * @return Token JWT
     */
    private String getToken(Map<String, Object> extraClaims, UserDetails user) {
        extraClaims.put("roles", user.getAuthorities());
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiration = new Date(System.currentTimeMillis() + authProperties.getJwt().getExpirationTime());
        
        log.debug("Generando token para usuario: {} con expiración: {}", user.getUsername(), expiration);
        
        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                .issuer(authProperties.getJwt().getIssuer())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getKey())
                .compact();
    }

    /**
     * Obtiene la clave secreta para firmar tokens
     * @return Clave secreta
     */
    private SecretKey getKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(authProperties.getJwt().getSecretKey());
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Error al decodificar la clave JWT", e);
            throw new IllegalArgumentException("Clave JWT no válida o mal codificada en Base64", e);
        }
    }

    /**
     * Extrae el nombre de usuario del token
     * @param token Token JWT
     * @return Nombre de usuario
     */
    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * Valida si el token es válido para el usuario
     * @param token Token JWT
     * @param userDetails Detalles del usuario
     * @return true si el token es válido
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Obtiene todos los claims del token
     * @param token Token JWT
     * @return Claims del token
     */
    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrae un claim específico del token
     * @param token Token JWT
     * @param claimsResolver Función para extraer el claim
     * @return Valor del claim
     */
    public <T> T getClaim(String token, Function<Claims,T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Obtiene la fecha de expiración del token
     * @param token Token JWT
     * @return Fecha de expiración
     */
    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    /**
     * Obtiene la fecha de expiración del token como LocalDateTime
     * @param token Token JWT
     * @return Fecha de expiración como LocalDateTime
     */
    public LocalDateTime getExpirationDateFromToken(String token) {
        Date expiration = getExpiration(token);
        return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Verifica si el token ha expirado
     * @param token Token JWT
     * @return true si el token ha expirado
     */
    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    /**
     * Obtiene la fecha de emisión del token como LocalDateTime
     * @param token Token JWT
     * @return Fecha de emisión como LocalDateTime
     */
    public LocalDateTime getIssuedAtFromToken(String token) {
        Date issuedAt = getClaim(token, Claims::getIssuedAt);
        return issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
