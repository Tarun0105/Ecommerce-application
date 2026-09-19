package com.shopverse.service;

import com.shopverse.dto.request.AddToCartRequest;
import com.shopverse.dto.request.UpdateCartItemRequest;
import com.shopverse.dto.response.CartResponse;
import com.shopverse.entity.Cart;
import com.shopverse.entity.CartItem;
import com.shopverse.entity.Product;
import com.shopverse.entity.User;
import com.shopverse.entity.UserRole;
import com.shopverse.exception.InsufficientStockException;
import com.shopverse.exception.ResourceNotFoundException;
import com.shopverse.exception.UnauthorizedException;
import com.shopverse.mapper.CartMapper;
import com.shopverse.repository.CartItemRepository;
import com.shopverse.repository.CartRepository;
import com.shopverse.repository.ProductRepository;
import com.shopverse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock CartMapper cartMapper;
    @InjectMocks CartService cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).email("user@test.com").firstName("Test").lastName("User")
                .role(UserRole.ROLE_USER).enabled(true).build();
        testCart = Cart.builder().id(1L).user(testUser).items(new ArrayList<>()).build();
        testProduct = Product.builder()
                .id(10L).name("Laptop").sku("LAP-001")
                .price(BigDecimal.valueOf(999.99)).stockQuantity(5).active(true)
                .build();
    }

    // --- getCart() ---

    @Test
    void getCart_existingCart_returnsMappedResponse() {
        CartResponse expected = CartResponse.builder().id(1L).totalItems(0).totalAmount(BigDecimal.ZERO).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartMapper.toCartResponse(testCart)).thenReturn(expected);

        CartResponse result = cartService.getCart("user@test.com");

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getCart_noCartExists_createsAndReturnsNewCart() {
        Cart newCart = Cart.builder().id(2L).user(testUser).items(new ArrayList<>()).build();
        CartResponse expected = CartResponse.builder().id(2L).totalItems(0).totalAmount(BigDecimal.ZERO).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);
        when(cartMapper.toCartResponse(newCart)).thenReturn(expected);

        CartResponse result = cartService.getCart("user@test.com");

        assertThat(result.getId()).isEqualTo(2L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getCart_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCart("ghost@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- addToCart() ---

    @Test
    void addToCart_newItem_addsToCart() {
        AddToCartRequest req = new AddToCartRequest(10L, 2);
        Cart updatedCart = Cart.builder().id(1L).user(testUser).items(new ArrayList<>()).build();
        CartResponse expected = CartResponse.builder().id(1L).totalItems(2).build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(CartItem.builder().id(1L).build());
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(updatedCart));
        when(cartMapper.toCartResponse(updatedCart)).thenReturn(expected);

        CartResponse result = cartService.addToCart("user@test.com", req);

        assertThat(result).isNotNull();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_existingItem_incrementsQuantity() {
        CartItem existingItem = CartItem.builder().id(5L).cart(testCart).product(testProduct).quantity(1).build();
        AddToCartRequest req = new AddToCartRequest(10L, 2);
        Cart updatedCart = Cart.builder().id(1L).user(testUser).items(new ArrayList<>()).build();
        CartResponse expected = CartResponse.builder().id(1L).totalItems(3).build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(existingItem)).thenReturn(existingItem);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(updatedCart));
        when(cartMapper.toCartResponse(updatedCart)).thenReturn(expected);

        CartResponse result = cartService.addToCart("user@test.com", req);

        assertThat(existingItem.getQuantity()).isEqualTo(3);
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void addToCart_exceedsStock_throwsInsufficientStockException() {
        AddToCartRequest req = new AddToCartRequest(10L, 10); // stock is only 5
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart("user@test.com", req))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Laptop");
    }

    @Test
    void addToCart_existingItemExceedsStock_throwsInsufficientStockException() {
        CartItem existingItem = CartItem.builder().id(5L).cart(testCart).product(testProduct).quantity(4).build();
        AddToCartRequest req = new AddToCartRequest(10L, 3); // 4 + 3 = 7 > 5 stock
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(existingItem));

        assertThatThrownBy(() -> cartService.addToCart("user@test.com", req))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void addToCart_productNotFound_throwsResourceNotFoundException() {
        AddToCartRequest req = new AddToCartRequest(999L, 1);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart("user@test.com", req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- updateCartItem() ---

    @Test
    void updateCartItem_validRequest_updatesQuantity() {
        CartItem item = CartItem.builder().id(7L).cart(testCart).product(testProduct).quantity(1).build();
        UpdateCartItemRequest req = new UpdateCartItemRequest(3);
        Cart updatedCart = Cart.builder().id(1L).user(testUser).items(new ArrayList<>()).build();
        CartResponse expected = CartResponse.builder().id(1L).totalItems(3).build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(7L)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(item)).thenReturn(item);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(updatedCart));
        when(cartMapper.toCartResponse(updatedCart)).thenReturn(expected);

        CartResponse result = cartService.updateCartItem("user@test.com", 7L, req);

        assertThat(item.getQuantity()).isEqualTo(3);
        verify(cartItemRepository).save(item);
    }

    @Test
    void updateCartItem_itemBelongsToOtherCart_throwsUnauthorizedException() {
        Cart otherCart = Cart.builder().id(99L).user(testUser).items(new ArrayList<>()).build();
        CartItem item = CartItem.builder().id(7L).cart(otherCart).product(testProduct).quantity(1).build();
        UpdateCartItemRequest req = new UpdateCartItemRequest(2);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(7L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.updateCartItem("user@test.com", 7L, req))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateCartItem_exceedsStock_throwsInsufficientStockException() {
        CartItem item = CartItem.builder().id(7L).cart(testCart).product(testProduct).quantity(1).build();
        UpdateCartItemRequest req = new UpdateCartItemRequest(100); // stock is 5

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(7L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.updateCartItem("user@test.com", 7L, req))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void updateCartItem_itemNotFound_throwsResourceNotFoundException() {
        UpdateCartItemRequest req = new UpdateCartItemRequest(2);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateCartItem("user@test.com", 999L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- removeFromCart() ---

    @Test
    void removeFromCart_existingItem_removesAndReturnsCart() {
        CartItem item = CartItem.builder().id(7L).cart(testCart).product(testProduct).quantity(1).build();
        testCart.getItems().add(item);
        Cart updatedCart = Cart.builder().id(1L).user(testUser).items(new ArrayList<>()).build();
        CartResponse expected = CartResponse.builder().id(1L).totalItems(0).build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(7L)).thenReturn(Optional.of(item));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(updatedCart));
        when(cartMapper.toCartResponse(updatedCart)).thenReturn(expected);

        CartResponse result = cartService.removeFromCart("user@test.com", 7L);

        assertThat(result.getTotalItems()).isEqualTo(0);
        verify(cartItemRepository).delete(item);
    }

    @Test
    void removeFromCart_itemBelongsToOtherCart_throwsUnauthorizedException() {
        Cart otherCart = Cart.builder().id(99L).user(testUser).items(new ArrayList<>()).build();
        CartItem item = CartItem.builder().id(7L).cart(otherCart).product(testProduct).quantity(1).build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(7L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.removeFromCart("user@test.com", 7L))
                .isInstanceOf(UnauthorizedException.class);

        verify(cartItemRepository, never()).delete(any());
    }

    // --- clearCart() ---

    @Test
    void clearCart_clearsAllItemsAndSaves() {
        CartItem item = CartItem.builder().id(7L).cart(testCart).product(testProduct).quantity(1).build();
        testCart.getItems().add(item);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(testCart)).thenReturn(testCart);

        cartService.clearCart("user@test.com");

        assertThat(testCart.getItems()).isEmpty();
        verify(cartRepository).save(testCart);
    }
}
