package org.pharmacy.Repository;

import org.pharmacy.Model.Supplier;
import org.pharmacy.Model.SupplierMedicine;
import org.pharmacy.Model.Address;
import org.pharmacy.Exceptions.DataIntegrityViolationException;
import org.pharmacy.Exceptions.DataNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierRepository {

    private final Connection conn;

    public SupplierRepository(Connection conn) {
        this.conn = conn;
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        // Constructs the embedded Address record first
        Address address = new Address(
                rs.getString("country"),
                rs.getString("city"),
                rs.getString("street"),
                rs.getString("postal_code")
        );

        return new Supplier(
                rs.getLong("supplier_id"),
                rs.getString("name"),
                address // Pass the Address record
        );
    }

    public List<Supplier> getAllSuppliers() throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        final String SQLQuery = "SELECT * FROM supplier ORDER BY name";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        }
        return suppliers;
    }

    public void addSupplier(Supplier supplier) throws SQLException {
        // Validation (name, address components) is expected to be handled by Supplier/Address records

        final String SQLQuery = "INSERT INTO supplier(name, country, city, street, postal_code) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {

            pstmt.setString(1, supplier.name());
            pstmt.setString(2, supplier.address().country());
            pstmt.setString(3, supplier.address().city());
            pstmt.setString(4, supplier.address().street());
            pstmt.setString(5, supplier.address().postalCode());

            int affectedRows = pstmt.executeUpdate();
            System.out.printf("Supplier '%s' successfully added. Rows changed: %d\n", supplier.name(), affectedRows);
        }
    }

    public void deleteSupplier(long supplierId) throws SQLException {
        if (supplierId <= 0) {
            throw new IllegalArgumentException("Supplier ID must be positive for deletion.");
        }

        final String SQLQuery = "DELETE FROM supplier WHERE supplier_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            pstmt.setLong(1, supplierId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DataNotFoundException("Supplier with ID " + supplierId + " was not found.");
            }

            System.out.printf("Supplier with ID %d successfully deleted.\n", supplierId);

        } catch (SQLException e) {
            // Check for Foreign Key Violation (PostgreSQL specific code: 23503)
            if ("23503".equals(e.getSQLState())) {
                throw new DataIntegrityViolationException(
                        String.format("Supplier ID %d cannot be deleted because it supplies existing medicines (suppliermedicine entry exists).", supplierId), e);
            }
            throw e;
        }
    }

    public List<SupplierMedicine> getMedicineBySupplierId(long supplierId) throws SQLException {
        if (supplierId <= 0) {
            return new ArrayList<>();
        }

        List<SupplierMedicine> supplierMedicines = new ArrayList<>();
        final String SQLQuery = "SELECT * FROM suppliermedicine WHERE supplier_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            pstmt.setLong(1, supplierId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    supplierMedicines.add(new SupplierMedicine(
                            rs.getLong("supplier_id"),
                            rs.getLong("medicine_id"),
                            rs.getDouble("supply_price")
                    ));
                }
            }
        }
        return supplierMedicines;
    }

    public void addMedicineToSupplier(SupplierMedicine supplierMedicine) throws SQLException {
        final String SQLQuery = "INSERT INTO suppliermedicine(supplier_id, medicine_id, supply_price) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (supplier_id, medicine_id) DO UPDATE SET supply_price = EXCLUDED.supply_price";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {

            pstmt.setLong(1, supplierMedicine.supplierId());
            pstmt.setLong(2, supplierMedicine.medicineId());
            pstmt.setDouble(3, supplierMedicine.supplyPrice());

            int affectedRows = pstmt.executeUpdate();
            System.out.printf("Link established/updated: Supplier ID %d supplies Medicine ID %d at price %.2f. Rows affected: %d\n",
                    supplierMedicine.supplierId(), supplierMedicine.medicineId(), supplierMedicine.supplyPrice(), affectedRows);

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new DataIntegrityViolationException(
                        "Cannot link medicine to supplier: Either Supplier ID or Medicine ID does not exist in the database.", e);
            }
            throw e;
        }
    }
}