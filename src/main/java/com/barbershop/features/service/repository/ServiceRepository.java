package com.barbershop.features.service.repository;

import com.barbershop.features.service.model.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {

    // Consultas básicas para servicios activos
    @Query("SELECT s FROM Service s WHERE s.isActive = true")
    List<Service> findAllActive();

    @Query("SELECT s FROM Service s WHERE s.isActive = true")
    Page<Service> findAllActive(Pageable pageable);

    @Query("SELECT s FROM Service s WHERE s.serviceId = :serviceId AND s.isActive = true")
    Optional<Service> findByIdAndActive(@Param("serviceId") String serviceId);

    // Consultas por barbería
    @Query("SELECT s FROM Service s WHERE s.barbershopId = :barbershopId AND s.isActive = true")
    List<Service> findByBarbershopIdAndActive(@Param("barbershopId") String barbershopId);

    @Query("SELECT s FROM Service s WHERE s.barbershopId = :barbershopId AND s.isActive = true")
    Page<Service> findByBarbershopIdAndActive(@Param("barbershopId") String barbershopId, Pageable pageable);

    @Query("SELECT s FROM Service s WHERE s.barbershopId = :barbershopId")
    List<Service> findByBarbershopId(@Param("barbershopId") String barbershopId);

    @Query("SELECT s FROM Service s WHERE s.barbershopId = :barbershopId")
    Page<Service> findByBarbershopId(@Param("barbershopId") String barbershopId, Pageable pageable);

    // Consultas por nombre
    @Query("SELECT s FROM Service s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) AND s.isActive = true")
    List<Service> findByNameContainingIgnoreCaseAndActive(@Param("name") String name);

    @Query("SELECT s FROM Service s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) AND s.isActive = true")
    Page<Service> findByNameContainingIgnoreCaseAndActive(@Param("name") String name, Pageable pageable);

    // Consultas por rango de precios
    @Query("SELECT s FROM Service s WHERE s.price BETWEEN :minPrice AND :maxPrice AND s.isActive = true")
    List<Service> findByPriceBetweenAndActive(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT s FROM Service s WHERE s.price BETWEEN :minPrice AND :maxPrice AND s.isActive = true")
    Page<Service> findByPriceBetweenAndActive(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    // Consultas por duración
    @Query("SELECT s FROM Service s WHERE s.durationMinutes BETWEEN :minDuration AND :maxDuration AND s.isActive = true")
    List<Service> findByDurationBetweenAndActive(@Param("minDuration") Integer minDuration, @Param("maxDuration") Integer maxDuration);

    @Query("SELECT s FROM Service s WHERE s.durationMinutes BETWEEN :minDuration AND :maxDuration AND s.isActive = true")
    Page<Service> findByDurationBetweenAndActive(@Param("minDuration") Integer minDuration, @Param("maxDuration") Integer maxDuration, Pageable pageable);

    // Consultas combinadas por barbería y filtros
    @Query("SELECT s FROM Service s WHERE s.barbershopId = :barbershopId AND LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) AND s.isActive = true")
    List<Service> findByBarbershopIdAndNameContainingIgnoreCaseAndActive(@Param("barbershopId") String barbershopId, @Param("name") String name);

    @Query("SELECT s FROM Service s WHERE s.barbershopId = :barbershopId AND s.price BETWEEN :minPrice AND :maxPrice AND s.isActive = true")
    List<Service> findByBarbershopIdAndPriceBetweenAndActive(@Param("barbershopId") String barbershopId, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    // Consultas de conteo
    @Query("SELECT COUNT(s) FROM Service s WHERE s.barbershopId = :barbershopId AND s.isActive = true")
    Long countByBarbershopIdAndActive(@Param("barbershopId") String barbershopId);

    @Query("SELECT COUNT(s) FROM Service s WHERE s.isActive = true")
    Long countAllActive();

    @Query("SELECT COUNT(s) FROM Service s WHERE s.barbershopId = :barbershopId")
    Long countByBarbershopId(@Param("barbershopId") String barbershopId);

    // Operaciones de soft delete
    @Modifying
    @Transactional
    @Query("UPDATE Service s SET s.isActive = false WHERE s.serviceId = :serviceId")
    int softDeleteById(@Param("serviceId") String serviceId);

    @Modifying
    @Transactional
    @Query("UPDATE Service s SET s.isActive = true WHERE s.serviceId = :serviceId")
    int restoreById(@Param("serviceId") String serviceId);

    @Modifying
    @Transactional
    @Query("UPDATE Service s SET s.isActive = false WHERE s.barbershopId = :barbershopId")
    int softDeleteByBarbershopId(@Param("barbershopId") String barbershopId);

    // Consultas para servicios inactivos
    @Query("SELECT s FROM Service s WHERE s.isActive = false")
    List<Service> findAllInactive();

    @Query("SELECT s FROM Service s WHERE s.isActive = false")
    Page<Service> findAllInactive(Pageable pageable);

    @Query("SELECT s FROM Service s WHERE s.barbershopId = :barbershopId AND s.isActive = false")
    List<Service> findByBarbershopIdAndInactive(@Param("barbershopId") String barbershopId);

    @Query("SELECT s FROM Service s WHERE s.barbershopId = :barbershopId AND s.isActive = false")
    Page<Service> findByBarbershopIdAndInactive(@Param("barbershopId") String barbershopId, Pageable pageable);

    // Validaciones de negocio
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Service s WHERE s.barbershopId = :barbershopId AND LOWER(s.name) = LOWER(:name) AND s.serviceId != :serviceId AND s.isActive = true")
    boolean existsByBarbershopIdAndNameIgnoreCaseAndNotServiceIdAndActive(@Param("barbershopId") String barbershopId, @Param("name") String name, @Param("serviceId") String serviceId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Service s WHERE s.barbershopId = :barbershopId AND LOWER(s.name) = LOWER(:name) AND s.isActive = true")
    boolean existsByBarbershopIdAndNameIgnoreCaseAndActive(@Param("barbershopId") String barbershopId, @Param("name") String name);

    // Consultas con joins para información adicional
    @Query("SELECT s FROM Service s JOIN FETCH s.barbershop WHERE s.serviceId = :serviceId AND s.isActive = true")
    Optional<Service> findByIdWithBarbershop(@Param("serviceId") String serviceId);

    @Query("SELECT s FROM Service s JOIN FETCH s.barbershop WHERE s.isActive = true")
    List<Service> findAllActiveWithBarbershop();

    // Consultas para estadísticas
    @Query("SELECT AVG(s.price) FROM Service s WHERE s.barbershopId = :barbershopId AND s.isActive = true")
    BigDecimal getAveragePriceByBarbershopId(@Param("barbershopId") String barbershopId);

    @Query("SELECT MIN(s.price) FROM Service s WHERE s.barbershopId = :barbershopId AND s.isActive = true")
    BigDecimal getMinPriceByBarbershopId(@Param("barbershopId") String barbershopId);

    @Query("SELECT MAX(s.price) FROM Service s WHERE s.barbershopId = :barbershopId AND s.isActive = true")
    BigDecimal getMaxPriceByBarbershopId(@Param("barbershopId") String barbershopId);

    @Query("SELECT AVG(s.durationMinutes) FROM Service s WHERE s.barbershopId = :barbershopId AND s.isActive = true")
    Double getAverageDurationByBarbershopId(@Param("barbershopId") String barbershopId);
}