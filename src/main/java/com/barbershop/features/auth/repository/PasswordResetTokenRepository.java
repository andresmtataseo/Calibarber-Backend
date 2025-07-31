package com.barbershop.features.auth.repository;

import com.barbershop.features.auth.model.PasswordResetToken;
import com.barbershop.features.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio para operaciones de base de datos relacionadas con tokens de restablecimiento de contraseña.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

    /**
     * Busca un token de restablecimiento por su valor
     * @param token El token a buscar
     * @return Optional con el token si existe
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Busca un token de restablecimiento válido por su valor
     * @param token El token a buscar
     * @return Optional con el token si existe y es válido
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.token = :token AND prt.used = false AND prt.expiresAt > :now")
    Optional<PasswordResetToken> findValidTokenByToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Busca tokens de restablecimiento por usuario
     * @param user El usuario
     * @return Lista de tokens del usuario
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user = :user ORDER BY prt.createdAt DESC")
    java.util.List<PasswordResetToken> findByUser(@Param("user") User user);

    /**
     * Busca tokens válidos por usuario
     * @param user El usuario
     * @return Lista de tokens válidos del usuario
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user = :user AND prt.used = false AND prt.expiresAt > :now ORDER BY prt.createdAt DESC")
    java.util.List<PasswordResetToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Marca todos los tokens de un usuario como usados
     * @param user El usuario
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true, prt.usedAt = :now WHERE prt.user = :user AND prt.used = false")
    void markAllUserTokensAsUsed(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Elimina tokens expirados
     * @param now Fecha y hora actual
     * @return Número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Verifica si existe un token válido para un usuario
     * @param user El usuario
     * @param now Fecha y hora actual
     * @return true si existe al menos un token válido
     */
    @Query("SELECT COUNT(prt) > 0 FROM PasswordResetToken prt WHERE prt.user = :user AND prt.used = false AND prt.expiresAt > :now")
    boolean existsValidTokenForUser(@Param("user") User user, @Param("now") LocalDateTime now);
}