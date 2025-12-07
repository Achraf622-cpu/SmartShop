package org.example.smartshopv2.controller;

import org.example.smartshopv2.entity.User;
import org.example.smartshopv2.enums.Role;
import org.example.smartshopv2.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for AuthController
 * 
 * These tests verify HTTP endpoints work correctly
 * Uses MockMvc to simulate HTTP requests
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @Test
    @DisplayName("POST /api/auth/login - Should return 200 with valid credentials")
    void testLogin_ValidCredentials_Returns200() throws Exception {
        // ARRANGE
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("admin123");
        user.setRole(Role.ADMIN);
        
        when(authService.authenticate("admin", "admin123"))
            .thenReturn(user);
        
        String requestBody = """
            {
                "username": "admin",
                "password": "admin123"
            }
            """;
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.role").value("ADMIN"));
        
        verify(authService, times(1)).authenticate("admin", "admin123");
    }
    
    @Test
    @DisplayName("POST /api/auth/login - Should return 400 with invalid credentials")
    void testLogin_InvalidCredentials_Returns400() throws Exception {
        // ARRANGE
        when(authService.authenticate("admin", "wrongpassword"))
            .thenThrow(new RuntimeException("Invalid username or password"));
        
        String requestBody = """
            {
                "username": "admin",
                "password": "wrongpassword"
            }
            """;
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }
    
    @Test
    @DisplayName("POST /api/auth/login - Should return 400 when username is blank")
    void testLogin_BlankUsername_Returns400() throws Exception {
        // ARRANGE
        String requestBody = """
            {
                "username": "",
                "password": "password"
            }
            """;
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/auth/login - Should create session with userId and role")
    void testLogin_Success_CreatesSession() throws Exception {
        // ARRANGE
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setRole(Role.ADMIN);
        
        when(authService.authenticate("admin", "admin123"))
            .thenReturn(user);
        
        String requestBody = """
            {
                "username": "admin",
                "password": "admin123"
            }
            """;
        
        // ACT & ASSERT
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession();
        
        // Verify session attributes
        assert session != null;
        assert session.getAttribute("userId").equals(1L);
        assert session.getAttribute("username").equals("admin");
        assert session.getAttribute("role").equals("ADMIN");
    }
    
    @Test
    @DisplayName("POST /api/auth/logout - Should return 200 and invalidate session")
    void testLogout_Success_Returns200() throws Exception {
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logout successful"));
    }
    
    @Test
    @DisplayName("GET /api/auth/session - Should return 200 when authenticated")
    void testGetSession_Authenticated_Returns200() throws Exception {
        // ARRANGE
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("username", "admin");
        session.setAttribute("role", "ADMIN");
        
        // ACT & ASSERT
        mockMvc.perform(get("/api/auth/session")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }
    
    @Test
    @DisplayName("GET /api/auth/session - Should return 401 when not authenticated")
    void testGetSession_NotAuthenticated_Returns401() throws Exception {
        // ACT & ASSERT
        mockMvc.perform(get("/api/auth/session"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Not authenticated"));
    }
}
