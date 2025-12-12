package org.pharmacy.model;

/**
 * Represents a medicine item available in stock.
 * This record enforces positive constraints on ID, price, and non-negative stock.
 *
 * @param id The unique identifier for the medicine.
 * @param name The commercial name of the medicine (cannot be null or blank).
 * @param unitPrice The selling price per unit (must be positive).
 * @param stock The current quantity in stock (cannot be negative).
 */
public record Medicine(
        long id,
        String name,
        double unitPrice,
        int stock
) {
    /**
     * Compact Constructor for validation, ensuring all numerical fields are valid
     * and the name is present.
     *
     * @throws IllegalArgumentException if ID is negative, name is blank, unitPrice is non-positive, or stock is negative.
     */
    public Medicine {
        if (id < 0) {
            throw new IllegalArgumentException("Medicine ID cannot be negative.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Medicine name cannot be empty.");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Unit price must be positive.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
    }
}