package com.shopverse.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
@Data @NoArgsConstructor @AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 255)
    private String name;
    private String description;
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
    private Long categoryId;
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
    @NotBlank(message = "SKU is required")
    @Size(max = 100)
    private String sku;
    private String imageUrl;
    private boolean active = true;
}
