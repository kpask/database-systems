package org.pharmacy.repository;

import org.pharmacy.model.Supplier;
import org.pharmacy.model.SupplierMedicine;
import org.pharmacy.model.Address;
import org.pharmacy.exceptions.DataIntegrityViolationException;
import org.pharmacy.exceptions.DataNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing CRUD operations related to the Supplier entity
 * and the SupplierMedicine many-to-many relationship.
 * Handles database interactions, including address mapping and foreign key checks.
 */
public class SupplierRepository {

    private final Connection conn;

    /**
     * Initializes the repository with an active database connection.
     * @param conn The active SQL connection object.
     */
    public SupplierRepository(Connection conn) {
        this.conn = conn;
    }

    /**
     * Maps a current row from the ResultSet to a Supplier record object,
     * including the nested Address record.
     *
     * @param rs The ResultSet containing supplier and address data.
     * @return A fully populated Supplier object.
     * @throws SQLException If a database access error occurs during reading.
     */
    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Address address = new Address(
                rs.getString("country"),
                rs.getString("city"),
                rs.getString("street"),
                rs.getString("postal_code")
        );

        return new Supplier(
                rs.getLong("supplier_id"),
                rs.getString("name"),
                address
        );
    }

    /**
     * Retrieves all supplier records from the database, ordered by name.
     * Assumes the supplier table contains all necessary address columns.
     *
     * @return A list of all Supplier objects.
     * @throws SQLException If a database access error occurs.
     */
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

    /**
     * Adds a new supplier record to the database.
     *
     * @param supplier The Supplier object containing name and address details.
     * @throws SQLException If a database access error occurs.
     */
    public void addSupplier(Supplier supplier) throws SQLException {
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

    /**
     * Deletes a supplier record from the database by ID.
     *
     * @param supplierId The ID of the supplier to delete (must be positive).
     * @throws SQLException If a general database access error occurs.
     * @throws IllegalArgumentException If the supplier ID is not positive.
     * @throws DataNotFoundException If the supplier with the given ID was not found.
     * @throws DataIntegrityViolationException If the supplier is linked to existing medicines (Foreign Key violation).
     */
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

    /**
     * Retrieves the medicine linkages (SupplierMedicine records) for a given supplier ID.
     *
     * @param supplierId The ID of the supplier (must be positive).
     * @return A list of SupplierMedicine objects supplied by this supplier. Returns an empty list if ID is invalid or no links are found.
     * @throws SQLException If a database access error occurs.
     */
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

    /**
     * Links a medicine to a supplier, or updates the supply price if the link already exists.
     * Uses PostgreSQL's {@code ON CONFLICT DO UPDATE} clause (UPSERT operation).
     *
     * @param supplierMedicine The SupplierMedicine object containing supplier ID, medicine ID, and supply price.
     * @throws SQLException If a general database access error occurs.
     * @throws DataIntegrityViolationException If either the Supplier ID or the Medicine ID does not exist in the database (Foreign Key constraint).
     */
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