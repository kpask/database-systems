package org.pharmacy.Repository;

import org.pharmacy.Exceptions.DataIntegrityViolationException;
import org.pharmacy.Model.Medicine;
import org.pharmacy.Exceptions.DataNotFoundException; // Assuming this exception is created

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedicineRepository {

    private final Connection conn;

    public MedicineRepository(Connection conn) {
        this.conn = conn;
    }

    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        // We assume the Medicine record constructor handles necessary validation
        return new Medicine(
                rs.getLong("medicine_id"),
                rs.getString("name"),
                rs.getDouble("unit_price"),
                rs.getInt("stock")
        );
    }

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

    public long addMedicine(Medicine medicine) throws SQLException {
        // Validation (name, unitPrice, stock > 0) is expected to be handled by Medicine record constructor

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

    public void deleteMedicine(long medicineId) throws SQLException {
        if (medicineId <= 0) {
            throw new IllegalArgumentException("Medicine ID must be positive for deletion.");
        }

        final String SQLQuery = "DELETE FROM medicine WHERE medicine_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            pstmt.setLong(1, medicineId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                // Throw specific exception when the entity is not found
                throw new DataNotFoundException("Medicine with ID " + medicineId + " was not found.");
            }

            System.out.printf("Medicine with ID %d successfully deleted.\n", medicineId);

        } catch (SQLException e) {
            // Check for Foreign Key Violation (e.g., if this medicine is still referenced in orderitem)
            if ("23503".equals(e.getSQLState())) {
                throw new DataIntegrityViolationException(
                        String.format("Medicine ID %d cannot be deleted because it is linked to existing orders or suppliers.", medicineId), e);
            }
            throw e;
        }
    }
}