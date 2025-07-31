package com.barbershop.features.user.mapper;

import com.barbershop.features.user.dto.UserResponseDto;
import com.barbershop.features.user.dto.request.CreateUserRequestDto;
import com.barbershop.features.user.dto.request.UpdateUserRequestDto;
import com.barbershop.features.user.model.User;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "barbers", ignore = true)
    @Mapping(target = "clientAppointments", ignore = true)
    User toEntity(CreateUserRequestDto dto);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "barbers", ignore = true)
    @Mapping(target = "clientAppointments", ignore = true)
    void updateEntity(@MappingTarget User entity, UpdateUserRequestDto dto);

    UserResponseDto toResponseDto(User entity);

    @IterableMapping(elementTargetType = UserResponseDto.class)
    java.util.List<UserResponseDto> toResponseDtoList(java.util.List<User> entities);
}