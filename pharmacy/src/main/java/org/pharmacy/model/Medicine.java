package org.pharmacy.Model;
public record Medicine(
        long id,
        String name,
        double unitPrice,
        int stock
) {
    // Compact Constructor for validation
    public Medicine {
        if (id < 0) {
            throw new IllegalArgumentException("Medicine ID cannot be negative.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Medicine name cannot be empty.");
        }
        if (unitPrice <= 0) {
            // Price must be positive, as defined in DB CHECK constraint
            throw new IllegalArgumentException("Unit price must be positive.");
        }
        if (stock < 0) {
            // Stock cannot be negative, as defined in DB CHECK constraint
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
    }
}