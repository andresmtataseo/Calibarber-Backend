package com.barbershop.features.service.mapper;

import com.barbershop.features.service.dto.ServiceResponseDto;
import com.barbershop.features.service.dto.request.CreateServiceRequestDto;
import com.barbershop.features.service.dto.request.UpdateServiceRequestDto;
import com.barbershop.features.service.model.Service;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ServiceMapper {

    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "barbershop", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    Service toEntity(CreateServiceRequestDto dto);

    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "barbershopId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "barbershop", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    void updateEntity(@MappingTarget Service entity, UpdateServiceRequestDto dto);

    ServiceResponseDto toResponseDto(Service entity);

    @IterableMapping(elementTargetType = ServiceResponseDto.class)
    java.util.List<ServiceResponseDto> toResponseDtoList(java.util.List<Service> entities);
}