package org.pharmacy.Model;

// package org.pharmacy;
public record Client(
    long id,
    String firstname,
    String lastname,
    Address address
) {

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
