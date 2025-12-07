package org.example.smartshopv2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * User-friendly wrapper for paginated API responses
 * Hides Spring Data's internal pagination structure
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  
public class PagedResponse<T> {
    
    private boolean success;
    private List<T> data;
    private String message;  
    private PaginationInfo pagination;
    
    /**
     * Create from Spring Data Page object
     */
    public static <T> PagedResponse<T> of(Page<T> page) {
        PaginationInfo pagination = new PaginationInfo(
            page.getNumber() + 1,  
            page.getTotalPages(),
            page.getSize(),
            page.getTotalElements()
        );
        
        String message = page.isEmpty() ? "No items found" : null;
        
        return new PagedResponse<>(
            true,
            page.getContent(),
            message,
            pagination
        );
    }
    
    /**
     * Create from Spring Data Page with custom message
     */
    public static <T> PagedResponse<T> of(Page<T> page, String customMessage) {
        PagedResponse<T> response = of(page);
        response.setMessage(customMessage);
        return response;
    }
    
    /**
     * Simplified pagination info
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private int currentPage;   // 1-based (user-friendly)
        private int totalPages;
        private int pageSize;
        private long totalItems;
    }
}
