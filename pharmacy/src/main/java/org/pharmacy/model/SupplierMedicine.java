package org.pharmacy.model;

/**
 * Represents the many-to-many relationship between a Supplier and a Medicine,
 * including the specific price at which the medicine is supplied.
 *
 * @param supplierId The ID of the supplier (must be positive).
 * @param medicineId The ID of the medicine (must be positive).
 * @param supplyPrice The cost price for the pharmacy from the supplier (must be positive).
 */
public record SupplierMedicine(
        long supplierId,
        long medicineId,
        double supplyPrice
) {
    /**
     * Compact constructor for the SupplierMedicine record, enforcing positive constraints on all fields.
     *
     * @throws IllegalArgumentException if any ID or supplyPrice is non-positive.
     */
    public SupplierMedicine {
        if (supplierId <= 0) {
            throw new IllegalArgumentException("Supplier ID must be positive.");
        }
        if (medicineId <= 0) {
            throw new IllegalArgumentException("Medicine ID must be positive.");
        }
        if (supplyPrice <= 0) {
            throw new IllegalArgumentException("Supply price must be positive.");
        }
    }
}