package com.barbershop.features.barbershop.repository;

import com.barbershop.features.barbershop.model.Barbershop;
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
public interface BarbershopRepository extends JpaRepository<Barbershop, String> {

    // Consultas que excluyen registros eliminados
    @Query("SELECT b FROM Barbershop b WHERE b.isDeleted = false")
    List<Barbershop> findAllActive();

    @Query("SELECT b FROM Barbershop b WHERE b.isDeleted = false")
    Page<Barbershop> findAllActive(Pageable pageable);

    @Query("SELECT b FROM Barbershop b WHERE b.barbershopId = :barbershopId AND b.isDeleted = false")
    Optional<Barbershop> findByIdAndNotDeleted(@Param("barbershopId") String barbershopId);

    @Query("SELECT b FROM Barbershop b WHERE b.name = :name AND b.isDeleted = false")
    Optional<Barbershop> findByNameAndNotDeleted(@Param("name") String name);
    
    @Query("SELECT b FROM Barbershop b WHERE LOWER(b.name) = LOWER(:name) AND b.isDeleted = false")
    Optional<Barbershop> findByNameIgnoreCaseAndNotDeleted(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barbershop b WHERE b.name = :name AND b.isDeleted = false")
    boolean existsByNameAndNotDeleted(@Param("name") String name);
    
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barbershop b WHERE LOWER(b.name) = LOWER(:name) AND b.isDeleted = false")
    boolean existsByNameIgnoreCaseAndNotDeleted(@Param("name") String name);
    
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barbershop b WHERE LOWER(b.name) = LOWER(:name) AND b.barbershopId != :barbershopId AND b.isDeleted = false")
    boolean existsByNameIgnoreCaseAndBarbershopIdNotAndNotDeleted(@Param("name") String name, @Param("barbershopId") String barbershopId);

    @Query("SELECT b FROM Barbershop b WHERE b.email = :email AND b.isDeleted = false")
    Optional<Barbershop> findByEmailAndNotDeleted(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barbershop b WHERE b.email = :email AND b.isDeleted = false")
    boolean existsByEmailAndNotDeleted(@Param("email") String email);
    
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barbershop b WHERE b.email = :email AND b.barbershopId != :barbershopId AND b.isDeleted = false")
    boolean existsByEmailAndBarbershopIdNotAndNotDeleted(@Param("email") String email, @Param("barbershopId") String barbershopId);

    // Métodos para soft delete
    @Modifying
    @Query("UPDATE Barbershop b SET b.isDeleted = true, b.deletedAt = :deletedAt WHERE b.barbershopId = :barbershopId")
    void softDeleteById(@Param("barbershopId") String barbershopId, @Param("deletedAt") LocalDateTime deletedAt);

    @Modifying
    @Query("UPDATE Barbershop b SET b.isDeleted = false, b.deletedAt = null WHERE b.barbershopId = :barbershopId")
    void restoreById(@Param("barbershopId") String barbershopId);

    // Consultas que incluyen registros eliminados (para administración)
    @Query("SELECT b FROM Barbershop b WHERE b.isDeleted = true")
    List<Barbershop> findAllDeleted();

    @Query("SELECT b FROM Barbershop b WHERE b.isDeleted = true")
    Page<Barbershop> findAllDeleted(Pageable pageable);

    @Query("SELECT b FROM Barbershop b WHERE b.barbershopId = :barbershopId AND b.isDeleted = true")
    Optional<Barbershop> findByIdAndDeleted(@Param("barbershopId") String barbershopId);

    // Métodos originales mantenidos para compatibilidad (ahora incluyen filtro de eliminados)
    @Query("SELECT b FROM Barbershop b WHERE b.name = :name AND b.isDeleted = false")
    Optional<Barbershop> findByName(@Param("name") String name);
    
    @Query("SELECT b FROM Barbershop b WHERE LOWER(b.name) = LOWER(:name) AND b.isDeleted = false")
    Optional<Barbershop> findByNameIgnoreCase(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barbershop b WHERE b.name = :name AND b.isDeleted = false")
    boolean existsByName(@Param("name") String name);
    
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barbershop b WHERE LOWER(b.name) = LOWER(:name) AND b.isDeleted = false")
    boolean existsByNameIgnoreCase(@Param("name") String name);
    
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barbershop b WHERE LOWER(b.name) = LOWER(:name) AND b.barbershopId != :barbershopId AND b.isDeleted = false")
    boolean existsByNameIgnoreCaseAndBarbershopIdNot(@Param("name") String name, @Param("barbershopId") String barbershopId);

    @Query("SELECT b FROM Barbershop b WHERE b.email = :email AND b.isDeleted = false")
    Optional<Barbershop> findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barbershop b WHERE b.email = :email AND b.isDeleted = false")
    boolean existsByEmail(@Param("email") String email);
    
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barbershop b WHERE b.email = :email AND b.barbershopId != :barbershopId AND b.isDeleted = false")
    boolean existsByEmailAndBarbershopIdNot(@Param("email") String email, @Param("barbershopId") String barbershopId);
}