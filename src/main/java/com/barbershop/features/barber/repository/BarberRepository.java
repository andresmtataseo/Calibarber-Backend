package com.barbershop.features.barber.repository;

import com.barbershop.features.barber.model.Barber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<Barber, String> {

    // Consultas que excluyen registros eliminados (soft delete)
    @Query("SELECT b FROM Barber b WHERE b.isActive = true")
    List<Barber> findAllActive();

    @Query("SELECT b FROM Barber b WHERE b.isActive = true")
    Page<Barber> findAllActive(Pageable pageable);

    @Query("SELECT b FROM Barber b WHERE b.barberId = :barberId AND b.isActive = true")
    Optional<Barber> findByIdAndActive(@Param("barberId") String barberId);

    // Consultas por barbería
    @Query("SELECT b FROM Barber b WHERE b.barbershopId = :barbershopId AND b.isActive = true")
    List<Barber> findByBarbershopIdAndActive(@Param("barbershopId") String barbershopId);

    @Query("SELECT b FROM Barber b WHERE b.barbershopId = :barbershopId AND b.isActive = true")
    Page<Barber> findByBarbershopIdAndActive(@Param("barbershopId") String barbershopId, Pageable pageable);

    // Consultas por usuario
    @Query("SELECT b FROM Barber b WHERE b.userId = :userId AND b.isActive = true")
    Optional<Barber> findByUserIdAndActive(@Param("userId") String userId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barber b WHERE b.userId = :userId AND b.isActive = true")
    boolean existsByUserIdAndActive(@Param("userId") String userId);

    // Consultas por especialización
    @Query("SELECT b FROM Barber b WHERE LOWER(b.specialization) LIKE LOWER(CONCAT('%', :specialization, '%')) AND b.isActive = true")
    List<Barber> findBySpecializationContainingIgnoreCaseAndActive(@Param("specialization") String specialization);

    @Query("SELECT b FROM Barber b WHERE LOWER(b.specialization) LIKE LOWER(CONCAT('%', :specialization, '%')) AND b.isActive = true")
    Page<Barber> findBySpecializationContainingIgnoreCaseAndActive(@Param("specialization") String specialization, Pageable pageable);

    // Conteos
    @Query("SELECT COUNT(b) FROM Barber b WHERE b.barbershopId = :barbershopId AND b.isActive = true")
    long countByBarbershopIdAndActive(@Param("barbershopId") String barbershopId);

    @Query("SELECT COUNT(b) FROM Barber b WHERE b.isActive = true")
    long countByActive();

    // Soft delete
    @Modifying
    @Query("UPDATE Barber b SET b.isActive = false, b.updatedAt = :updatedAt WHERE b.barberId = :barberId")
    void softDeleteById(@Param("barberId") String barberId, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query("UPDATE Barber b SET b.isActive = true, b.updatedAt = :updatedAt WHERE b.barberId = :barberId")
    void restoreById(@Param("barberId") String barberId, @Param("updatedAt") LocalDateTime updatedAt);

    // Consultas para registros eliminados (para administradores)
    @Query("SELECT b FROM Barber b WHERE b.isActive = false")
    List<Barber> findAllInactive();

    @Query("SELECT b FROM Barber b WHERE b.isActive = false")
    Page<Barber> findAllInactive(Pageable pageable);

    @Query("SELECT b FROM Barber b WHERE b.barberId = :barberId AND b.isActive = false")
    Optional<Barber> findByIdAndInactive(@Param("barberId") String barberId);

    // Consultas con joins para obtener información completa
    @Query("SELECT b FROM Barber b JOIN FETCH b.user WHERE b.isActive = true")
    List<Barber> findAllActiveWithUser();

    @Query("SELECT b FROM Barber b JOIN FETCH b.user WHERE b.barberId = :barberId AND b.isActive = true")
    Optional<Barber> findByIdAndActiveWithUser(@Param("barberId") String barberId);

    @Query("SELECT b FROM Barber b JOIN FETCH b.user WHERE b.barbershopId = :barbershopId AND b.isActive = true")
    List<Barber> findByBarbershopIdAndActiveWithUser(@Param("barbershopId") String barbershopId);

    // Validaciones de negocio
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barber b WHERE b.userId = :userId AND b.barbershopId = :barbershopId AND b.isActive = true")
    boolean existsByUserIdAndBarbershopIdAndActive(@Param("userId") String userId, @Param("barbershopId") String barbershopId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barber b WHERE b.userId = :userId AND b.barbershopId != :barbershopId AND b.isActive = true")
    boolean existsByUserIdAndBarbershopIdNotAndActive(@Param("userId") String userId, @Param("barbershopId") String barbershopId);
}