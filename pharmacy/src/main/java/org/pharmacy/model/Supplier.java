package org.pharmacy.model;

/**
 * Represents a supplier entity for the pharmacy, providing medicines.
 *
 * @param id The unique identifier for the supplier.
 * @param name The legal name of the supplier (cannot be null or blank).
 * @param address The physical address of the supplier (cannot be null).
 */
public record Supplier(
        long id,
        String name,
        Address address
) {
    /**
     * Compact Constructor for validation, enforcing positive ID, non-blank name, and a valid Address.
     *
     * @throws IllegalArgumentException if ID is negative, name is null/blank, or address is null.
     */
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