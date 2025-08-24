package com.barbershop.features.user.repository;

import com.barbershop.features.user.model.User;
import com.barbershop.features.user.model.enums.RoleEnum;
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
public interface UserRepository extends JpaRepository<User, String> {

    // Consultas que excluyen registros eliminados
    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    List<User> findAllActive();

    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    Page<User> findAllActive(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findByEmailAndNotDeleted(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.isDeleted = false")
    Optional<User> findByIdAndNotDeleted(@Param("userId") String userId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.isDeleted = false")
    boolean existsByEmailAndNotDeleted(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.userId != :userId AND u.isDeleted = false")
    boolean existsByEmailAndUserIdNotAndNotDeleted(@Param("email") String email, @Param("userId") String userId);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isDeleted = false")
    List<User> findByRoleAndNotDeleted(@Param("role") RoleEnum role);

    @Query("SELECT u FROM User u WHERE u.isActive = :isActive AND u.isDeleted = false")
    List<User> findByIsActiveAndNotDeleted(@Param("isActive") Boolean isActive);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = :isActive AND u.isDeleted = false")
    List<User> findByRoleAndIsActiveAndNotDeleted(@Param("role") RoleEnum role, @Param("isActive") Boolean isActive);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isDeleted = false")
    long countByRoleAndNotDeleted(@Param("role") RoleEnum role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = :isActive AND u.isDeleted = false")
    long countByIsActiveAndNotDeleted(@Param("isActive") Boolean isActive);

    @Query("SELECT u FROM User u WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND u.isDeleted = false")
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndNotDeleted(@Param("firstName") String firstName, @Param("lastName") String lastName);

    // Métodos para soft delete
    @Modifying
    @Query("UPDATE User u SET u.isDeleted = true, u.deletedAt = :deletedAt WHERE u.userId = :userId")
    void softDeleteById(@Param("userId") String userId, @Param("deletedAt") LocalDateTime deletedAt);

    @Modifying
    @Query("UPDATE User u SET u.isDeleted = false, u.deletedAt = null WHERE u.userId = :userId")
    void restoreById(@Param("userId") String userId);

    // Consultas que incluyen registros eliminados (para administración)
    @Query("SELECT u FROM User u WHERE u.isDeleted = true")
    List<User> findAllDeleted();

    @Query("SELECT u FROM User u WHERE u.isDeleted = true")
    Page<User> findAllDeleted(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.isDeleted = true")
    Optional<User> findByIdAndDeleted(@Param("userId") String userId);

    // Métodos originales mantenidos para compatibilidad (ahora incluyen filtro de eliminados)
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.isDeleted = false")
    boolean existsByEmail(@Param("email") String email);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.userId != :userId AND u.isDeleted = false")
    boolean existsByEmailAndUserIdNot(@Param("email") String email, @Param("userId") String userId);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isDeleted = false")
    List<User> findByRole(@Param("role") RoleEnum role);
    
    @Query("SELECT u FROM User u WHERE u.isActive = :isActive AND u.isDeleted = false")
    List<User> findByIsActive(@Param("isActive") Boolean isActive);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = :isActive AND u.isDeleted = false")
    List<User> findByRoleAndIsActive(@Param("role") RoleEnum role, @Param("isActive") Boolean isActive);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isDeleted = false")
    long countByRole(@Param("role") RoleEnum role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = :isActive AND u.isDeleted = false")
    long countByIsActive(@Param("isActive") Boolean isActive);
    
    @Query("SELECT u FROM User u WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND u.isDeleted = false")
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(@Param("firstName") String firstName, @Param("lastName") String lastName);
}
