package org.example.smartshopv2.dto;

import lombok.Data;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Double priceHT;
    private Integer stockQuantity;
}
