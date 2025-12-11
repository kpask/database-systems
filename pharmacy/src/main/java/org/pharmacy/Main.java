package org.pharmacy;

import org.pharmacy.db.DBConnector;
import org.pharmacy.model.*;
import org.pharmacy.repository.ClientRepository;
import org.pharmacy.repository.MedicineRepository;
import org.pharmacy.repository.OrderRepository;
import org.pharmacy.repository.SupplierRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection conn = DBConnector.getConnection()) {

            ClientRepository clientRepo = new ClientRepository(conn);
            OrderRepository orderRepo = new OrderRepository(conn);
            MedicineRepository medicineRepo = new MedicineRepository(conn);
            SupplierRepository supplierRepo = new SupplierRepository(conn);

            System.out.println("--- PHARMACY DB MANAGEMENT SYSTEM ---");
            runMenu(clientRepo, orderRepo, medicineRepo, supplierRepo);

        } catch (SQLException e) {
            System.err.println("\n### DATABASE ERROR OCCURRED: ###");
            System.err.printf("SQL State: %s\nMessage: %s\n", e.getSQLState(), e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("\n### APPLICATION ERROR: ###");
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("\n### UNEXPECTED ERROR: ###");
            e.printStackTrace();
        } finally {
            SCANNER.close();
        }
    }

    private static void runMenu(
            ClientRepository clientRepo,
            OrderRepository orderRepo,
            MedicineRepository medicineRepo,
            SupplierRepository supplierRepo) throws SQLException {
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Select operation (1-19): ");

            if (!SCANNER.hasNextInt()) {
                System.out.println("\nInvalid input. Please enter a number.");
                SCANNER.next();
                continue;
            }

            int choice = SCANNER.nextInt();
            SCANNER.nextLine();

            try {
                switch (choice) {
                    // CLIENT & ORDER OPERATIONS (1-9)
                    case 1: addClientInteractive(clientRepo); break;
                    case 2: updateClientAddressInteractive(clientRepo); break;
                    case 3: deleteClientInteractive(clientRepo); break;
                    case 4: readAllClients(clientRepo); break;
                    case 5: createOrderInteractive(orderRepo); break;
                    case 6: deleteOrderInteractive(orderRepo); break;
                    case 7: readOrdersByClientInteractive(orderRepo); break;
                    case 8: readOrderItemsInteractive(orderRepo); break;
                    case 9: readAllDetailedOrdersInteractive(orderRepo); break; // NAUJAS

                    // MEDICINE OPERATIONS (10-12)
                    case 10: addMedicineInteractive(medicineRepo); break;
                    case 11: deleteMedicineInteractive(medicineRepo); break;
                    case 12: readAllMedicinesInteractive(medicineRepo); break;

                    // SUPPLIER OPERATIONS (13-17)
                    case 13: addSupplierInteractive(supplierRepo); break;
                    case 14: deleteSupplierInteractive(supplierRepo); break;
                    case 15: addMedicineToSupplierInteractive(supplierRepo); break;
                    case 16: getMedicineBySupplierInteractive(supplierRepo); break;
                    case 17: readAllSuppliersInteractive(supplierRepo); break;
                    case 19:
                        running = false;
                        System.out.println("Exiting application. Goodbye.");
                        break;
                    default:
                        System.out.println("Invalid choice. Please select a number between 1 and 19.");
                }
            } catch (SQLException e) {
                throw e;
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n-------------------------------------------");
        System.out.println("--- CLIENTS & ORDERS ---");
        System.out.println("1. Add New Client");
        System.out.println("2. Update Client Address");
        System.out.println("3. Delete Client");
        System.out.println("4. View All Clients");
        System.out.println("5. Create New Order (Transaction)");
        System.out.println("6. Delete Order");
        System.out.println("7. View Orders by Client ID");
        System.out.println("8. View Order Items by Order ID");
        System.out.println("9. View All Orders (Detailed Summary)");

        System.out.println("-------------------------------------------");
        System.out.println("--- MEDICINES ---");
        System.out.println("10. Add New Medicine");
        System.out.println("11. Delete Medicine by ID");
        System.out.println("12. View All Medicines");

        System.out.println("-------------------------------------------");
        System.out.println("--- SUPPLIERS & LINKS ---");
        System.out.println("13. Add New Supplier");
        System.out.println("14. Delete Supplier by ID");
        System.out.println("15. Add/Update Medicine Link to Supplier");
        System.out.println("16. View Medicines by Supplier ID");
        System.out.println("17. View All Suppliers");

        System.out.println("-------------------------------------------");
        System.out.println("19. Exit");
        System.out.println("-------------------------------------------");
    }

    private static void readOrderItemsInteractive(OrderRepository orderRepo) throws SQLException {
        System.out.print("Enter Order ID: ");

        if (!SCANNER.hasNextLong()) {
            System.out.println("Invalid input. Please enter a valid numerical ID.");
            SCANNER.nextLine();
            return;
        }

        long orderId = SCANNER.nextLong();
        SCANNER.nextLine();

        List<OrderItem> items = orderRepo.getOrderItemsByOrderId(orderId);

        if (items.isEmpty()) {
            System.out.printf("No items found for Order ID %d (or Order does not exist).\n", orderId);
            return;
        }

        System.out.printf("\n--- Items for Order ID %d ---\n", orderId);
        System.out.printf("%-10s | %-12s | %-10s | %-15s\n", "Order ID", "Medicine ID", "Quantity", "Price at Purchase");
        System.out.println("-------------------------------------------------------");

        for (OrderItem item : items) {
            System.out.printf("%-10d | %-12d | %-10d | %-15.2f\n",
                    item.orderId(),
                    item.medicineId(),
                    item.quantity(),
                    item.priceAtPurchase());
        }
    }

    private static void readAllSuppliersInteractive(SupplierRepository supplierRepo) throws SQLException {
        List<Supplier> suppliers = supplierRepo.getAllSuppliers();

        if (suppliers.isEmpty()) {
            System.out.println("No suppliers found.");
            return;
        }

        System.out.printf("%-5s | %-25s | %-15s | %-15s\n", "ID", "Name", "Country", "City");
        System.out.println("----------------------------------------------------------------------");
        for (Supplier s : suppliers) {
            System.out.printf("%-5d | %-25s | %-15s | %-15s\n",
                    s.id(), s.name(), s.address().country(), s.address().city());
        }
    }

    private static void addMedicineInteractive(MedicineRepository medicineRepo) throws SQLException {
        System.out.print("Enter Medicine Name: ");
        String name = SCANNER.nextLine();
        System.out.print("Enter Unit Price (e.g., 5.99): ");
        double price = SCANNER.nextDouble();
        System.out.print("Enter Stock Quantity: ");
        int stock = SCANNER.nextInt();
        SCANNER.nextLine();

        Medicine newMedicine = new Medicine(0, name, price, stock);
        medicineRepo.addMedicine(newMedicine);
    }

    private static void deleteMedicineInteractive(MedicineRepository medicineRepo) throws SQLException {
        System.out.print("Enter Medicine ID to delete: ");
        long medicineId = SCANNER.nextLong();
        SCANNER.nextLine();

        medicineRepo.deleteMedicine(medicineId);
    }

    private static void readAllMedicinesInteractive(MedicineRepository medicineRepo) throws SQLException {
        List<Medicine> medicines = medicineRepo.getAllMedicines();

        if (medicines.isEmpty()) {
            System.out.println("No medicines found.");
            return;
        }

        System.out.printf("%-5s | %-30s | %-12s | %-6s\n", "ID", "Name", "Unit Price", "Stock");
        System.out.println("---------------------------------------------------------------");
        for (Medicine m : medicines) {
            System.out.printf("%-5d | %-30s | %-12.2f | %-6d\n",
                    m.id(), m.name(), m.unitPrice(), m.stock());
        }
    }

    private static void addSupplierInteractive(SupplierRepository supplierRepo) throws SQLException {
        System.out.print("Enter Supplier Name: ");
        String name = SCANNER.nextLine();

        System.out.print("Enter Country: ");
        String country = SCANNER.nextLine();
        System.out.print("Enter City: ");
        String city = SCANNER.nextLine();
        System.out.print("Enter Street: ");
        String street = SCANNER.nextLine();
        System.out.print("Enter Postal Code: ");
        String postalCode = SCANNER.nextLine();

        Address address = new Address(country, city, street, postalCode);
        Supplier newSupplier = new Supplier(0, name, address);
        supplierRepo.addSupplier(newSupplier);
    }

    private static void deleteSupplierInteractive(SupplierRepository supplierRepo) throws SQLException {
        System.out.print("Enter Supplier ID to delete: ");
        long supplierId = SCANNER.nextLong();
        SCANNER.nextLine();

        supplierRepo.deleteSupplier(supplierId);
    }

    private static void addMedicineToSupplierInteractive(SupplierRepository supplierRepo) throws SQLException {
        System.out.print("Enter Supplier ID: ");
        long supplierId = SCANNER.nextLong();
        System.out.print("Enter Medicine ID to link: ");
        long medicineId = SCANNER.nextLong();
        System.out.print("Enter Supply Price (Cost to Pharmacy): ");
        double supplyPrice = SCANNER.nextDouble();
        SCANNER.nextLine();

        SupplierMedicine link = new SupplierMedicine(supplierId, medicineId, supplyPrice);
        supplierRepo.addMedicineToSupplier(link);
    }

    private static void getMedicineBySupplierInteractive(SupplierRepository supplierRepo) throws SQLException {
        System.out.print("Enter Supplier ID to view medicines: ");
        long supplierId = SCANNER.nextLong();
        SCANNER.nextLine();

        List<SupplierMedicine> links = supplierRepo.getMedicineBySupplierId(supplierId);

        if (links.isEmpty()) {
            System.out.printf("Supplier ID %d does not supply any medicines.\n", supplierId);
            return;
        }

        System.out.printf("%-15s | %-15s | %-12s\n", "Supplier ID", "Medicine ID", "Supply Price");
        System.out.println("----------------------------------------------");

        for (SupplierMedicine link : links) {
            System.out.printf("%-15d | %-15d | %-12.2f\n",
                    link.supplierId(),
                    link.medicineId(),
                    link.supplyPrice());
        }
    }

    private static void addClientInteractive(ClientRepository clientRepo) throws SQLException {
        System.out.print("Enter First Name: ");
        String fName = SCANNER.nextLine();
        System.out.print("Enter Last Name: ");
        String lName = SCANNER.nextLine();

        System.out.print("Enter Country: ");
        String country = SCANNER.nextLine();
        System.out.print("Enter City: ");
        String city = SCANNER.nextLine();
        System.out.print("Enter Street: ");
        String street = SCANNER.nextLine();
        System.out.print("Enter Postal Code: ");
        String postalCode = SCANNER.nextLine();

        Address address = new Address(country, city, street, postalCode);
        Client newClient = new Client(0, fName, lName, address);

        clientRepo.addClient(newClient);
    }

    private static void updateClientAddressInteractive(ClientRepository clientRepo) throws SQLException {
        System.out.print("Enter Client ID to update: ");
        long clientId = SCANNER.nextLong();
        SCANNER.nextLine();

        System.out.print("Enter NEW Country: ");
        String country = SCANNER.nextLine();
        System.out.print("Enter NEW City: ");
        String city = SCANNER.nextLine();
        System.out.print("Enter NEW Street: ");
        String street = SCANNER.nextLine();
        System.out.print("Enter NEW Postal Code: ");
        String postalCode = SCANNER.nextLine();

        Address newAddress = new Address(country, city, street, postalCode);
        clientRepo.updateClientAddress(clientId, newAddress);
    }

    private static void deleteClientInteractive(ClientRepository clientRepo) throws SQLException {
        System.out.print("Enter Client ID to delete: ");
        long clientId = SCANNER.nextLong();
        SCANNER.nextLine();
        clientRepo.deleteClient(clientId);
    }

    private static void readAllClients(ClientRepository clientRepo) throws SQLException {
        List<Client> clients = clientRepo.getAllCLients();

        if (clients.isEmpty()) {
            System.out.println("No clients found.");
            return;
        }

        System.out.printf("%-5s | %-15s | %-15s | %-30s | %-15s\n", "ID", "First Name", "Last Name", "Street", "City");
        System.out.println("----------------------------------------------------------------------------------");
        for (Client c : clients) {
            System.out.printf("%-5d | %-15s | %-15s | %-30s | %-15s\n",
                    c.id(), c.firstname(), c.lastname(), c.address().street(), c.address().city());
        }
    }

    private static void createOrderInteractive(OrderRepository orderRepo) throws SQLException {
        System.out.print("Enter Client ID for the order: ");
        long clientId = SCANNER.nextLong();

        Map<Long, Integer> items = new HashMap<>();
        boolean addingItems = true;

        while (addingItems) {
            System.out.print("Enter Medicine ID (or 0 to finish): ");
            long medicineId = SCANNER.nextLong();
            if (medicineId == 0) {
                addingItems = false;
                continue;
            }
            System.out.print("Enter Quantity for Medicine " + medicineId + ": ");
            int quantity = SCANNER.nextInt();

            if (quantity > 0) {
                items.put(medicineId, quantity);
            } else {
                System.out.println("Quantity must be positive. Item skipped.");
            }
        }
        SCANNER.nextLine();

        if (!items.isEmpty()) {
            long newOrderId = orderRepo.createOrder(clientId, items);
            System.out.printf("SUCCESS: New Order created with ID %d.\n", newOrderId);
        } else {
            System.out.println("Order cancelled: No items were added.");
        }
    }

    private static void readAllDetailedOrdersInteractive(OrderRepository orderRepo) throws SQLException {
        List<OrderSummary> orders = orderRepo.getAllDetailedOrders();

        if (orders.isEmpty()) {
            System.out.println("No orders found in the system.");
            return;
        }

        System.out.printf("%-8s | %-12s | %-30s | %-10s | %-12s\n",
                "Order ID", "Date", "Client Name", "Items", "Total Price");
        System.out.println("----------------------------------------------------------------------------------");

        for (OrderSummary order : orders) {
            String clientName = order.clientFirstName() + " " + order.clientLastName();
            System.out.printf("%-8d | %-12s | %-30s | %-10d | %-12.2f\n",
                    order.orderId(),
                    order.orderDate(),
                    clientName,
                    order.totalItemsCount(),
                    order.totalPrice());
        }
    }

    private static void readOrdersByClientInteractive(OrderRepository orderRepo) throws SQLException {
        System.out.print("Enter Client ID to view orders: ");
        if (!SCANNER.hasNextLong()) {
            System.out.println("Invalid input. Please enter a valid numerical ID.");
            SCANNER.nextLine();
            return;
        }

        long clientId = SCANNER.nextLong();
        SCANNER.nextLine();

        List<Order> orders = orderRepo.getOrdersByClient(clientId);

        if (orders.isEmpty()) {
            System.out.printf("No orders found for Client ID %d.\n", clientId);
            return;
        }

        System.out.printf("%-10s | %-12s | %-15s\n", "Order ID", "Order Date", "Total Price");
        System.out.println("----------------------------------------------");

        for (Order order : orders) {
            System.out.printf("%-10d | %-12s | %-15.2f\n",
                    order.orderId(),
                    order.orderDate(),
                    order.totalPrice());
        }
    }

    private static void deleteOrderInteractive(OrderRepository orderRepo) throws SQLException {
        System.out.print("Enter Order ID to delete: ");
        long orderId = SCANNER.nextLong();
        SCANNER.nextLine();

        orderRepo.deleteOrder(orderId);
        System.out.printf("SUCCESS: Order ID %d deleted.\n", orderId);
    }
}