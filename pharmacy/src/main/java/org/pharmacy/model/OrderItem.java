package org.pharmacy.model;


/**
 * Represents a single item line within an order, capturing the price at the time of purchase.
 *
 * @param orderId The ID of the parent order (must be positive).
 * @param medicineId The ID of the medicine purchased (must be positive).
 * @param quantity The amount purchased (must be positive).
 * @param priceAtPurchase The price per unit at the time of the order (must be positive).
 */
public record OrderItem(
        long orderId,
        long medicineId,
        int quantity,
        double priceAtPurchase // The price per unit when the order was created
) {
    /**
     * Compact constructor for the OrderItem record, enforcing positive values for all components.
     *
     * @throws IllegalArgumentException if any ID, quantity, or priceAtPurchase is non-positive.
     */
    public OrderItem {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive.");
        }
        if (medicineId <= 0) {
            throw new IllegalArgumentException("Medicine ID must be positive.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if (priceAtPurchase <= 0) {
            throw new IllegalArgumentException("Price at purchase must be positive.");
        }
    }
}