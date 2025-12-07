package org.example.smartshopv2.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.dto.PagedResponse;
import org.example.smartshopv2.dto.ProductRequest;
import org.example.smartshopv2.dto.ProductResponse;
import org.example.smartshopv2.service.AuthorizationService;
import org.example.smartshopv2.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    private final AuthorizationService authService;
    
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest request, HttpSession session) {
        try {
            // Only ADMIN can create products
            authService.requireAdmin(session);
            ProductResponse response = productService.createProduct(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id, HttpSession session) {
        try {
            // Anyone authenticated can view products
            authService.requireAuthenticated(session);
            ProductResponse response = productService.getProduct(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           HttpSession session) {
        try {
            // Anyone authenticated can view products
            authService.requireAuthenticated(session);
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductResponse> products = productService.getAllProducts(pageable);
            
            // Wrap in user-friendly response
            PagedResponse<ProductResponse> response = PagedResponse.of(products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                          @Valid @RequestBody ProductRequest request,
                                          HttpSession session) {
        try {
            // Only ADMIN can update products
            authService.requireAdmin(session);
            ProductResponse response = productService.updateProduct(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, HttpSession session) {
        try {
            // Only ADMIN can delete products
            authService.requireAdmin(session);
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
