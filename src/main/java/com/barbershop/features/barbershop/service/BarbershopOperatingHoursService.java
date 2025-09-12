package com.barbershop.features.barbershop.service;

import com.barbershop.features.barbershop.dto.BarbershopOperatingHoursCreateDto;
import com.barbershop.features.barbershop.dto.BarbershopOperatingHoursDto;
import com.barbershop.features.barbershop.model.Barbershop;
import com.barbershop.features.barbershop.model.BarbershopOperatingHours;
import com.barbershop.features.barbershop.repository.BarbershopOperatingHoursRepository;
import com.barbershop.features.barbershop.repository.BarbershopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para gestionar los horarios de operación de las barberías.
 * Proporciona funcionalidades para crear, actualizar, consultar y validar horarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BarbershopOperatingHoursService {

    private final BarbershopOperatingHoursRepository operatingHoursRepository;
    private final BarbershopRepository barbershopRepository;

    /**
     * Obtiene todos los horarios de operación de una barbería
     * @param barbershopId ID de la barbería
     * @return Lista de horarios ordenados por día de la semana
     */
    @Transactional(readOnly = true)
    public List<BarbershopOperatingHoursDto> getOperatingHoursByBarbershop(String barbershopId) {
        log.debug("Obteniendo horarios de operación para barbería: {}", barbershopId);
        
        List<BarbershopOperatingHours> operatingHours = operatingHoursRepository
                .findByBarbershopIdOrderByDayOfWeek(barbershopId);
        
        return operatingHours.stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Obtiene el horario de operación para un día específico
     * @param barbershopId ID de la barbería
     * @param dayOfWeek Día de la semana
     * @return Horario de operación si existe
     */
    @Transactional(readOnly = true)
    public Optional<BarbershopOperatingHoursDto> getOperatingHoursForDay(String barbershopId, DayOfWeek dayOfWeek) {
        log.debug("Obteniendo horario para barbería {} en día {}", barbershopId, dayOfWeek);
        
        return operatingHoursRepository
                .findByBarbershopIdAndDayOfWeek(barbershopId, dayOfWeek)
                .map(this::convertToDto);
    }

    /**
     * Crea o actualiza horarios de operación para una barbería
     * @param barbershopId ID de la barbería
     * @param operatingHoursData Lista de horarios a crear/actualizar
     * @return Lista de horarios creados/actualizados
     */
    @Transactional
    public List<BarbershopOperatingHoursDto> createOrUpdateOperatingHours(
            String barbershopId, 
            List<BarbershopOperatingHoursCreateDto> operatingHoursData) {
        
        log.info("Creando/actualizando horarios para barbería: {}", barbershopId);
        
        // Verificar que la barbería existe
        Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new RuntimeException("Barbería no encontrada: " + barbershopId));
        
        // Validar todos los horarios antes de procesar
        for (BarbershopOperatingHoursCreateDto dto : operatingHoursData) {
            if (!dto.isValid()) {
                throw new IllegalArgumentException(
                    "Horario inválido para " + dto.getDayOfWeek() + ": " + dto.getValidationError());
            }
        }
        
        // Procesar cada horario
        List<BarbershopOperatingHours> savedHours = operatingHoursData.stream()
                .map(dto -> createOrUpdateSingleOperatingHour(barbershop, dto))
                .toList();
        
        return savedHours.stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Verifica si una barbería está abierta en un día y hora específicos
     * @param barbershopId ID de la barbería
     * @param dayOfWeek Día de la semana
     * @param time Hora a verificar
     * @return true si está abierta
     */
    @Transactional(readOnly = true)
    public boolean isBarbershopOpenAt(String barbershopId, DayOfWeek dayOfWeek, LocalTime time) {
        log.debug("Verificando si barbería {} está abierta el {} a las {}", barbershopId, dayOfWeek, time);
        
        Optional<BarbershopOperatingHours> operatingHours = operatingHoursRepository
                .findByBarbershopIdAndDayOfWeek(barbershopId, dayOfWeek);
        
        if (operatingHours.isEmpty()) {
            return false;
        }
        
        return operatingHours.get().isWithinOperatingHours(time);
    }

    /**
     * Elimina todos los horarios de una barbería
     * @param barbershopId ID de la barbería
     */
    @Transactional
    public void deleteAllOperatingHours(String barbershopId) {
        log.info("Eliminando todos los horarios de barbería: {}", barbershopId);
        operatingHoursRepository.deleteByBarbershopId(barbershopId);
    }

    /**
     * Crea o actualiza un horario específico
     */
    private BarbershopOperatingHours createOrUpdateSingleOperatingHour(
            Barbershop barbershop, 
            BarbershopOperatingHoursCreateDto dto) {
        
        // Buscar si ya existe un horario para este día
        Optional<BarbershopOperatingHours> existingHours = operatingHoursRepository
                .findByBarbershopIdAndDayOfWeek(barbershop.getBarbershopId(), dto.getDayOfWeek());
        
        BarbershopOperatingHours operatingHours;
        
        if (existingHours.isPresent()) {
            // Actualizar existente
            operatingHours = existingHours.get();
            log.debug("Actualizando horario existente para {} en {}", 
                    barbershop.getBarbershopId(), dto.getDayOfWeek());
        } else {
            // Crear nuevo
            operatingHours = new BarbershopOperatingHours();
            operatingHours.setOperatingHoursId(UUID.randomUUID().toString());
            operatingHours.setBarbershop(barbershop);
            operatingHours.setDayOfWeek(dto.getDayOfWeek());
            log.debug("Creando nuevo horario para {} en {}", 
                    barbershop.getBarbershopId(), dto.getDayOfWeek());
        }
        
        // Actualizar campos
        operatingHours.setOpeningTime(dto.getOpeningTime());
        operatingHours.setClosingTime(dto.getClosingTime());
        operatingHours.setIsClosed(dto.getIsClosed());
        operatingHours.setNotes(dto.getNotes());
        
        return operatingHoursRepository.save(operatingHours);
    }

    /**
     * Convierte una entidad a DTO
     */
    private BarbershopOperatingHoursDto convertToDto(BarbershopOperatingHours entity) {
        BarbershopOperatingHoursDto dto = BarbershopOperatingHoursDto.builder()
                .operatingHoursId(entity.getOperatingHoursId())
                .dayOfWeek(entity.getDayOfWeek())
                .openingTime(entity.getOpeningTime())
                .closingTime(entity.getClosingTime())
                .isClosed(entity.getIsClosed())
                .notes(entity.getNotes())
                .formattedHours(entity.getFormattedHours())
                .isOpen(entity.isOpen())
                .build();
        
        // Establecer el nombre del día
        dto.setDayOfWeek(entity.getDayOfWeek());
        
        return dto;
    }
}