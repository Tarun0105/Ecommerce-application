package com.shopverse.service;

import com.shopverse.dto.request.CheckoutRequest;
import com.shopverse.dto.request.ShippingAddressRequest;
import com.shopverse.dto.response.OrderResponse;
import com.shopverse.dto.response.PageResponse;
import com.shopverse.entity.*;
import com.shopverse.exception.BadRequestException;
import com.shopverse.exception.InsufficientStockException;
import com.shopverse.exception.ResourceNotFoundException;
import com.shopverse.exception.UnauthorizedException;
import com.shopverse.mapper.OrderMapper;
import com.shopverse.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock OrderMapper orderMapper;
    @InjectMocks OrderService orderService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem cartItem;
    private ShippingAddressRequest shippingAddressRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).email("user@test.com").firstName("Test").lastName("User")
                .role(UserRole.ROLE_USER).enabled(true).build();

        testProduct = Product.builder()
                .id(10L).name("Laptop").sku("LAP-001")
                .price(BigDecimal.valueOf(999.99)).stockQuantity(5).active(true)
                .build();

        testCart = Cart.builder().id(1L).user(testUser).items(new ArrayList<>()).build();

        cartItem = CartItem.builder()
                .id(1L).cart(testCart).product(testProduct).quantity(2)
                .build();
        testCart.getItems().add(cartItem);

        shippingAddressRequest = new ShippingAddressRequest(
                "Test", "User", "123 Main St", "Springfield", "IL", "62701", "US");
    }

    private OrderResponse buildOrderResponse(Long id, String status, BigDecimal total) {
        return OrderResponse.builder().id(id).status(status).totalAmount(total).build();
    }

    // --- checkout() ---

    @Test
    void checkout_validCart_createsOrderAndClearsCart() {
        CheckoutRequest req = new CheckoutRequest(shippingAddressRequest, "Leave at door");
        OrderResponse expectedResp = buildOrderResponse(1L, "PENDING", BigDecimal.valueOf(1999.98));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });
        when(cartRepository.save(testCart)).thenReturn(testCart);
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(expectedResp);

        OrderResponse result = orderService.checkout("user@test.com", req);

        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1999.98));
        // Stock should have been decremented
        assertThat(testProduct.getStockQuantity()).isEqualTo(3);
        // Cart should be cleared
        assertThat(testCart.getItems()).isEmpty();
        verify(cartRepository).save(testCart);
    }

    @Test
    void checkout_emptyCart_throwsBadRequestException() {
        testCart.getItems().clear();
        CheckoutRequest req = new CheckoutRequest(shippingAddressRequest, null);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> orderService.checkout("user@test.com", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void checkout_cartNotFound_throwsBadRequestException() {
        CheckoutRequest req = new CheckoutRequest(shippingAddressRequest, null);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.checkout("user@test.com", req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void checkout_insufficientStock_throwsInsufficientStockException() {
        testProduct.setStockQuantity(1); // less than cartItem quantity (2)
        CheckoutRequest req = new CheckoutRequest(shippingAddressRequest, null);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> orderService.checkout("user@test.com", req))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Laptop");
    }

    @Test
    void checkout_orderHasCorrectTotalAmount() {
        // 2 units at 999.99 = 1999.98
        CheckoutRequest req = new CheckoutRequest(shippingAddressRequest, null);
        OrderResponse expectedResp = buildOrderResponse(1L, "PENDING", BigDecimal.valueOf(1999.98));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> { Order o = inv.getArgument(0); o.setId(1L); return o; });
        when(cartRepository.save(testCart)).thenReturn(testCart);
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(expectedResp);

        OrderResponse result = orderService.checkout("user@test.com", req);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1999.98));
    }

    @Test
    void checkout_userNotFound_throwsResourceNotFoundException() {
        CheckoutRequest req = new CheckoutRequest(shippingAddressRequest, null);
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.checkout("ghost@test.com", req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getUserOrders() ---

    @Test
    void getUserOrders_returnsPageOfOrders() {
        Order order = Order.builder().id(1L).user(testUser).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.TEN).items(new ArrayList<>()).build();
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        Page<OrderResponse> responsePage = new PageImpl<>(List.of(buildOrderResponse(1L, "PENDING", BigDecimal.TEN)));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(orderPage);
        when(orderMapper.toResponsePage(orderPage)).thenReturn(responsePage);

        PageResponse<OrderResponse> result = orderService.getUserOrders("user@test.com", 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getUserOrders_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getUserOrders("ghost@test.com", 0, 10))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getUserOrderById() ---

    @Test
    void getUserOrderById_ownOrder_returnsOrderResponse() {
        Order order = Order.builder().id(5L).user(testUser).status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(500)).items(new ArrayList<>()).build();
        OrderResponse resp = buildOrderResponse(5L, "CONFIRMED", BigDecimal.valueOf(500));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(resp);

        OrderResponse result = orderService.getUserOrderById("user@test.com", 5L);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void getUserOrderById_otherUsersOrder_throwsUnauthorizedException() {
        User otherUser = User.builder().id(99L).email("other@test.com").build();
        Order order = Order.builder().id(5L).user(otherUser).status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(500)).items(new ArrayList<>()).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getUserOrderById("user@test.com", 5L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getUserOrderById_notFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getUserOrderById("user@test.com", 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- cancelOrder() ---

    @Test
    void cancelOrder_pendingOrder_setsCancelledAndRestoresStock() {
        Order order = Order.builder().id(3L).user(testUser).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(1999.98)).items(new ArrayList<>()).build();
        OrderItem oi = OrderItem.builder().id(1L).order(order).product(testProduct)
                .productName("Laptop").productSku("LAP-001")
                .unitPrice(BigDecimal.valueOf(999.99)).quantity(2).build();
        order.getItems().add(oi);

        OrderResponse resp = buildOrderResponse(3L, "CANCELLED", BigDecimal.valueOf(1999.98));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(resp);

        testProduct.setStockQuantity(3); // was 5, sold 2, now at 3

        OrderResponse result = orderService.cancelOrder("user@test.com", 3L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(testProduct.getStockQuantity()).isEqualTo(5); // restored 2
    }

    @Test
    void cancelOrder_confirmedOrder_cancelsSuccessfully() {
        Order order = Order.builder().id(4L).user(testUser).status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.TEN).items(new ArrayList<>()).build();
        OrderResponse resp = buildOrderResponse(4L, "CANCELLED", BigDecimal.TEN);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(resp);

        orderService.cancelOrder("user@test.com", 4L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_shippedOrder_throwsBadRequestException() {
        Order order = Order.builder().id(5L).user(testUser).status(OrderStatus.SHIPPED)
                .totalAmount(BigDecimal.TEN).items(new ArrayList<>()).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder("user@test.com", 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SHIPPED");
    }

    @Test
    void cancelOrder_otherUsersOrder_throwsUnauthorizedException() {
        User otherUser = User.builder().id(99L).email("other@test.com").build();
        Order order = Order.builder().id(5L).user(otherUser).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.TEN).items(new ArrayList<>()).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder("user@test.com", 5L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void cancelOrder_deliveredOrder_throwsBadRequestException() {
        Order order = Order.builder().id(6L).user(testUser).status(OrderStatus.DELIVERED)
                .totalAmount(BigDecimal.TEN).items(new ArrayList<>()).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder("user@test.com", 6L))
                .isInstanceOf(BadRequestException.class);
    }

    // --- updateOrderStatus() (admin) ---

    @Test
    void updateOrderStatus_validTransition_updatesStatus() {
        Order order = Order.builder().id(7L).user(testUser).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.TEN).items(new ArrayList<>()).build();
        OrderResponse resp = buildOrderResponse(7L, "CONFIRMED", BigDecimal.TEN);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(resp);

        OrderResponse result = orderService.updateOrderStatus(7L, OrderStatus.CONFIRMED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateOrderStatus_orderNotFound_throwsResourceNotFoundException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(999L, OrderStatus.CONFIRMED))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getAllOrders() (admin) ---

    @Test
    void getAllOrders_noStatusFilter_returnAllOrders() {
        Order order = Order.builder().id(1L).user(testUser).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.TEN).items(new ArrayList<>()).build();
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        Page<OrderResponse> responsePage = new PageImpl<>(List.of(buildOrderResponse(1L, "PENDING", BigDecimal.TEN)));
        when(orderRepository.findAllWithStatusFilter(isNull(), any(Pageable.class))).thenReturn(orderPage);
        when(orderMapper.toResponsePage(orderPage)).thenReturn(responsePage);

        PageResponse<OrderResponse> result = orderService.getAllOrders(0, 10, null);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllOrders_withStatusFilter_filtersOrders() {
        Order order = Order.builder().id(2L).user(testUser).status(OrderStatus.SHIPPED)
                .totalAmount(BigDecimal.valueOf(200)).items(new ArrayList<>()).build();
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        Page<OrderResponse> responsePage = new PageImpl<>(List.of(buildOrderResponse(2L, "SHIPPED", BigDecimal.valueOf(200))));
        when(orderRepository.findAllWithStatusFilter(any(OrderStatus.class), any(Pageable.class))).thenReturn(orderPage);
        when(orderMapper.toResponsePage(orderPage)).thenReturn(responsePage);

        PageResponse<OrderResponse> result = orderService.getAllOrders(0, 10, "SHIPPED");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("SHIPPED");
    }

    // helper for static import equivalence
    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
