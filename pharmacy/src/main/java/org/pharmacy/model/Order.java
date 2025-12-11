package org.pharmacy.Model;

import java.util.Date;

public record Order(
        long orderId,
        long clientId,
        Date orderDate,
        double totalPrice
) {
    public Order {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive.");
        }
        if (clientId <= 0) {
            throw new IllegalArgumentException("Client ID must be positive.");
        }
        if (orderDate == null) {
            throw new IllegalArgumentException("Order date cannot be null.");
        }
        if (totalPrice < 0) {
            throw new IllegalArgumentException("Total price cannot be negative.");
        }
    }
}