package com.barbershop.features.barbershop.service;

import com.barbershop.common.exception.BusinessLogicException;
import com.barbershop.common.exception.ResourceNotFoundException;
import com.barbershop.features.barbershop.dto.BarbershopOperatingHoursDto;
import com.barbershop.features.barbershop.dto.BarbershopOperatingHoursRequestDto;
import com.barbershop.features.barbershop.mapper.BarbershopOperatingHoursMapper;
import com.barbershop.features.barbershop.model.Barbershop;
import com.barbershop.features.barbershop.model.BarbershopOperatingHours;
import com.barbershop.features.barbershop.repository.BarbershopOperatingHoursRepository;
import com.barbershop.features.barbershop.repository.BarbershopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar los horarios de operación de las barberías.
 * Proporciona funcionalidades para crear, actualizar y consultar horarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BarbershopOperatingHoursService {

    private final BarbershopOperatingHoursRepository operatingHoursRepository;
    private final BarbershopRepository barbershopRepository;
    private final BarbershopOperatingHoursMapper mapper;

    /**
     * Crea o actualiza un horario de operación para una barbería.
     * Si ya existe un horario para el día especificado, lo actualiza.
     * Si no existe, crea uno nuevo.
     * 
     * @param requestDto datos del horario a crear o actualizar
     * @return DTO con los datos del horario creado o actualizado
     * @throws ResourceNotFoundException si la barbería no existe
     * @throws BusinessLogicException si los datos del horario no son válidos
     */
    @Transactional
    public BarbershopOperatingHoursDto createOrUpdateOperatingHours(BarbershopOperatingHoursRequestDto requestDto) {
        log.info("Creando o actualizando horario de operación para barbería: {} en día: {}", 
                requestDto.getBarbershopId(), requestDto.getDayOfWeek());
        
        // Validar datos del request
        validateOperatingHoursRequest(requestDto);
        
        // Verificar que la barbería existe
        Barbershop barbershop = barbershopRepository.findById(requestDto.getBarbershopId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Barbería no encontrada con ID: " + requestDto.getBarbershopId()));
        
        // Buscar si ya existe un horario para este día
        Optional<BarbershopOperatingHours> existingHours = operatingHoursRepository
            .findByBarbershop_BarbershopIdAndDayOfWeek(
                requestDto.getBarbershopId(), 
                requestDto.getDayOfWeek());
        
        BarbershopOperatingHours operatingHours;
        
        if (existingHours.isPresent()) {
            // Actualizar horario existente
            operatingHours = existingHours.get();
            mapper.updateEntityFromDto(requestDto, operatingHours);
            log.info("Actualizando horario existente para día: {}", requestDto.getDayOfWeek());
        } else {
            // Crear nuevo horario
            operatingHours = mapper.toEntity(requestDto);
            operatingHours.setBarbershop(barbershop);
            log.info("Creando nuevo horario para día: {}", requestDto.getDayOfWeek());
        }
        
        // Guardar y retornar
        BarbershopOperatingHours savedHours = operatingHoursRepository.save(operatingHours);
        log.info("Horario guardado exitosamente con ID: {}", savedHours.getOperatingHoursId());
        
        return mapper.toResponseDto(savedHours);
    }
    
    /**
     * Obtiene todos los horarios de operación de una barbería.
     * 
     * @param barbershopId ID de la barbería
     * @return lista de horarios de operación
     * @throws ResourceNotFoundException si la barbería no existe
     */
    @Transactional(readOnly = true)
    public List<BarbershopOperatingHoursDto> getOperatingHoursByBarbershop(String barbershopId) {
        log.info("Obteniendo horarios de operación para barbería: {}", barbershopId);
        
        // Verificar que la barbería existe
        if (!barbershopRepository.existsById(barbershopId)) {
            throw new ResourceNotFoundException("Barbería no encontrada con ID: " + barbershopId);
        }
        
        List<BarbershopOperatingHours> operatingHours = operatingHoursRepository
            .findByBarbershopIdOrderByDayOfWeek(barbershopId);
        
        log.info("Encontrados {} horarios para la barbería: {}", operatingHours.size(), barbershopId);
        
        return mapper.toResponseDtoList(operatingHours);
    }
    
    /**
     * Obtiene un horario específico por barbería y día de la semana.
     * 
     * @param barbershopId ID de la barbería
     * @param dayOfWeek día de la semana
     * @return horario de operación si existe
     * @throws ResourceNotFoundException si no se encuentra el horario
     */
    @Transactional(readOnly = true)
    public BarbershopOperatingHoursDto getOperatingHoursByBarbershopAndDay(
            String barbershopId, java.time.DayOfWeek dayOfWeek) {
        log.info("Obteniendo horario para barbería: {} en día: {}", barbershopId, dayOfWeek);
        
        BarbershopOperatingHours operatingHours = operatingHoursRepository
            .findByBarbershop_BarbershopIdAndDayOfWeek(barbershopId, dayOfWeek)
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format("No se encontró horario para la barbería %s en el día %s", 
                    barbershopId, dayOfWeek)));
        
        return mapper.toResponseDto(operatingHours);
    }
    
    /**
     * Valida que los datos del request sean consistentes y válidos.
     * 
     * @param requestDto datos a validar
     * @throws BusinessLogicException si los datos no son válidos
     */
    private void validateOperatingHoursRequest(BarbershopOperatingHoursRequestDto requestDto) {
        if (!requestDto.isValidSchedule()) {
            String errorMessage = requestDto.getValidationErrorMessage();
            log.warn("Datos de horario inválidos: {}", errorMessage);
            throw new BusinessLogicException(errorMessage != null ? errorMessage : 
                "Los datos del horario de operación no son válidos");
        }
    }
}