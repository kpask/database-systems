package org.pharmacy.Model;
public record Address(
        String country,
        String city,
        String street,
        String postalCode // Using standard Java naming convention (CamelCase)
) {
    public Address {
        country = (country != null) ? country.trim() : null;
        city = (city != null) ? city.trim() : null;
        street = (street != null) ? street.trim() : null;
        postalCode = (postalCode != null) ? postalCode.trim() : null;

        if (country == null || country.isEmpty()) {
            throw new IllegalArgumentException("Country cannot be null or empty.");
        }
        if (city == null || city.isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty.");
        }
        if (street == null || street.isEmpty()) {
            throw new IllegalArgumentException("Street cannot be null or empty.");
        }
        if (postalCode == null || postalCode.isEmpty()) {
            throw new IllegalArgumentException("Postal code cannot be null or empty.");
        }
    }
}