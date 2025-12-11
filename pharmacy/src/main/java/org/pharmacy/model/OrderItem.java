package org.pharmacy.Model;


public record OrderItem(
        long orderId,
        long medicineId,
        int quantity,
        double priceAtPurchase // The price per unit when the order was created
) {
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