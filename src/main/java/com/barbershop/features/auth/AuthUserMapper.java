package com.barbershop.features.auth;

import com.barbershop.features.auth.dto.SignUpRequestDto;
import com.barbershop.features.user.dto.UserResponseDto;
import com.barbershop.features.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthUserMapper {

    @Mapping(target = "role", ignore = true)
    User toUser(SignUpRequestDto signUpRequestDto);

    UserResponseDto toUserResponseDto(User user);
}
