package com.shopverse.mapper;
import com.shopverse.dto.response.*;
import com.shopverse.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        Long productId = item.getProduct() != null ? item.getProduct().getId() : null;
        return OrderItemResponse.builder()
                .id(item.getId()).productId(productId)
                .productName(item.getProductName()).productSku(item.getProductSku())
                .unitPrice(item.getUnitPrice()).quantity(item.getQuantity()).subtotal(subtotal)
                .build();
    }

    public OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toOrderItemResponse).collect(Collectors.toList());
        ShippingAddress sa = order.getShippingAddress();
        ShippingAddressResponse addressResponse = sa == null ? null : ShippingAddressResponse.builder()
                .firstName(sa.getFirstName()).lastName(sa.getLastName())
                .street(sa.getStreet()).city(sa.getCity())
                .state(sa.getState()).zipCode(sa.getZipCode()).country(sa.getCountry())
                .build();
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .items(items).shippingAddress(addressResponse)
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount()).notes(order.getNotes())
                .createdAt(order.getCreatedAt()).updatedAt(order.getUpdatedAt())
                .build();
    }

    public Page<OrderResponse> toResponsePage(Page<Order> page) {
        return page.map(this::toOrderResponse);
    }
}
