package org.pharmacy.repository;

import org.pharmacy.exceptions.DataNotFoundException;
import org.pharmacy.model.Order;
import org.pharmacy.model.OrderItem;
import org.pharmacy.model.OrderSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderRepository {
    Connection conn;

    public OrderRepository(Connection conn) throws SQLException {
        this.conn = conn;
    }

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
                            res.getDouble("total_price") // Reikalingas total_price laukas
                    );
                    clientOrders.add(order);
                }
            }
        }
        return clientOrders;
    }

    public long createOrder(long clientId, Map<Long, Integer> itemQuantities) throws SQLException {
        String insertOrderSQL = "INSERT INTO \"order\"(client_id, order_date) VALUES(?, CURRENT_DATE)";
        long orderID;

        try (PreparedStatement pstmt = conn.prepareStatement(
                insertOrderSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, clientId);
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to create order, no ID obtained.");
                }
                orderID = keys.getLong(1);

            }

        }


        // 2. Insert each order item
        String insertItemSQL = "INSERT INTO orderitem(order_id, medicine_id, quantity) VALUES (?, ?, ?)";

        for (Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {
            try (PreparedStatement pstmt = conn.prepareStatement(insertItemSQL)) {
                pstmt.setLong(1, orderID);
                pstmt.setLong(2, entry.getKey());      // medicine_id
                pstmt.setInt(3, entry.getValue());     // quantity
                pstmt.executeUpdate();
            }

            updateMedicineStock(entry.getKey(), entry.getValue());
            System.out.printf("Inserted %d units of medicine %d into order %d\n",
                    entry.getValue(), entry.getKey(), orderID);
        }

        return orderID;
    }

    private void updateMedicineStock(long medicineId, int quantity) throws SQLException {
        final String updateStockSQL = "UPDATE medicine SET stock = stock - ? WHERE medicine_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(updateStockSQL)) {
            pstmt.setInt(1, quantity);
            pstmt.setLong(2, medicineId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Cannot update stock for medicine ID " + medicineId + ". Medicine not found.");
            }
            System.out.println("   -> Stock reduced for Medicine ID " + medicineId + " by " + quantity);
        }
    }

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

}
