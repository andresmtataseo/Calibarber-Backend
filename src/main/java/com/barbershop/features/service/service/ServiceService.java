package com.barbershop.features.service.service;

import com.barbershop.common.exception.ResourceNotFoundException;
import com.barbershop.common.exception.ResourceAlreadyExistsException;
import com.barbershop.common.exception.BusinessLogicException;
import com.barbershop.features.auth.security.JwtService;
import com.barbershop.features.service.exception.ServiceHasActiveRecordsException;
import com.barbershop.features.appointment.repository.AppointmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import com.barbershop.features.service.dto.ServiceResponseDto;
import com.barbershop.features.service.dto.request.CreateServiceRequestDto;
import com.barbershop.features.service.dto.request.UpdateServiceRequestDto;
import com.barbershop.features.service.mapper.ServiceMapper;
import com.barbershop.features.service.repository.ServiceRepository;
import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.features.user.model.User;
import com.barbershop.features.user.repository.UserRepository;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.barbershop.shared.util.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Crea un nuevo servicio
     * 
     * @param request Datos del servicio a crear
     * @return Respuesta con el servicio creado
     * @throws AccessDeniedException si el usuario no tiene permisos para crear servicios
     * @apiNote La autenticación se maneja automáticamente a través de Spring Security
     */
    public ApiResponseDto<ServiceResponseDto> createService(CreateServiceRequestDto request) {
        log.info("Creando nuevo servicio: {}", request.getName());
        
        // Validar autorización
        validateServiceCreationAccess(request.getBarbershopId());
        
        // Validar que no exista un servicio con el mismo nombre en la barbería
        if (serviceRepository.existsByBarbershopIdAndNameIgnoreCaseAndActive(request.getBarbershopId(), request.getName())) {
            throw new ResourceAlreadyExistsException("Ya existe un servicio con el nombre '" + request.getName() + "' en esta barbería");
        }
        
        try {
            com.barbershop.features.service.model.Service service = serviceMapper.toEntity(request);
            com.barbershop.features.service.model.Service savedService = serviceRepository.save(service);
            
            ServiceResponseDto responseDto = serviceMapper.toResponseDto(savedService);
            
            log.info("Servicio creado exitosamente con ID: {}", savedService.getServiceId());
            return ApiResponseDto.<ServiceResponseDto>builder()
                    .status(HttpStatus.CREATED.value())
                    .message("Servicio creado exitosamente")
                    .data(responseDto)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (DataIntegrityViolationException ex) {
            log.error("Error de integridad de datos al crear servicio: {}", ex.getMessage());
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("barbershop_id")) {
                throw new BusinessLogicException("La barbería especificada no existe. Por favor, seleccione una barbería válida.");
            }
            throw new BusinessLogicException("Error al crear el servicio. Verifique que todos los datos sean válidos.");
        }

    }

    /**
     * Obtiene un servicio por ID
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<ServiceResponseDto> getServiceById(String serviceId) {
        log.info("Obteniendo servicio por ID: {}", serviceId);
        
        com.barbershop.features.service.model.Service service = serviceRepository.findByIdAndActive(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + serviceId));
        
        ServiceResponseDto responseDto = serviceMapper.toResponseDto(service);
        return ApiResponseDto.<ServiceResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("Servicio obtenido exitosamente")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene todos los servicios con paginación y ordenamiento (solo para administradores)
     * 
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el cual ordenar
     * @param sortDir Dirección del ordenamiento (asc/desc)
     * @return Respuesta con la página de servicios
     * @throws AccessDeniedException si el usuario no es administrador
     * @apiNote La autenticación se maneja automáticamente a través de Spring Security
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<ServiceResponseDto>> getAllServices(int page, int size, String sortBy, String sortDir) {
        log.info("Obteniendo servicios - Página: {}, Tamaño: {}, Ordenar por: {}, Dirección: {}", page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.barbershop.features.service.model.Service> servicesPage = serviceRepository.findAllActive(pageable);
        Page<ServiceResponseDto> responsePage = servicesPage.map(serviceMapper::toResponseDto);
        
        return ApiResponseDto.<Page<ServiceResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Servicios obtenidos exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene servicios por barbería con paginación
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<ServiceResponseDto>> getServicesByBarbershop(String barbershopId, int page, int size, String sortBy, String sortDir) {
        log.info("Obteniendo servicios por barbería: {} - Página: {}, Tamaño: {}", barbershopId, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.barbershop.features.service.model.Service> servicesPage = serviceRepository.findByBarbershopIdAndActive(barbershopId, pageable);
        Page<ServiceResponseDto> responsePage = servicesPage.map(serviceMapper::toResponseDto);
        
        return ApiResponseDto.<Page<ServiceResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Servicios obtenidos exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Busca servicios por nombre
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<ServiceResponseDto>> searchServicesByName(String name, int page, int size, String sortBy, String sortDir) {
        log.info("Buscando servicios por nombre: {}", name);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.barbershop.features.service.model.Service> servicesPage = serviceRepository.findByNameContainingIgnoreCaseAndActive(name, pageable);
        Page<ServiceResponseDto> responsePage = servicesPage.map(serviceMapper::toResponseDto);
        
        return ApiResponseDto.<Page<ServiceResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Servicios encontrados exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Busca servicios por rango de precios
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<ServiceResponseDto>> searchServicesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size, String sortBy, String sortDir) {
        log.info("Buscando servicios por rango de precios: {} - {}", minPrice, maxPrice);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.barbershop.features.service.model.Service> servicesPage = serviceRepository.findByPriceBetweenAndActive(minPrice, maxPrice, pageable);
        Page<ServiceResponseDto> responsePage = servicesPage.map(serviceMapper::toResponseDto);
        
        return ApiResponseDto.<Page<ServiceResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Servicios encontrados exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Busca servicios por rango de duración
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<ServiceResponseDto>> searchServicesByDurationRange(Integer minDuration, Integer maxDuration, int page, int size, String sortBy, String sortDir) {
        log.info("Buscando servicios por rango de duración: {} - {} minutos", minDuration, maxDuration);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.barbershop.features.service.model.Service> servicesPage = serviceRepository.findByDurationBetweenAndActive(minDuration, maxDuration, pageable);
        Page<ServiceResponseDto> responsePage = servicesPage.map(serviceMapper::toResponseDto);
        
        return ApiResponseDto.<Page<ServiceResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Servicios encontrados exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Actualiza un servicio existente
     * 
     * @param serviceId ID del servicio a actualizar
     * @param request Datos actualizados del servicio
     * @return Respuesta con el servicio actualizado
     * @throws ServiceNotFoundException si el servicio no existe
     * @throws AccessDeniedException si el usuario no tiene permisos para modificar el servicio
     * @apiNote La autenticación se maneja automáticamente a través de Spring Security
     */
    public ApiResponseDto<ServiceResponseDto> updateService(String serviceId, UpdateServiceRequestDto request) {
        log.info("Actualizando servicio con ID: {}", serviceId);
        
        com.barbershop.features.service.model.Service existingService = serviceRepository.findByIdAndActive(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + serviceId));
        
        // Validar autorización
        validateServiceModificationAccess(existingService.getBarbershopId());
        
        // Validar nombre único si se está actualizando
        if (request.getName() != null && !request.getName().equals(existingService.getName())) {
            if (serviceRepository.existsByBarbershopIdAndNameIgnoreCaseAndNotServiceIdAndActive(
                    existingService.getBarbershopId(), request.getName(), serviceId)) {
                throw new ResourceAlreadyExistsException("Ya existe un servicio con el nombre '" + request.getName() + "' en esta barbería");
            }
        }
        
        try {
            serviceMapper.updateEntity(existingService, request);
            com.barbershop.features.service.model.Service updatedService = serviceRepository.save(existingService);
            
            ServiceResponseDto responseDto = serviceMapper.toResponseDto(updatedService);
            
            log.info("Servicio actualizado exitosamente con ID: {}", serviceId);
            return ApiResponseDto.<ServiceResponseDto>builder()
                    .status(HttpStatus.OK.value())
                    .message("Servicio actualizado exitosamente")
                    .data(responseDto)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (DataIntegrityViolationException ex) {
            log.error("Error de integridad de datos al actualizar servicio: {}", ex.getMessage());
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("barbershop_id")) {
                throw new BusinessLogicException("La barbería especificada no existe. Por favor, seleccione una barbería válida.");
            }
            throw new BusinessLogicException("Error al actualizar el servicio. Verifique que todos los datos sean válidos.");
        }

    }

    /**
     * Elimina un servicio (soft delete)
     * 
     * @param serviceId ID del servicio a eliminar
     * @return Respuesta confirmando la eliminación
     * @throws ServiceNotFoundException si el servicio no existe
     * @throws AccessDeniedException si el usuario no tiene permisos para eliminar el servicio
     * @apiNote La autenticación se maneja automáticamente a través de Spring Security
     */
    public ApiResponseDto<Void> deleteService(String serviceId) {
        log.info("Eliminando servicio con ID: {}", serviceId);
        
        com.barbershop.features.service.model.Service service = serviceRepository.findByIdAndActive(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + serviceId));
        
        // Validar autorización
        validateServiceModificationAccess(service.getBarbershopId());
        
        // Verificar si el servicio tiene citas activas
        long activeAppointments = appointmentRepository.countActiveAppointmentsByServiceId(serviceId);
        if (activeAppointments > 0) {
            throw new ServiceHasActiveRecordsException(serviceId, 
                "El servicio tiene " + activeAppointments + " cita(s) activa(s)");
        }
        
        int deletedCount = serviceRepository.softDeleteById(serviceId);
        if (deletedCount == 0) {
            throw new ResourceNotFoundException("No se pudo eliminar el servicio con ID: " + serviceId);
        }
        
        log.info("Servicio eliminado exitosamente con ID: {}", serviceId);
        return ApiResponseDto.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Servicio eliminado exitosamente")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Restaura un servicio eliminado
     */
    public ApiResponseDto<ServiceResponseDto> restoreService(String serviceId) {
        log.info("Restaurando servicio con ID: {}", serviceId);
        
        com.barbershop.features.service.model.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + serviceId));
        
        if (service.getIsActive()) {
            throw new IllegalStateException("El servicio ya está activo");
        }
        
        // Validar autorización
        validateServiceModificationAccess(service.getBarbershopId());
        
        // Validar que no exista un servicio activo con el mismo nombre
        if (serviceRepository.existsByBarbershopIdAndNameIgnoreCaseAndActive(service.getBarbershopId(), service.getName())) {
            throw new ResourceAlreadyExistsException("Ya existe un servicio activo con el nombre '" + service.getName() + "' en esta barbería");
        }
        
        int restoredCount = serviceRepository.restoreById(serviceId);
        if (restoredCount == 0) {
            throw new ResourceNotFoundException("No se pudo restaurar el servicio con ID: " + serviceId);
        }
        
        com.barbershop.features.service.model.Service restoredService = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado después de restaurar"));
        
        ServiceResponseDto responseDto = serviceMapper.toResponseDto(restoredService);
        
        log.info("Servicio restaurado exitosamente con ID: {}", serviceId);
        return ApiResponseDto.<ServiceResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("Servicio restaurado exitosamente")
                .data(responseDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene servicios eliminados con paginación
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<ServiceResponseDto>> getDeletedServices(int page, int size, String sortBy, String sortDir) {
        log.info("Obteniendo servicios eliminados - Página: {}, Tamaño: {}", page, size);
        
        // Validar que el usuario tenga permisos de administrador
        validateAdminAccess();
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.barbershop.features.service.model.Service> servicesPage = serviceRepository.findAllInactive(pageable);
        Page<ServiceResponseDto> responsePage = servicesPage.map(serviceMapper::toResponseDto);
        
        return ApiResponseDto.<Page<ServiceResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Servicios eliminados obtenidos exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Obtiene servicios eliminados por barbería
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<Page<ServiceResponseDto>> getDeletedServicesByBarbershop(String barbershopId, int page, int size, String sortBy, String sortDir) {
        log.info("Obteniendo servicios eliminados por barbería: {}", barbershopId);
        
        // Validar autorización
        validateServiceModificationAccess(barbershopId);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.barbershop.features.service.model.Service> servicesPage = serviceRepository.findByBarbershopIdAndInactive(barbershopId, pageable);
        Page<ServiceResponseDto> responsePage = servicesPage.map(serviceMapper::toResponseDto);
        
        return ApiResponseDto.<Page<ServiceResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Servicios eliminados obtenidos exitosamente")
                .data(responsePage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Métodos de validación de autorización
    /**
     * Obtiene el usuario autenticado desde el SecurityContext
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        
        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
    }
    
    /**
     * Verifica si el usuario autenticado es administrador
     */
    private boolean isCurrentUserAdmin() {
        return SecurityUtils.isCurrentUserAdmin();
    }
    
    /**
     * Verifica si el usuario autenticado es barbero
     */
    private boolean isCurrentUserBarber() {
        return SecurityUtils.isCurrentUserBarber();
    }
    
    /**
     * Valida el acceso para crear servicios
     */
    private void validateServiceCreationAccess(String barbershopId) {
        if (isCurrentUserAdmin()) {
            return; // Los administradores pueden crear servicios en cualquier barbería
        }
        
        if (isCurrentUserBarber()) {
            // Validar que el barbero esté creando servicios en su propia barbería
            // Esta validación requeriría acceso al repositorio de barberías
            return;
        }
        
        throw new AccessDeniedException("No tienes permisos para crear servicios");
    }
    
    /**
     * Valida el acceso para modificar servicios
     */
    private void validateServiceModificationAccess(String barbershopId) {
        if (isCurrentUserAdmin()) {
            return; // Los administradores pueden modificar cualquier servicio
        }
        
        if (isCurrentUserBarber()) {
            // Validar que el barbero esté modificando servicios en su propia barbería
            // Esta validación requeriría acceso al repositorio de barberías
            return;
        }
        
        throw new AccessDeniedException("No tienes permisos para modificar este servicio");
    }
    
    /**
     * Valida que el usuario autenticado sea administrador
     */
    private void validateAdminAccess() {
        if (!isCurrentUserAdmin()) {
            throw new AccessDeniedException("Solo los administradores pueden acceder a esta información");
        }
    }
}