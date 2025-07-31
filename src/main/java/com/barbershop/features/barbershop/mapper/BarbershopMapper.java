package com.barbershop.features.barbershop.mapper;

import com.barbershop.features.barbershop.dto.BarbershopResponseDto;
import com.barbershop.features.barbershop.dto.request.CreateBarbershopRequestDto;
import com.barbershop.features.barbershop.dto.request.UpdateBarbershopRequestDto;
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
    Barbershop toEntity(CreateBarbershopRequestDto dto);

    @Mapping(target = "barbershopId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "barbers", ignore = true)
    @Mapping(target = "services", ignore = true)
    void updateEntity(@MappingTarget Barbershop entity, UpdateBarbershopRequestDto dto);

    BarbershopResponseDto toResponseDto(Barbershop entity);

    @IterableMapping(elementTargetType = BarbershopResponseDto.class)
    java.util.List<BarbershopResponseDto> toResponseDtoList(java.util.List<Barbershop> entities);
}