package org.pharmacy.repository;

import org.pharmacy.model.Client;

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
        if (client.getFirstname() == null || client.getFirstname().trim().isEmpty()
                || client.getLastname() == null || client.getLastname().trim().isEmpty()
                || client.getCountry() == null || client.getCountry().trim().isEmpty()
                || client.getCity() == null || client.getCity().trim().isEmpty()
                || client.getStreet() == null || client.getStreet().trim().isEmpty()
                || client.getPostalCode() == null || client.getPostalCode().trim().isEmpty()) {
            throw new IllegalArgumentException("All client fields must be filled.");
        }

        final String SQLQuery = "INSERT INTO client(first_name, last_name, country, city, street, postal_code) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(SQLQuery)) {
            pstmt.setString(1, client.getFirstname());
            pstmt.setString(2, client.getLastname());
            pstmt.setString(3, client.getCountry());
            pstmt.setString(4, client.getCity());
            pstmt.setString(5, client.getStreet());
            pstmt.setString(6, client.getPostalCode());

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Client " + client.getFirstname() + " " + client.getLastname() + " successfully added. Rows changed: " + affectedRows);
        }
    }

    public void deleteClient(Client client) throws SQLException {
        final String SQLQuery = "DELETE FROM client WHERE client_id = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(SQLQuery)){
            pstmt.setLong(1, client.getId());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Client with ID " + client.getId() + " was not found.");
            }
            System.out.println("Client with ID " + client.getId() + " successfully deleted");
        }
    }

    public void updateClientAddress(long clientId, String country, String city, String street, String postal_code) throws SQLException {
        final String SQLQuery = "UPDATE client SET country = ?, city = ?, street = ?, postal_code = ? WHERE client_id = ?";

        try(PreparedStatement pstmt = conn.prepareStatement(SQLQuery)){
            pstmt.setString(1, country);
            pstmt.setString(2, city);
            pstmt.setString(3, street);
            pstmt.setString(4, postal_code);
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
                clients.add(new Client(
                        rs.getLong("client_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("postal_code"))
                );
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
