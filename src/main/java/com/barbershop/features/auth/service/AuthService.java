package com.barbershop.features.auth.service;

import com.barbershop.features.auth.dto.AuthResponseDto;
import com.barbershop.features.auth.dto.SignInRequestDto;
import com.barbershop.features.auth.dto.SignUpRequestDto;
import com.barbershop.features.auth.security.JwtService;
import com.barbershop.features.auth.AuthUserMapper;
import com.barbershop.features.user.model.enums.RoleEnum;
import com.barbershop.features.user.repository.UserRepository;
import com.barbershop.features.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthUserMapper authUserMapper;

    public AuthResponseDto signIn(SignInRequestDto request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDetails user=userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token=jwtService.getToken(user);
        return AuthResponseDto.builder()
                .token(token)
                .type("Bearer")
                .user(authUserMapper.toUserResponseDto(userRepository.findByEmail(request.getEmail()).orElseThrow()))
                .build();

    }

    public AuthResponseDto signUp(SignUpRequestDto request) {
        User user = authUserMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(user.getPassword()));
        user.setRole(RoleEnum.CLIENT);
        user = userRepository.save(user);

        return AuthResponseDto.builder()
                .token(jwtService.getToken(user))
                .type("Bearer")
                .user(authUserMapper.toUserResponseDto(user))
                .build();
    }
}