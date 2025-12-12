package org.pharmacy.repository;

import org.pharmacy.exceptions.DataIntegrityViolationException;
import org.pharmacy.exceptions.DataNotFoundException;
import org.pharmacy.model.Order;
import org.pharmacy.model.OrderItem;
import org.pharmacy.model.OrderSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OrderRepository {
    Connection conn;

    /**
     * Initializes the repository with a database connection.
     * @param conn The active SQL connection object.
     * @throws SQLException If a database access error occurs.
     */
    public OrderRepository(Connection conn) throws SQLException {
        this.conn = conn;
    }

    /**
     * Retrieves a single Order object by its ID.
     * @param orderId The ID of the order to retrieve.
     * @return The Order object if found, or null if the ID is invalid or not found.
     * @throws SQLException If a database access error occurs.
     */
    public Order getOrderById(long orderId) throws SQLException{
        if(orderId <= 0){
            return null;
        }
        String SQLQuery = "SELECT * FROM \"order\" WHERE order_id = ?;";

        try(PreparedStatement pstmt = conn.prepareStatement(SQLQuery)){
            pstmt.setLong(1, orderId);
            try (ResultSet res = pstmt.executeQuery()) {

                if (!res.next()) {
                    return null; // order not found
                }

                return new Order(
                        res.getLong("order_id"),
                        res.getLong("client_id"),
                        res.getDate("order_date"),
                        res.getDouble("total_price")
                );
            }
        }
    }

    /**
     * Retrieves all orders associated with a specific client ID, ordered by date descending.
     * @param clientId The ID of the client whose orders are to be retrieved.
     * @return A list of Order objects. Returns an empty list if client ID is invalid or no orders are found.
     * @throws SQLException If a database access error occurs.
     */
    public List<Order> getOrdersByClient(long clientId) throws SQLException{
        if(clientId <= 0){
            return new ArrayList<>();
        }

        final String SQLQuery = "SELECT * FROM \"order\" WHERE client_id = ? ORDER BY order_date DESC";
        List<Order> clientOrders = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            pstmt.setLong(1, clientId);
            try (ResultSet res = pstmt.executeQuery()) {
                while (res.next()) {
                    Order order = new Order(
                            res.getLong("order_id"),
                            res.getLong("client_id"),
                            res.getDate("order_date"),
                            res.getDouble("total_price")
                    );
                    clientOrders.add(order);
                }
            }
        }
        return clientOrders;
    }

    /**
     * Helper method to safely update the medicine stock by decreasing the quantity.
     * Throws DataIntegrityViolationException if stock is insufficient or medicine is not found.
     * @param medicineId The ID of the medicine to update.
     * @param quantity The amount to subtract from stock.
     * @throws SQLException If a database access error occurs.
     * @throws DataIntegrityViolationException If stock check fails (i.e., insufficient stock).
     */
    private void updateMedicineStock(long medicineId, int quantity) throws SQLException {
        final String updateStockSQL = "UPDATE medicine SET stock = stock - ? " +
                "WHERE medicine_id = ? AND stock >= ?";

        try (PreparedStatement pstmt = conn.prepareStatement(updateStockSQL)) {
            pstmt.setInt(1, quantity);
            pstmt.setLong(2, medicineId);
            pstmt.setInt(3, quantity);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                // Jei atnaujinimas nepavyko, vadinasi, atsargos nebuvo pakankamos (dėl WHERE sąlygos).
                throw new DataIntegrityViolationException("Stock check failed (insufficient stock).");
            }
            System.out.printf("   -> Stock reduced for Medicine ID %d by %d.\n", medicineId, quantity);
        }
    }

    /**
     * Creates a new order. If any single item fails (not found, insufficient stock),
     * the entire order transaction is rolled back.
     *
     * @param clientId The ID of the client placing the order.
     * @param itemQuantities Map of Medicine ID to Quantity.
     * @return The ID of the newly created order.
     * @throws SQLException If a database access error occurs.
     * @throws DataIntegrityViolationException If any item fails to be processed, forcing a rollback.
     */
    public long createOrder(long clientId, Map<Long, Integer> itemQuantities) throws SQLException {

        long orderID = -1;
        // 1. Pradedame transakciją
        conn.setAutoCommit(false);

        try {
            // A. Insert a new order
            String insertOrderSQL = "INSERT INTO \"order\"(client_id, order_date, total_price) VALUES(?, CURRENT_DATE, 0.00)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertOrderSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstmt.setLong(1, clientId);
                pstmt.executeUpdate();
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Failed to create order, no ID obtained.");
                    }
                    orderID = keys.getLong(1);
                }
            }

            String insertItemSQL = "INSERT INTO orderitem(order_id, medicine_id, quantity) VALUES (?, ?, ?)";
            boolean hasFailed = false;

            for (Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {
                long medicineId = entry.getKey();
                int quantity = entry.getValue();

                try {
                    // 1. Insert orderitem
                    try (PreparedStatement pstmt = conn.prepareStatement(insertItemSQL)) {
                        pstmt.setLong(1, orderID);
                        pstmt.setLong(2, medicineId);
                        pstmt.setInt(3, quantity);
                        pstmt.executeUpdate();
                    }

                    // 2. Update the stock
                    updateMedicineStock(medicineId, quantity);
                    System.out.printf("  [SUCCESS] Added Medicine ID %d (%d units).\n", medicineId, quantity);

                } catch (SQLException | DataIntegrityViolationException e) {
                    System.err.printf("[FAIL] Medicine ID %d could not be processed: %s\n", medicineId,
                            e.getMessage().contains("integrity") ? "Insufficient stock" : e.getMessage());
                    hasFailed = true;
                    break;
                }
            }

            if (hasFailed) {
                throw new DataIntegrityViolationException("Order creation failed: One or more items could not be processed successfully.");
            }

            // D. If everything is fine confirm the transactions
            conn.commit();
            return orderID;

        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            // F. Return autoCommit to previous state
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Deletes an order and associated items (cascading assumed).
     * @param orderId The ID of the order to delete.
     * @throws SQLException If a database access error occurs.
     * @throws IllegalArgumentException If the order ID is not positive.
     */
    public void deleteOrder(long orderId) throws SQLException {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive.");
        }
        final String SQLQuery = "DELETE FROM \"order\" WHERE order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            pstmt.setLong(1, orderId);

            int affectedRows = pstmt.executeUpdate();
            if(affectedRows == 0){
                System.out.println("Order with ID " + orderId + " was not found");
            }
            System.out.println("Order with ID " + orderId + " was successfully deleted.");
        }
    }

    /**
     * Retrieves all items belonging to a specific order, including medicine name.
     * @param orderId The ID of the order.
     * @return A list of OrderItem objects.
     * @throws SQLException If a database access error occurs.
     * @throws DataNotFoundException If no items are found for the given order ID.
     */
    public List<OrderItem> getOrderItemsByOrderId(long orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();

        final String SQLQuery = "SELECT oi.order_id, oi.medicine_id, oi.quantity, oi.unit_price, m.name AS medicine_name " +
                "FROM orderitem oi " +
                "JOIN medicine m ON oi.medicine_id = m.medicine_id " +
                "WHERE oi.order_id = ?";

        boolean found = false;
        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            pstmt.setLong(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    found = true;
                    items.add(new OrderItem(
                            rs.getLong("order_id"),
                            rs.getLong("medicine_id"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price")
                    ));
                }
            }
        }
        if (!found) {
            throw new DataNotFoundException("Order items associated with ID " + orderId + " were not found.");
        }

        return items;
    }

    /**
     * Retrieves all orders in the database, ordered by date descending.
     * @return A list of all Order objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Order> getAllOrders() throws SQLException {
        final String SQLQuery = "SELECT * FROM \"order\" ORDER BY order_date DESC, order_id DESC";
        List<Order> allOrders = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            try (ResultSet res = pstmt.executeQuery()) {
                while (res.next()) {
                    Order order = new Order(
                            res.getLong("order_id"),
                            res.getLong("client_id"),
                            res.getDate("order_date"),
                            res.getDouble("total_price")
                    );
                    allOrders.add(order);
                }
            }
        }
        return allOrders;
    }

    /**
     * Retrieves detailed summaries of all orders using the 'detailed_order_summary' view.
     * @return A list of OrderSummary objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<OrderSummary> getAllDetailedOrders() throws SQLException {
        List<OrderSummary> summaries = new ArrayList<>();
        final String SQLQuery = "SELECT * FROM detailed_order_summary ORDER BY order_date DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                summaries.add(new OrderSummary(
                        rs.getLong("order_id"),
                        rs.getDate("order_date"),
                        rs.getString("client_first_name"),
                        rs.getString("client_last_name"),
                        rs.getDouble("total_price"),
                        rs.getLong("total_items_count")
                ));
            }
        }
        return summaries;
    }

    /**
     * Retrieves detailed summaries of orders for a specific client using the 'detailed_order_summary' view.
     * @param clientId The ID of the client.
     * @return A list of OrderSummary objects.
     * @throws SQLException If a database access error occurs.
     * @throws IllegalArgumentException If the client ID is not positive.
     * @throws DataNotFoundException If the client has no orders or does not exist.
     */
    public List<OrderSummary> getClientOrderSummaries(long clientId) throws SQLException {

        if (clientId <= 0) {
            throw new IllegalArgumentException("Client ID must be positive.");
        }

        List<OrderSummary> summaries = new ArrayList<>();
        final String SQLQuery = "SELECT order_id, order_date, client_first_name, client_last_name, total_price, total_items_count " +
                "FROM detailed_order_summary WHERE client_id = ? ORDER BY order_date DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            pstmt.setLong(1, clientId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("order_date");
                    Date orderDate = new Date(sqlDate.getTime());

                    summaries.add(new OrderSummary(
                            rs.getLong("order_id"),
                            orderDate,
                            rs.getString("client_first_name"),
                            rs.getString("client_last_name"),
                            rs.getDouble("total_price"),
                            rs.getLong("total_items_count")
                    ));
                }
            }
        }
        if (summaries.isEmpty()) {
            throw new DataNotFoundException("Client with ID " + clientId + " has no orders or does not exist.");
        }
        return summaries;
    }
}
