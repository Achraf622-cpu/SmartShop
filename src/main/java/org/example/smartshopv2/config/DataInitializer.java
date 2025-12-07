package org.example.smartshopv2.config;

import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.entity.User;
import org.example.smartshopv2.enums.Role;
import org.example.smartshopv2.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    
    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Default admin user created: username=admin, password=admin123");
        }
    }
}
