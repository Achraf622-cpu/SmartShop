package org.example.smartshopv2.service;

import org.example.smartshopv2.entity.User;
import org.example.smartshopv2.enums.Role;
import org.example.smartshopv2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 * 
 * These tests use MOCKS (fake objects) - no real database!
 * Fast and isolated.
 */
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create a test user (reused in multiple tests)
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setPassword("admin123");
        testUser.setRole(Role.ADMIN);
    }
    
    @Test
    @DisplayName("Should authenticate user with valid credentials")
    void testAuthenticate_ValidCredentials_ReturnsUser() {
        // ARRANGE (Setup)
        // Tell mock: when findByUsername("admin") is called, return testUser
        when(userRepository.findByUsername("admin"))
            .thenReturn(Optional.of(testUser));
        
        // ACT (Execute)
        User result = authService.authenticate("admin", "admin123");
        
        // ASSERT (Verify)
        assertNotNull(result, "Authenticated user should not be null");
        assertEquals("admin", result.getUsername(), "Username should match");
        assertEquals(Role.ADMIN, result.getRole(), "Role should be ADMIN");
        assertEquals(1L, result.getId(), "User ID should match");
        
        // Verify the repository was called exactly once
        verify(userRepository, times(1)).findByUsername("admin");
    }
    
    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void testAuthenticate_WrongPassword_ThrowsException() {
        // ARRANGE
        when(userRepository.findByUsername("admin"))
            .thenReturn(Optional.of(testUser));
        
        // ACT & ASSERT
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.authenticate("admin", "wrongpassword"),
            "Should throw exception for wrong password"
        );
        
        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("admin");
    }
    
    @Test
    @DisplayName("Should throw exception when user does not exist")
    void testAuthenticate_UserNotFound_ThrowsException() {
        // ARRANGE
        // Mock returns empty (user doesn't exist)
        when(userRepository.findByUsername("nonexistent"))
            .thenReturn(Optional.empty());
        
        // ACT & ASSERT
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.authenticate("nonexistent", "anypassword"),
            "Should throw exception when user not found"
        );
        
        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }
    
    @Test
    @DisplayName("Should throw exception when username is null")
    void testAuthenticate_NullUsername_ThrowsException() {
        // ARRANGE
        when(userRepository.findByUsername(null))
            .thenReturn(Optional.empty());
        
        // ACT & ASSERT
        assertThrows(
            RuntimeException.class,
            () -> authService.authenticate(null, "password"),
            "Should throw exception for null username"
        );
    }
    
    @Test
    @DisplayName("Should throw exception when password is null")
    void testAuthenticate_NullPassword_ThrowsException() {
        // ARRANGE
        when(userRepository.findByUsername("admin"))
            .thenReturn(Optional.of(testUser));
        
        // ACT & ASSERT
        assertThrows(
            RuntimeException.class,
            () -> authService.authenticate("admin", null),
            "Should throw exception for null password"
        );
    }
    
    @Test
    @DisplayName("Should authenticate CLIENT role correctly")
    void testAuthenticate_ClientRole_ReturnsUser() {
        // ARRANGE
        User clientUser = new User();
        clientUser.setId(2L);
        clientUser.setUsername("client1");
        clientUser.setPassword("pass123");
        clientUser.setRole(Role.CLIENT);
        
        when(userRepository.findByUsername("client1"))
            .thenReturn(Optional.of(clientUser));
        
        // ACT
        User result = authService.authenticate("client1", "pass123");
        
        // ASSERT
        assertNotNull(result);
        assertEquals(Role.CLIENT, result.getRole());
        assertEquals("client1", result.getUsername());
    }
}
