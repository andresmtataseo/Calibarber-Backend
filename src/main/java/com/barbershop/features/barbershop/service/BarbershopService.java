package com.barbershop.features.barbershop.service;

import com.barbershop.common.exception.ResourceAlreadyExistsException;
import com.barbershop.common.exception.ResourceNotFoundException;
import com.barbershop.common.exception.BusinessLogicException;
import com.barbershop.features.barbershop.dto.BarbershopCreateDto;
import com.barbershop.features.barbershop.dto.BarbershopOperatingHoursCreateDto;
import com.barbershop.features.barbershop.dto.BarbershopOperatingHoursDto;
import com.barbershop.features.barbershop.dto.BarbershopResponseDto;
import com.barbershop.features.barbershop.dto.BarbershopUpdateDto;
import com.barbershop.features.barbershop.mapper.BarbershopMapper;
import com.barbershop.features.barbershop.model.Barbershop;
import com.barbershop.features.barbershop.repository.BarbershopRepository;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BarbershopService {

    private final BarbershopRepository barbershopRepository;
    private final BarbershopMapper barbershopMapper;
    private final BarbershopOperatingHoursService operatingHoursService;

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

        try {
            Barbershop barbershop = barbershopMapper.toEntity(createDto);
            Barbershop savedBarbershop = barbershopRepository.save(barbershop);

            log.info("Barbería creada exitosamente con ID: {}", savedBarbershop.getBarbershopId());
            BarbershopResponseDto responseDto = barbershopMapper.toResponseDto(savedBarbershop);
            
            // Incluir horarios de operación si existen
            List<BarbershopOperatingHoursDto> operatingHours = operatingHoursService
                    .getOperatingHoursByBarbershop(savedBarbershop.getBarbershopId());
            responseDto.setOperatingHours(operatingHours);
            
            return responseDto;
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMessage().toLowerCase();
            if (errorMessage.contains("name") || errorMessage.contains("nombre")) {
                throw new BusinessLogicException("Ya existe una barbería con este nombre");
            } else if (errorMessage.contains("email") || errorMessage.contains("correo")) {
                throw new BusinessLogicException("Ya existe una barbería con este email");
            } else if (errorMessage.contains("phone") || errorMessage.contains("telefono")) {
                throw new BusinessLogicException("Ya existe una barbería con este número de teléfono");
            } else {
                throw new BusinessLogicException("Error al crear la barbería: datos inválidos o duplicados");
            }
        }
    }

    /**
     * Obtiene todas las barberías con paginación
     */
    @Transactional(readOnly = true)
    public Page<BarbershopResponseDto> getAllBarbershops(int page, int size, String sortBy, String sortDir) {
        log.info("Obteniendo todas las barberías - Página: {}, Tamaño: {}, Ordenar por: {}, Dirección: {}", 
                page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Barbershop> barbershops = barbershopRepository.findAllActive(pageable);
        return barbershops.map(barbershop -> {
            BarbershopResponseDto responseDto = barbershopMapper.toResponseDto(barbershop);
            // Incluir horarios de operación
             List<BarbershopOperatingHoursDto> operatingHours = operatingHoursService
                     .getOperatingHoursByBarbershop(barbershop.getBarbershopId());
            responseDto.setOperatingHours(operatingHours);
            return responseDto;
        });
    }

    /**
     * Obtiene una barbería por su ID
     */
    @Transactional(readOnly = true)
    public BarbershopResponseDto getBarbershopById(String id) {
        log.info("Obteniendo barbería con ID: {}", id);

        Barbershop barbershop = barbershopRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + id));

        BarbershopResponseDto responseDto = barbershopMapper.toResponseDto(barbershop);
        
        // Incluir horarios de operación
        List<BarbershopOperatingHoursDto> operatingHours = operatingHoursService
                .getOperatingHoursByBarbershop(barbershop.getBarbershopId());
        responseDto.setOperatingHours(operatingHours);
        
        return responseDto;
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

        try {
            // Actualizar solo los campos que no son nulos
            barbershopMapper.updateEntityFromDto(updateDto, existingBarbershop);
            Barbershop updatedBarbershop = barbershopRepository.save(existingBarbershop);

            log.info("Barbería actualizada exitosamente con ID: {}", id);
            BarbershopResponseDto responseDto = barbershopMapper.toResponseDto(updatedBarbershop);
            
            // Incluir horarios de operación si existen
            List<BarbershopOperatingHoursDto> operatingHours = operatingHoursService
                    .getOperatingHoursByBarbershop(updatedBarbershop.getBarbershopId());
            responseDto.setOperatingHours(operatingHours);
            
            return responseDto;
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMessage().toLowerCase();
            if (errorMessage.contains("name") || errorMessage.contains("nombre")) {
                throw new BusinessLogicException("Ya existe una barbería con este nombre");
            } else if (errorMessage.contains("email") || errorMessage.contains("correo")) {
                throw new BusinessLogicException("Ya existe una barbería con este email");
            } else if (errorMessage.contains("phone") || errorMessage.contains("telefono")) {
                throw new BusinessLogicException("Ya existe una barbería con este número de teléfono");
            } else {
                throw new BusinessLogicException("Error al actualizar la barbería: datos inválidos o duplicados");
            }
        }
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
        BarbershopResponseDto responseDto = barbershopMapper.toResponseDto(restoredBarbershop);
        
        // Incluir horarios de operación
        List<BarbershopOperatingHoursDto> operatingHours = operatingHoursService
                .getOperatingHoursByBarbershop(restoredBarbershop.getBarbershopId());
        responseDto.setOperatingHours(operatingHours);
        
        return responseDto;
    }

    /**
     * Obtiene todas las barberías eliminadas paginadas
     */
    @Transactional(readOnly = true)
    public Page<BarbershopResponseDto> getDeletedBarbershops(int page, int size, String sortBy, String sortDir) {
        log.info("Obteniendo barberías eliminadas paginadas: página {}, tamaño {}, Ordenar por: {}, Dirección: {}", 
                page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Barbershop> deletedBarbershops = barbershopRepository.findAllDeleted(pageable);
        return deletedBarbershops.map(barbershop -> {
            BarbershopResponseDto responseDto = barbershopMapper.toResponseDto(barbershop);
            // Incluir horarios de operación
             List<BarbershopOperatingHoursDto> operatingHours = operatingHoursService
                     .getOperatingHoursByBarbershop(barbershop.getBarbershopId());
            responseDto.setOperatingHours(operatingHours);
            return responseDto;
        });
    }

    // ==================== MÉTODOS PARA HORARIOS DE OPERACIÓN ====================

    /**
     * Obtiene todos los horarios de operación de una barbería
     */
    @Transactional(readOnly = true)
    public List<BarbershopOperatingHoursDto> getBarbershopOperatingHours(String barbershopId) {
        log.info("Obteniendo horarios de operación para barbería: {}", barbershopId);
        
        // Verificar que la barbería existe
        barbershopRepository.findByIdAndNotDeleted(barbershopId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + barbershopId));
        
        return operatingHoursService.getOperatingHoursByBarbershop(barbershopId);
    }

    /**
     * Obtiene el horario de operación para un día específico
     */
    @Transactional(readOnly = true)
    public BarbershopOperatingHoursDto getBarbershopOperatingHoursForDay(String barbershopId, DayOfWeek dayOfWeek) {
        log.info("Obteniendo horario para barbería {} en día {}", barbershopId, dayOfWeek);
        
        // Verificar que la barbería existe
        barbershopRepository.findByIdAndNotDeleted(barbershopId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + barbershopId));
        
        return operatingHoursService.getOperatingHoursForDay(barbershopId, dayOfWeek)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No se encontraron horarios para el día " + dayOfWeek + " en la barbería: " + barbershopId));
    }

    /**
     * Crea o actualiza horarios de operación para una barbería
     */
    public List<BarbershopOperatingHoursDto> createOrUpdateBarbershopOperatingHours(
            String barbershopId, 
            List<BarbershopOperatingHoursCreateDto> operatingHoursData) {
        
        log.info("Creando/actualizando horarios para barbería: {}", barbershopId);
        
        // Verificar que la barbería existe
        barbershopRepository.findByIdAndNotDeleted(barbershopId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + barbershopId));
        
        return operatingHoursService.createOrUpdateOperatingHours(barbershopId, operatingHoursData);
    }

    /**
     * Verifica si una barbería está abierta en un día y hora específicos
     */
    @Transactional(readOnly = true)
    public boolean isBarbershopOpenAt(String barbershopId, DayOfWeek dayOfWeek, LocalTime time) {
        log.debug("Verificando si barbería {} está abierta el {} a las {}", barbershopId, dayOfWeek, time);
        
        // Verificar que la barbería existe
        barbershopRepository.findByIdAndNotDeleted(barbershopId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + barbershopId));
        
        return operatingHoursService.isBarbershopOpenAt(barbershopId, dayOfWeek, time);
    }

    /**
     * Elimina todos los horarios de operación de una barbería
     */
    public void deleteBarbershopOperatingHours(String barbershopId) {
        log.info("Eliminando todos los horarios de barbería: {}", barbershopId);
        
        // Verificar que la barbería existe
        barbershopRepository.findByIdAndNotDeleted(barbershopId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbería no encontrada con ID: " + barbershopId));
        
        operatingHoursService.deleteAllOperatingHours(barbershopId);
    }

}