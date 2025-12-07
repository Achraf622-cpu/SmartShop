package org.example.smartshopv2.service;

import jakarta.servlet.http.HttpSession;
import org.example.smartshopv2.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
    
    /**
     * Check if user is authenticated (has active session)
     */
    public void requireAuthenticated(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            throw new UnauthorizedException("Authentication required. Please login first.");
        }
    }
    
    /**
     * Check if user has ADMIN role
     */
    public void requireAdmin(HttpSession session) {
        requireAuthenticated(session);
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Access denied. Admin privileges required.");
        }
    }
    
    /**
     * Check if user has CLIENT role
     */
    public void requireClient(HttpSession session) {
        requireAuthenticated(session);
        String role = (String) session.getAttribute("role");
        if (!"CLIENT".equals(role)) {
            throw new UnauthorizedException("Access denied. Client role required.");
        }
    }
    
    /**
     * Check if user is accessing their own resource
     * @param session HTTP session
     * @param clientId The client ID being accessed
     */
    public void requireOwnerOrAdmin(HttpSession session, Long clientId) {
        requireAuthenticated(session);
        String role = (String) session.getAttribute("role");
        

        if ("ADMIN".equals(role)) {
            return;
        }
        

        Long sessionClientId = (Long) session.getAttribute("clientId");
        if (sessionClientId == null || !sessionClientId.equals(clientId)) {
            throw new UnauthorizedException("Access denied. You can only access your own resources.");
        }
    }
    
    /**
     * Get current user role from session
     */
    public String getRole(HttpSession session) {
        return (String) session.getAttribute("role");
    }
    
    /**
     * Get current user ID from session
     */
    public Long getUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }
    
    /**
     * Get current client ID from session (for CLIENT role)
     */
    public Long getClientId(HttpSession session) {
        return (Long) session.getAttribute("clientId");
    }
    
    /**
     * Check if current user is ADMIN
     */
    public boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("role"));
    }
}
