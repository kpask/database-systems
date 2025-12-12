package org.pharmacy.repository;

import org.pharmacy.exceptions.DataIntegrityViolationException;
import org.pharmacy.model.Medicine;
import org.pharmacy.exceptions.DataNotFoundException; // Assuming this exception is created

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing CRUD operations related to the Medicine entity.
 * Handles database interactions, including stock updates and foreign key checks.
 */
public class MedicineRepository {

    private final Connection conn;

    /**
     * Initializes the repository with an active database connection.
     * @param conn The active SQL connection object.
     */
    public MedicineRepository(Connection conn) {
        this.conn = conn;
    }

    /**
     * Maps a current row from the ResultSet to a Medicine record object.
     * This is a private helper method.
     *
     * @param rs The ResultSet containing the medicine data.
     * @return A fully populated Medicine object.
     * @throws SQLException If a database access error occurs during reading.
     */
    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        return new Medicine(
                rs.getLong("medicine_id"),
                rs.getString("name"),
                rs.getDouble("unit_price"),
                rs.getInt("stock")
        );
    }

    /**
     * Retrieves all medicine records from the database, ordered by name.
     *
     * @return A list of all available Medicine objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Medicine> getAllMedicines() throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        final String SQLQuery = "SELECT * FROM medicine ORDER BY name";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }
        }
        return medicines;
    }

    /**
     * Adds a new medicine record to the database and retrieves the generated ID.
     *
     * @param medicine The Medicine object containing name, price, and initial stock.
     * @return The auto-generated ID of the newly added medicine.
     * @throws SQLException If a database access error occurs or if no ID was returned.
     */
    public long addMedicine(Medicine medicine) throws SQLException {
        final String SQLQuery = "INSERT INTO medicine(name, unit_price, stock) VALUES (?, ?, ?)";
        long generatedId = -1;

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, medicine.name());
            pstmt.setDouble(2, medicine.unitPrice());
            pstmt.setInt(3, medicine.stock());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedId = keys.getLong(1);
                        System.out.printf("Medicine '%s' successfully added with ID: %d\n", medicine.name(), generatedId);
                        return generatedId;
                    }
                }
            } else {
                throw new SQLException("Failed to create medicine, no rows affected.");
            }
        }
        return generatedId;
    }

    /**
     * Deletes a medicine record from the database by ID.
     *
     * @param medicineId The ID of the medicine to delete (must be positive).
     * @throws SQLException If a general database access error occurs.
     * @throws IllegalArgumentException If the medicine ID is not positive.
     * @throws DataNotFoundException If the medicine with the given ID was not found.
     * @throws DataIntegrityViolationException If the medicine is referenced by existing orders or suppliers (Foreign Key).
     */
    public void deleteMedicine(long medicineId) throws SQLException {
        if (medicineId <= 0) {
            throw new IllegalArgumentException("Medicine ID must be positive for deletion.");
        }

        final String SQLQuery = "DELETE FROM medicine WHERE medicine_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            pstmt.setLong(1, medicineId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DataNotFoundException("Medicine with ID " + medicineId + " was not found.");
            }

            System.out.printf("Medicine with ID %d successfully deleted.\n", medicineId);

        } catch (SQLException e) {
            // Check for Foreign Key Violation (e.g., PostgreSQL code "23503")
            if ("23503".equals(e.getSQLState())) {
                throw new DataIntegrityViolationException(
                        String.format("Medicine ID %d cannot be deleted because it is linked to existing orders or suppliers.", medicineId), e);
            }
            throw e;
        }
    }

    /**
     * Updates the stock quantity for a specific medicine ID.
     *
     * @param medicineId The ID of the medicine to update (must be positive).
     * @param newStock The new stock quantity (cannot be negative).
     * @throws SQLException If a database access error occurs.
     * @throws IllegalArgumentException If medicineId is non-positive or newStock is negative.
     * @throws DataNotFoundException If the medicine with the given ID was not found.
     */
    public void updateMedicineStock(long medicineId, int newStock) throws SQLException, DataNotFoundException {
        if (medicineId <= 0) {
            throw new IllegalArgumentException("Medicine ID must be positive for stock update.");
        }
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }

        // SQL Execution
        final String SQLQuery = "UPDATE medicine SET stock = ? WHERE medicine_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            pstmt.setInt(1, newStock);
            pstmt.setLong(2, medicineId);

            int affectedRows = pstmt.executeUpdate();

            // 3. Result Check
            if (affectedRows == 0) {
                throw new DataNotFoundException("Medicine with ID " + medicineId + " was not found. Stock update failed.");
            }
        }
    }
}