package com.shopverse.service;

import com.shopverse.dto.request.*;
import com.shopverse.dto.response.CartResponse;
import com.shopverse.entity.*;
import com.shopverse.exception.*;
import com.shopverse.mapper.CartMapper;
import com.shopverse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = Cart.builder().user(user).build();
            return cartRepository.save(cart);
        });
    }

    public CartResponse getCart(String userEmail) {
        User user = getUser(userEmail);
        Cart cart = getOrCreateCart(user);
        return cartMapper.toCartResponse(cart);
    }

    public CartResponse addToCart(String userEmail, AddToCartRequest request) {
        User user = getUser(userEmail);
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findByIdAndActiveTrue(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));
        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()).ifPresentOrElse(
            existingItem -> {
                int newQty = existingItem.getQuantity() + request.getQuantity();
                if (newQty > product.getStockQuantity()) {
                    throw new InsufficientStockException(product.getName(), newQty, product.getStockQuantity());
                }
                existingItem.setQuantity(newQty);
                cartItemRepository.save(existingItem);
            },
            () -> {
                if (request.getQuantity() > product.getStockQuantity()) {
                    throw new InsufficientStockException(product.getName(), request.getQuantity(), product.getStockQuantity());
                }
                CartItem item = CartItem.builder()
                        .cart(cart).product(product).quantity(request.getQuantity()).build();
                cart.getItems().add(item);
                cartItemRepository.save(item);
            }
        );
        Cart updated = cartRepository.findByUserId(user.getId()).orElse(cart);
        return cartMapper.toCartResponse(updated);
    }

    public CartResponse updateCartItem(String userEmail, Long itemId, UpdateCartItemRequest request) {
        User user = getUser(userEmail);
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Cart item does not belong to current user.");
        }
        Product product = item.getProduct();
        if (request.getQuantity() > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getName(), request.getQuantity(), product.getStockQuantity());
        }
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        Cart updated = cartRepository.findByUserId(user.getId()).orElse(cart);
        return cartMapper.toCartResponse(updated);
    }

    public CartResponse removeFromCart(String userEmail, Long itemId) {
        User user = getUser(userEmail);
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Cart item does not belong to current user.");
        }
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        Cart updated = cartRepository.findByUserId(user.getId()).orElse(cart);
        return cartMapper.toCartResponse(updated);
    }

    public void clearCart(String userEmail) {
        User user = getUser(userEmail);
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
