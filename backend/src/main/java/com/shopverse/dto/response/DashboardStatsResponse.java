package com.shopverse.dto.response;
import lombok.*;
import java.math.BigDecimal;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStatsResponse {
    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private long pendingOrders;
    private BigDecimal totalRevenue;
}
