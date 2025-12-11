package org.pharmacy.Repository;

import org.pharmacy.Exceptions.DataIntegrityViolationException;
import org.pharmacy.Exceptions.DataNotFoundException;
import org.pharmacy.Model.Order;
import org.pharmacy.Model.OrderItem;
import org.pharmacy.Model.OrderSummary;

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

        long orderID = -1;
        double calculatedTotalPrice = 0.0;

        // 1. PRADEDAME TRANSAKCIJĄ (Užsakymas ir visos eilutės turi būti sėkmingai įterptos)
        conn.setAutoCommit(false);

        try {
            // A. Įterpti pagrindinį užsakymą (su nuliu kaina)
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

            // B. Įterpti užsakymo eilutes su "BEST EFFORT"
            String insertItemSQL = "INSERT INTO orderitem(order_id, medicine_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
            boolean successfulItemsExist = false;

            for (Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {
                long medicineId = entry.getKey();
                int quantity = entry.getValue();

                try {
                    // 1. Pasiimti kainą ir atsargas
                    double[] details = getMedicineDetails(medicineId);
                    double currentUnitPrice = details[0];

                    // 2. Įterpti užsakymo eilutę (su fiksuota kaina)
                    try (PreparedStatement pstmt = conn.prepareStatement(insertItemSQL)) {
                        pstmt.setLong(1, orderID);
                        pstmt.setLong(2, medicineId);
                        pstmt.setInt(3, quantity);
                        pstmt.setDouble(4, currentUnitPrice);
                        pstmt.executeUpdate();
                    }

                    // 3. Sumažinti atsargas (su patikra)
                    updateMedicineStock(medicineId, quantity);

                    // 4. Atnaujinti bendrą kainą
                    calculatedTotalPrice += currentUnitPrice * quantity;
                    successfulItemsExist = true;

                    System.out.printf("  [SUCCESS] Added Medicine ID %d (%d units). Subtotal: %.2f\n",
                            medicineId, quantity, currentUnitPrice * quantity);

                } catch (DataNotFoundException e) {
                    // Ignoruoti ir pranešti, jei vaistas neegzistuoja
                    System.err.printf("  [SKIP] Medicine ID %d skipped: %s\n", medicineId, e.getMessage());
                    continue; // Tęsti kitą eilutę
                } catch (DataIntegrityViolationException e) {
                    // Ignoruoti ir pranešti, jei trūksta atsargų
                    System.err.printf("  [SKIP] Medicine ID %d skipped: Insufficient stock for %d units.\n", medicineId, quantity);
                    continue; // Tęsti kitą eilutę
                }
            }

            // C. Finalinis patikrinimas: Ar įterpta bent viena sėkminga prekė?
            if (!successfulItemsExist) {
                // Jei nei viena prekė nebuvo įterpta, atšaukti visą užsakymą!
                throw new DataIntegrityViolationException("Order creation failed: No valid items could be processed.");
            }

            // D. Atnaujinti pagrindinio užsakymo total_price
            String updateTotalSQL = "UPDATE \"order\" SET total_price = ? WHERE order_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateTotalSQL)) {
                pstmt.setDouble(1, calculatedTotalPrice);
                pstmt.setLong(2, orderID);
                pstmt.executeUpdate();
            }

            // E. Jei viskas gerai, PATVIRTINTI transakciją
            conn.commit();
            return orderID;

        } catch (SQLException | DataIntegrityViolationException | DataNotFoundException e) {
            // F. Jei įvyko klaida (pvz., order lentelės klaida ar successfulItemsExist patikrinimas)
            if (conn != null) {
                conn.rollback();
            }
            // Jei orderID buvo gautas, bet transakcija atšaukta, informuoti apie tai
            if (orderID != -1) {
                System.err.printf("\n### TRANSACTION ROLLED BACK for potential Order ID %d ###\n", orderID);
            }
            throw e; // Perduoti klaidą aukštyn

        } finally {
            // G. Atstatyti autoCommit į pradinę būseną
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    private void updateMedicineStock(long medicineId, int quantity) throws SQLException {
        // ... (kodas, kuris atlieka saugų UPDATE su WHERE stock >= ? ir meta DataIntegrityViolationException jei nepavyksta) ...
        final String updateStockSQL = "UPDATE medicine SET stock = stock - ? " +
                "WHERE medicine_id = ? AND stock >= ?";

        try (PreparedStatement pstmt = conn.prepareStatement(updateStockSQL)) {
            pstmt.setInt(1, quantity);
            pstmt.setLong(2, medicineId);
            pstmt.setInt(3, quantity);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                // Čia tiesiog naudojame bendrąją klaidą, kurią pagausime cikle
                throw new DataIntegrityViolationException("Stock check failed (insufficient stock or medicine not found).");
            }
            System.out.printf("   -> Stock reduced for Medicine ID %d by %d.\n", medicineId, quantity);
        }
    }

    private double[] getMedicineDetails(long medicineId) throws SQLException {
        final String SQL = "SELECT unit_price, stock FROM medicine WHERE medicine_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setLong(1, medicineId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new double[]{rs.getDouble("unit_price"), (double) rs.getInt("stock")};
                } else {
                    throw new DataNotFoundException("Medicine not found in the database.");
                }
            }
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
