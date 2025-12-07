package org.example.smartshopv2.service;

import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.entity.User;
import org.example.smartshopv2.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid username or password");
        }
        
        return user;
    }
}
