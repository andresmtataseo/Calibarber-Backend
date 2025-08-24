package com.barbershop.features.barbershop.service;

import com.barbershop.common.exception.ResourceAlreadyExistsException;
import com.barbershop.common.exception.ResourceNotFoundException;
import com.barbershop.features.barbershop.dto.BarbershopCreateDto;
import com.barbershop.features.barbershop.dto.BarbershopResponseDto;
import com.barbershop.features.barbershop.dto.BarbershopUpdateDto;
import com.barbershop.features.barbershop.mapper.BarbershopMapper;
import com.barbershop.features.barbershop.model.Barbershop;
import com.barbershop.features.barbershop.repository.BarbershopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BarbershopService {

    private final BarbershopRepository barbershopRepository;
    private final BarbershopMapper barbershopMapper;

    /**
     * Crea una nueva barbería
     */
    public BarbershopResponseDto createBarbershop(BarbershopCreateDto createDto) {
        log.info("Creando nueva barbería con nombre: {}", createDto.getName());

        // Verificar que no exista una barbería con el mismo nombre
        if (barbershopRepository.existsByName(createDto.getName())) {
            throw new ResourceAlreadyExistsException("Ya existe una barbería con el nombre: " + createDto.getName());
        }

        // Verificar que no exista una barbería con el mismo email (si se proporciona)
        if (createDto.getEmail() != null && barbershopRepository.existsByEmail(createDto.getEmail())) {
            throw new ResourceAlreadyExistsException("Ya existe una barbería con el email: " + createDto.getEmail());
        }

        Barbershop barbershop = barbershopMapper.toEntity(createDto);
        Barbershop savedBarbershop = barbershopRepository.save(barbershop);

        log.info("Barbería creada exitosamente con ID: {}", savedBarbershop.getBarbershopId());
        return barbershopMapper.toResponseDto(savedBarbershop);
    }

    /**
     * Obtiene todas las barberías con paginación
     */
    @Transactional(readOnly = true)
    public Page<BarbershopResponseDto> getAllBarbershops(Pageable pageable) {
        log.info("Obteniendo todas las barberías - Página: {}, Tamaño: {}", 
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Barbershop> barbershops = barbershopRepository.findAllActive(pageable);
        return barbershops.map(barbershopMapper::toResponseDto);
    }

    /**
     * Obtiene una barbería por su ID
     */
    @Transactional(readOnly = true)
    public BarbershopResponseDto getBarbershopById(String id) {
        log.info("Obteniendo barbería con ID: {}", id);

        Barbershop barbershop = barbershopRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + id));

        return barbershopMapper.toResponseDto(barbershop);
    }

    /**
     * Actualiza una barbería existente
     */
    public BarbershopResponseDto updateBarbershop(String id, BarbershopUpdateDto updateDto) {
        log.info("Actualizando barbería con ID: {}", id);

        Barbershop existingBarbershop = barbershopRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + id));

        // Verificar que no exista otra barbería con el mismo nombre (si se está cambiando)
        if (updateDto.getName() != null && !updateDto.getName().equals(existingBarbershop.getName())) {
            if (barbershopRepository.existsByName(updateDto.getName())) {
                throw new ResourceAlreadyExistsException("Ya existe una barbería con el nombre: " + updateDto.getName());
            }
        }

        // Verificar que no exista otra barbería con el mismo email (si se está cambiando)
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(existingBarbershop.getEmail())) {
            if (barbershopRepository.existsByEmail(updateDto.getEmail())) {
                throw new ResourceAlreadyExistsException("Ya existe una barbería con el email: " + updateDto.getEmail());
            }
        }

        // Actualizar solo los campos que no son nulos
        barbershopMapper.updateEntityFromDto(updateDto, existingBarbershop);
        Barbershop updatedBarbershop = barbershopRepository.save(existingBarbershop);

        log.info("Barbería actualizada exitosamente con ID: {}", id);
        return barbershopMapper.toResponseDto(updatedBarbershop);
    }

    /**
     * Elimina una barbería usando soft delete
     */
    public void deleteBarbershop(String id) {
        log.info("Eliminando barbería con ID: {} (soft delete)", id);

        Barbershop barbershop = barbershopRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + id));

        barbershop.setIsDeleted(true);
        barbershop.setDeletedAt(LocalDateTime.now());
        barbershopRepository.save(barbershop);
        
        log.info("Barbería eliminada exitosamente con ID: {} (soft delete)", id);
    }

    /**
     * Restaura una barbería eliminada por ID
     */
    public BarbershopResponseDto restoreBarbershop(String id) {
        log.info("Restaurando barbería con ID: {}", id);

        // Verificar que la barbería existe y está eliminada
        Barbershop barbershop = barbershopRepository.findByIdAndDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbería eliminada no encontrada con ID: " + id));

        barbershop.setIsDeleted(false);
        barbershop.setDeletedAt(null);
        Barbershop restoredBarbershop = barbershopRepository.save(barbershop);

        log.info("Barbería restaurada exitosamente con ID: {}", id);
        return barbershopMapper.toResponseDto(restoredBarbershop);
    }

    /**
     * Obtiene todas las barberías eliminadas paginadas
     */
    @Transactional(readOnly = true)
    public Page<BarbershopResponseDto> getDeletedBarbershops(Pageable pageable) {
        log.info("Obteniendo barberías eliminadas paginadas: página {}, tamaño {}", 
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Barbershop> deletedBarbershops = barbershopRepository.findAllDeleted(pageable);
        return deletedBarbershops.map(barbershopMapper::toResponseDto);
    }


}