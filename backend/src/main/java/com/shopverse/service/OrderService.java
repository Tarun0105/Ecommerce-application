package com.shopverse.service;

import com.shopverse.dto.request.CheckoutRequest;
import com.shopverse.dto.response.*;
import com.shopverse.entity.*;
import com.shopverse.exception.*;
import com.shopverse.mapper.OrderMapper;
import com.shopverse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public OrderResponse checkout(String userEmail, CheckoutRequest request) {
        User user = getUser(userEmail);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty."));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout with an empty cart.");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
            if (cartItem.getQuantity() > product.getStockQuantity()) {
                throw new InsufficientStockException(product.getName(), cartItem.getQuantity(), product.getStockQuantity());
            }
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .unitPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
            orderItems.add(orderItem);
        }

        var addr = request.getShippingAddress();
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .firstName(addr.getFirstName()).lastName(addr.getLastName())
                .street(addr.getStreet()).city(addr.getCity())
                .state(addr.getState()).zipCode(addr.getZipCode()).country(addr.getCountry())
                .build();

        Order order = Order.builder()
                .user(user).shippingAddress(shippingAddress)
                .status(OrderStatus.PENDING).totalAmount(total)
                .notes(request.getNotes())
                .build();
        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);
        Order saved = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return orderMapper.toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getUserOrders(String userEmail, int page, int size) {
        User user = getUser(userEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findByUserId(user.getId(), pageable);
        return PageResponse.from(orderMapper.toResponsePage(orders));
    }

    @Transactional(readOnly = true)
    public OrderResponse getUserOrderById(String userEmail, Long orderId) {
        User user = getUser(userEmail);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Access denied to this order.");
        }
        return orderMapper.toOrderResponse(order);
    }

    public OrderResponse cancelOrder(String userEmail, Long orderId) {
        User user = getUser(userEmail);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Access denied to this order.");
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order can only be cancelled when PENDING or CONFIRMED. Current status: " + order.getStatus());
        }
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        OrderStatus statusEnum = StringUtils.hasText(status) ? OrderStatus.valueOf(status) : null;
        Page<Order> orders = orderRepository.findAllWithStatusFilter(statusEnum, pageable);
        return PageResponse.from(orderMapper.toResponsePage(orders));
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        order.setStatus(newStatus);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }
}
