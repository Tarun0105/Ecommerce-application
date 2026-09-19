package com.shopverse.exception;
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName, int requested, int available) {
        super("Insufficient stock for '" + productName + "'. Requested: " + requested + ", Available: " + available);
    }
}
