package org.example.smartshopv2.mapper;

import org.example.smartshopv2.dto.ProductRequest;
import org.example.smartshopv2.dto.ProductResponse;
import org.example.smartshopv2.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductRequest request);

    ProductResponse toResponse(Product product);
}
