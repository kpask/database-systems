package org.pharmacy.model;

public record Supplier(
        long id,
        String name,
        Address address
) {
    // Compact Constructor for validation
    public Supplier {
        if (id < 0) {
            throw new IllegalArgumentException("Supplier ID cannot be negative.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Supplier name cannot be empty.");
        }
        if (address == null) {
            throw new IllegalArgumentException("Supplier must have an address.");
        }
    }
}