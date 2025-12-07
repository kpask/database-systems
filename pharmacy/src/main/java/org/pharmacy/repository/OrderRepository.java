package org.pharmacy.repository;

import org.pharmacy.model.Order;

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

            System.out.printf("Inserted %d units of medicine %d into order %d\n",
                    entry.getValue(), entry.getKey(), orderID);
        }

        return orderID;
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

}
