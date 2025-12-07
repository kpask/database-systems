package org.pharmacy;

import org.pharmacy.db.DBConnector;
import org.pharmacy.model.Order;
import org.pharmacy.repository.ClientRepository;
import org.pharmacy.repository.OrderRepository;

import java.sql.*;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        final String SQL_QUERY = "SELECT * FROM medicine";
        Connection conn = DBConnector.getConnection();
        ClientRepository clientRepository = new ClientRepository(conn);
        OrderRepository orderRepository = new OrderRepository(conn);
        clientRepository.getAllCLients();
        Order order = orderRepository.getOrderById(4);
        System.out.println(order.getClientId() + " " + order.getTotalPrice());
        List<Order> ord = orderRepository.getOrdersByClient(1);
        System.out.println(ord.getFirst().getTotalPrice());
    }
}