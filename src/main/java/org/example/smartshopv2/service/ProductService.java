package org.example.smartshopv2.service;

import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.dto.ProductRequest;
import org.example.smartshopv2.dto.ProductResponse;
import org.example.smartshopv2.entity.Product;
import org.example.smartshopv2.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPriceHT(request.getPriceHT());
        product.setStockQuantity(request.getStockQuantity());
        
        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }
    
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.getDeleted()) {
            throw new RuntimeException("Product not found");
        }
        
        return mapToResponse(product);
    }
    
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByDeletedFalse(pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.getDeleted()) {
            throw new RuntimeException("Product not found");
        }
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPriceHT(request.getPriceHT());
        product.setStockQuantity(request.getStockQuantity());
        
        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setDeleted(true);
        productRepository.save(product);
    }
    
    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPriceHT(product.getPriceHT());
        response.setStockQuantity(product.getStockQuantity());
        return response;
    }
}
