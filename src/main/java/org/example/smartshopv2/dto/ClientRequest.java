package org.example.smartshopv2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequest {
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    private String contactName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    
    private String address;
}
