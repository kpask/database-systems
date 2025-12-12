package org.pharmacy.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class responsible for establishing and providing a connection
 * to the PostgreSQL database for the pharmacy application.
 */
public class DBConnector {

    /**
     * The URL for the PostgreSQL database connection, specifying the host, port, and database name.
     */
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/pharmacy";

    /**
     * The database username used for connection authentication.
     */
    private static final String USER = "postgres";

    /**
     * The database password used for connection authentication.
     */
    private static final String PASS = "postgres!";

    /**
     * Establishes and returns a new active database connection instance.
     * <p>
     * This method ensures the PostgreSQL JDBC driver is loaded before attempting connection.
     * </p>
     *
     * @return A new {@code Connection} object to the database.
     * @throws SQLException If a database access error occurs (e.g., connection details are wrong, DB is down)
     * or if the JDBC driver cannot be loaded.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Ensure the PostgreSQL JDBC driver is loaded
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found.");
            // Wrap the ClassNotFoundException in a SQLException for consistent method signature
            throw new SQLException("Driver not found", e);
        }

        System.out.println("Connecting to PostgreSQL database...");
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}