package com.barbershop.features.barbershop.mapper;

import com.barbershop.features.barbershop.dto.BarbershopOperatingHoursDto;
import com.barbershop.features.barbershop.dto.BarbershopOperatingHoursRequestDto;
import com.barbershop.features.barbershop.model.BarbershopOperatingHours;
import org.mapstruct.*;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Mapper para convertir entre DTOs y entidades de horarios de operación de barbería.
 * Utiliza MapStruct para generar automáticamente las implementaciones.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BarbershopOperatingHoursMapper {

    /**
     * Convierte un DTO de request a entidad para creación.
     * Ignora campos que se generan automáticamente.
     */
    @Mapping(target = "operatingHoursId", ignore = true)
    @Mapping(target = "barbershop", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BarbershopOperatingHours toEntity(BarbershopOperatingHoursRequestDto dto);

    /**
     * Actualiza una entidad existente con datos del DTO de request.
     * Ignora campos que no deben ser modificados.
     */
    @Mapping(target = "operatingHoursId", ignore = true)
    @Mapping(target = "barbershop", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(BarbershopOperatingHoursRequestDto dto, @MappingTarget BarbershopOperatingHours entity);

    /**
     * Convierte una entidad a DTO de respuesta.
     * Incluye campos calculados como el nombre del día y horario formateado.
     */
    @Mapping(target = "dayName", source = "dayOfWeek", qualifiedByName = "dayOfWeekToSpanishName")
    @Mapping(target = "formattedHours", source = ".", qualifiedByName = "formatHours")
    BarbershopOperatingHoursDto toResponseDto(BarbershopOperatingHours entity);

    /**
     * Convierte una lista de entidades a lista de DTOs de respuesta.
     */
    @IterableMapping(elementTargetType = BarbershopOperatingHoursDto.class)
    java.util.List<BarbershopOperatingHoursDto> toResponseDtoList(java.util.List<BarbershopOperatingHours> entities);

    /**
     * Convierte el día de la semana a su nombre en español.
     */
    @Named("dayOfWeekToSpanishName")
    default String dayOfWeekToSpanishName(DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            return null;
        }
        return dayOfWeek.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
    }

    /**
     * Formatea las horas de apertura y cierre para mostrar.
     */
    @Named("formatHours")
    default String formatHours(BarbershopOperatingHours entity) {
        if (entity == null) {
            return null;
        }
        
        if (Boolean.TRUE.equals(entity.getIsClosed())) {
            return "Cerrado";
        }
        
        if (entity.getOpeningTime() == null || entity.getClosingTime() == null) {
            return "Horario no definido";
        }
        
        return String.format("%s - %s", 
            entity.getOpeningTime().toString(), 
            entity.getClosingTime().toString());
    }
}