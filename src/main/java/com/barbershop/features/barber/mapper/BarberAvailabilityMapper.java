package com.barbershop.features.barber.mapper;

import com.barbershop.features.barber.dto.BarberAvailabilityResponseDto;
import com.barbershop.features.barber.dto.request.CreateBarberAvailabilityRequestDto;
import com.barbershop.features.barber.model.BarberAvailability;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BarberAvailabilityMapper {

    @Mapping(target = "barberAvailabilityId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "barber", ignore = true)
    BarberAvailability toEntity(CreateBarberAvailabilityRequestDto dto);

    BarberAvailabilityResponseDto toResponseDto(BarberAvailability entity);

    @IterableMapping(elementTargetType = BarberAvailabilityResponseDto.class)
    java.util.List<BarberAvailabilityResponseDto> toResponseDtoList(java.util.List<BarberAvailability> entities);
}