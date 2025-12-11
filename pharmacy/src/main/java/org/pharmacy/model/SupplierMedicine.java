package org.pharmacy.Model;

public record SupplierMedicine(
        long supplierId,
        long medicineId,
        double supplyPrice
) {
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