package org.pharmacy.Repository;

import org.pharmacy.Exceptions.DataNotFoundException;
import org.pharmacy.Model.Address;
import org.pharmacy.Model.Client;
import org.pharmacy.Exceptions.DataIntegrityViolationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientRepository {
    Connection conn;

    public ClientRepository(Connection conn){
        this.conn = conn;
    }

    public void addClient(Client client) throws SQLException {
        if (client == null){
            throw new IllegalArgumentException("Client object cannot be null.");
        }

        final String SQLQuery = "INSERT INTO client(first_name, last_name, country, city, street, postal_code) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            pstmt.setString(1, client.firstname());
            pstmt.setString(2, client.lastname());
            pstmt.setString(3, client.address().country());
            pstmt.setString(4, client.address().city());
            pstmt.setString(5, client.address().city());
            pstmt.setString(6, client.address().postalCode());

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Client " + client.firstname() + " " + client.lastname() + " successfully added. Rows changed: " + affectedRows);
        }
    }

    public void deleteClient(long clientId) throws SQLException {
        // Validation check for the ID itself (Repository responsibility)
        if (clientId <= 0) {
            throw new IllegalArgumentException("Client ID must be positive for deletion.");
        }

        final String SQLQuery = "DELETE FROM client WHERE client_id = ?";

        try(PreparedStatement pstmt = conn.prepareStatement(SQLQuery)){
            pstmt.setLong(1, clientId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DataNotFoundException("Client with ID " + clientId + " was not found.");
            }
            System.out.printf("Client with ID %d successfully deleted.\n", clientId);

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new DataIntegrityViolationException(
                        String.format("Client with ID %d cannot be deleted because there are existing orders associated with this client. Please delete the orders first.", clientId), e);
            }
            throw e;
        }
    }

    public void updateClientAddress(long clientId, Address address) throws SQLException {
        final String SQLQuery = "UPDATE client SET country = ?, city = ?, street = ?, postal_code = ? WHERE client_id = ?";

        try(PreparedStatement pstmt = conn.prepareStatement(SQLQuery)){
            pstmt.setString(1, address.country());
            pstmt.setString(2, address.city());
            pstmt.setString(3, address.street());
            pstmt.setString(4, address.postalCode());
            pstmt.setLong(5, clientId);

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Client with id " + clientId + " adddress updated. Rows affected: " + affectedRows);
        }
    }

    public List<Client> getAllCLients() throws SQLException {
        ArrayList<Client> clients = new ArrayList<>();
        final String SQLQuery = "SELECT * FROM client";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Address address = new Address(
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("postal_code")
                );

                clients.add(new Client(
                        rs.getLong("client_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        address
                ));
            }
        }
        return clients;
    }

    public void deleteOrder(long orderId) throws SQLException {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive.");
        }

        final String SQLQuery = "DELETE FROM \"order\" WHERE order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            pstmt.setLong(1, orderId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Order with ID " + orderId + " was not found.");
            }

            System.out.println("Order with ID " + orderId + " was successfully deleted.");
        }
    }
}
