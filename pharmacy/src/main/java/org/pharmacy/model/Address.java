package org.pharmacy.model;
/**
 * Represents an unchangeable address data structure for a client or supplier.
 * <p>
 * This record enforces strict validation during construction to ensure all address
 * components are non-null and non-empty. It also automatically trims whitespace.
 * </p>
 *
 * @param country The country name (must be non-null and non-empty).
 * @param city The city name (must be non-null and non-empty).
 * @param street The street name (must be non-null and non-empty).
 * @param postalCode The postal code (must be non-null and non-empty).
 */
public record Address(
        String country,
        String city,
        String street,
        String postalCode // Using standard Java naming convention (CamelCase)
) {
    /**
     * Compact constructor for the Address record.
     * <p>
     * This constructor performs data normalization (trimming whitespace)
     * and validation checks (ensuring no component is null or empty).
     * </p>
     * @throws IllegalArgumentException if any address component is null, empty, or consists only of whitespace.
     */
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