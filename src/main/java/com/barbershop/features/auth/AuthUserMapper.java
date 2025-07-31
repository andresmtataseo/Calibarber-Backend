package com.barbershop.features.auth;

import com.barbershop.features.auth.dto.SignUpRequestDto;
import com.barbershop.features.user.dto.UserResponseDto;
import com.barbershop.features.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class AuthUserMapper {

    public User toUser(SignUpRequestDto signUpRequestDto) {
        if (signUpRequestDto == null) {
            return null;
        }
        
        User user = new User();
        user.setEmail(signUpRequestDto.getEmail());
        user.setFirstName(signUpRequestDto.getFirstName());
        user.setLastName(signUpRequestDto.getLastName());
        user.setPhoneNumber(signUpRequestDto.getPhoneNumber());
        user.setProfilePictureUrl(signUpRequestDto.getProfilePictureUrl());
        user.setPasswordHash(signUpRequestDto.getPassword()); // Mapeo manual del password
        
        return user;
    }

    public UserResponseDto toUserResponseDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIsActive(user.getIsActive());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        return dto;
    }
}
