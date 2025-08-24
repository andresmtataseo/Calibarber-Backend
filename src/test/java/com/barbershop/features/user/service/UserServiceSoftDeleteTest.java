package com.barbershop.features.user.service;

import com.barbershop.features.auth.exception.UserNotFoundException;
import com.barbershop.features.user.dto.UserResponseDto;
import com.barbershop.features.user.mapper.UserMapper;
import com.barbershop.features.user.model.User;
import com.barbershop.features.user.model.enums.RoleEnum;
import com.barbershop.features.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceSoftDeleteTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponseDto testUserResponseDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId("user-123");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(RoleEnum.CLIENT);
        testUser.setIsActive(true);
        testUser.setIsDeleted(false);
        testUser.setCreatedAt(LocalDateTime.now());

        testUserResponseDto = new UserResponseDto();
        testUserResponseDto.setUserId("user-123");
        testUserResponseDto.setEmail("test@example.com");
        testUserResponseDto.setFirstName("Test");
        testUserResponseDto.setLastName("User");
        testUserResponseDto.setRole(RoleEnum.CLIENT);
    }

    @Test
    void deleteUser_ShouldPerformSoftDelete_WhenUserExists() {
        // Given
        when(userRepository.findByIdAndNotDeleted("user-123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deleteUser("user-123");

        // Then
        verify(userRepository).findByIdAndNotDeleted("user-123");
        verify(userRepository).save(testUser);
        assertTrue(testUser.getIsDeleted());
        assertNotNull(testUser.getDeletedAt());
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByIdAndNotDeleted("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser("nonexistent"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void restoreUser_ShouldRestoreDeletedUser_WhenUserIsDeleted() {
        // Given
        testUser.setIsDeleted(true);
        testUser.setDeletedAt(LocalDateTime.now());
        
        when(userRepository.findByIdAndDeleted("user-123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDto(testUser)).thenReturn(testUserResponseDto);

        // When
        UserResponseDto result = userService.restoreUser("user-123");

        // Then
        verify(userRepository).findByIdAndDeleted("user-123");
        verify(userRepository).save(testUser);
        verify(userMapper).toResponseDto(testUser);
        assertFalse(testUser.getIsDeleted());
        assertNull(testUser.getDeletedAt());
        assertEquals(testUserResponseDto, result);
    }

    @Test
    void restoreUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByIdAndDeleted("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.restoreUser("nonexistent"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getDeletedUsers_ShouldReturnPageOfDeletedUsers() {
        // Given
        User deletedUser1 = new User();
        deletedUser1.setUserId("deleted-1");
        deletedUser1.setIsDeleted(true);
        
        User deletedUser2 = new User();
        deletedUser2.setUserId("deleted-2");
        deletedUser2.setIsDeleted(true);

        Page<User> deletedUsersPage = new PageImpl<>(Arrays.asList(deletedUser1, deletedUser2));
        Pageable pageable = PageRequest.of(0, 10);
        
        UserResponseDto responseDto1 = new UserResponseDto();
        responseDto1.setUserId("deleted-1");
        
        UserResponseDto responseDto2 = new UserResponseDto();
        responseDto2.setUserId("deleted-2");

        when(userRepository.findAllDeleted(pageable)).thenReturn(deletedUsersPage);
        when(userMapper.toResponseDto(deletedUser1)).thenReturn(responseDto1);
        when(userMapper.toResponseDto(deletedUser2)).thenReturn(responseDto2);

        // When
        Page<UserResponseDto> result = userService.getDeletedUsers(pageable);

        // Then
        verify(userRepository).findAllDeleted(pageable);
        assertEquals(2, result.getContent().size());
        assertEquals("deleted-1", result.getContent().get(0).getUserId());
        assertEquals("deleted-2", result.getContent().get(1).getUserId());
    }



    @Test
    void getUserById_ShouldOnlyReturnActiveUsers() {
        // Given
        when(userRepository.findByIdAndNotDeleted("user-123")).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDto(testUser)).thenReturn(testUserResponseDto);

        // When
        UserResponseDto result = userService.getUserById("user-123");

        // Then
        verify(userRepository).findByIdAndNotDeleted("user-123");
        verify(userRepository, never()).findById(anyString());
        assertEquals(testUserResponseDto, result);
    }

    @Test
    void getAllUsers_ShouldOnlyReturnActiveUsers() {
        // Given
        Page<User> activeUsersPage = new PageImpl<>(Arrays.asList(testUser));
        Pageable pageable = PageRequest.of(0, 10);
        
        when(userRepository.findAllActive(pageable)).thenReturn(activeUsersPage);
        when(userMapper.toResponseDto(testUser)).thenReturn(testUserResponseDto);

        // When
        Page<UserResponseDto> result = userService.getAllUsers(pageable);

        // Then
        verify(userRepository).findAllActive(pageable);
        verify(userRepository, never()).findAll(any(Pageable.class));
        assertEquals(1, result.getContent().size());
        assertEquals(testUserResponseDto, result.getContent().get(0));
    }
}