package com.barbershop.features.barbershop.mapper;

import com.barbershop.features.barbershop.dto.BarbershopCreateDto;
import com.barbershop.features.barbershop.dto.BarbershopResponseDto;
import com.barbershop.features.barbershop.dto.BarbershopUpdateDto;
import com.barbershop.features.barbershop.model.Barbershop;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BarbershopMapper {

    @Mapping(target = "barbershopId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "barbers", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "operatingHours", ignore = true)
    Barbershop toEntity(BarbershopCreateDto dto);

    @Mapping(target = "barbershopId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "barbers", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "operatingHours", ignore = true)
    void updateEntityFromDto(BarbershopUpdateDto dto, @MappingTarget Barbershop entity);

    @Mapping(target = "operatingHours", ignore = true)
    BarbershopResponseDto toResponseDto(Barbershop entity);

    @IterableMapping(elementTargetType = BarbershopResponseDto.class)
    java.util.List<BarbershopResponseDto> toResponseDtoList(java.util.List<Barbershop> entities);
}