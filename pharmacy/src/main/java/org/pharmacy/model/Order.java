package org.pharmacy.model;

import java.util.Date;

/**
 * Represents a main order entity, linking a client to a date and total price.
 *
 * @param orderId The unique identifier for the order (must be positive).
 * @param clientId The ID of the client who placed the order (must be positive).
 * @param orderDate The date the order was placed (cannot be null).
 * @param totalPrice The final total price of the order (cannot be negative).
 */
public record Order(
        long orderId,
        long clientId,
        Date orderDate,
        double totalPrice
) {
    /**
     * Compact constructor for the Order record, enforcing positive IDs and non-null date.
     *
     * @throws IllegalArgumentException if orderId or clientId is non-positive, orderDate is null, or totalPrice is negative.
     */
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