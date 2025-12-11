package org.pharmacy.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/pharmacy";

    // PostgreSQL username, password, update with your credentials
    private static final String USER = "karolis";
    private static final String PASS = "acme123!";

    /**
     * Returns a new database connection.
     */

    public static Connection getConnection() throws SQLException {
        try {
            // Ensure the PostgreSQL JDBC driver is loaded
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found.");
            throw new SQLException("Driver not found", e);
        }

        System.out.println("Connecting to PostgreSQL database...");
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}
