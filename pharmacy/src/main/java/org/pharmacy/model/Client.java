package org.pharmacy.model;

/**
 * Represents a client entity in the pharmacy system.
 * This record ensures that the client's name and address are valid upon construction.
 *
 * @param id The unique identifier for the client.
 * @param firstname The first name of the client (cannot be null or blank).
 * @param lastname The last name of the client (cannot be null or blank).
 * @param address The address details of the client (cannot be null).
 */
public record Client(
        long id,
        String firstname,
        String lastname,
        Address address
) {
    /**
     * Compact constructor for the Client record, enforcing non-null/non-blank constraints
     * on the name fields and ensuring an Address object is provided.
     *
     * @throws IllegalArgumentException if firstname or lastname is null/blank, or if address is null.
     */
    public Client {
        if (firstname == null || firstname.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
        if (lastname == null || lastname.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
        if (address == null) {
            throw new IllegalArgumentException("Client must have an address.");
        }
    }
}