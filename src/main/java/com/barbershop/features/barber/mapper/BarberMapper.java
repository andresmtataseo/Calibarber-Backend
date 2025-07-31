package com.barbershop.features.barber.mapper;

import com.barbershop.features.barber.dto.BarberResponseDto;
import com.barbershop.features.barber.dto.request.CreateBarberRequestDto;
import com.barbershop.features.barber.dto.request.UpdateBarberRequestDto;
import com.barbershop.features.barber.model.Barber;
import com.barbershop.features.user.mapper.UserMapper;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {UserMapper.class}
)
public interface BarberMapper {

    @Mapping(target = "barberId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "barbershop", ignore = true)
    @Mapping(target = "barberAvailabilities", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    Barber toEntity(CreateBarberRequestDto dto);

    @Mapping(target = "barberId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "barbershopId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "barbershop", ignore = true)
    @Mapping(target = "barberAvailabilities", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    void updateEntity(@MappingTarget Barber entity, UpdateBarberRequestDto dto);

    @Mapping(target = "user", source = "user")
    BarberResponseDto toResponseDto(Barber entity);

    @IterableMapping(elementTargetType = BarberResponseDto.class)
    java.util.List<BarberResponseDto> toResponseDtoList(java.util.List<Barber> entities);
}