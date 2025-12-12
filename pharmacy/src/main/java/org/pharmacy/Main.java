package org.pharmacy;

import org.pharmacy.exceptions.*;
import org.pharmacy.db.DBConnector;
import org.pharmacy.model.*;
import org.pharmacy.repository.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class for the Pharmacy Database Management System.
 * Manages the application lifecycle, database connection, exception handling,
 * and provides the interactive command-line interface (CLI) menu.
 */
public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);

    /**
     * Main entry point of the application.
     * Initializes the database connection and runs the main menu loop.
     *
     * @param args Command line arguments (unused).
     */
    public static void main(String[] args) {

        try (Connection conn = DBConnector.getConnection()) {

            // Initialize Repositories
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

    /**
     * Runs the main interactive menu loop, directing control to specific interactive methods
     * based on user input.
     *
     * @param clientRepo The repository for client operations.
     * @param orderRepo The repository for order operations.
     * @param medicineRepo The repository for medicine operations.
     * @param supplierRepo The repository for supplier operations.
     * @throws SQLException Thrown if a serious, unhandled database error occurs during execution.
     */
    private static void runMenu(
            ClientRepository clientRepo,
            OrderRepository orderRepo,
            MedicineRepository medicineRepo,
            SupplierRepository supplierRepo) throws SQLException {
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Select operation (1-20): ");

            if (!SCANNER.hasNextInt()) {
                System.out.println("\nInvalid input. Please enter a number.");
                SCANNER.next();
                continue;
            }

            int choice = SCANNER.nextInt();
            SCANNER.nextLine();

            try { // Separate try-catch block to handle exceptions thrown by interactive methods
                switch (choice) {
                    // CLIENT & ORDER OPERATIONS (1-8)
                    case 1: addClientInteractive(clientRepo); break;
                    case 2: updateClientAddressInteractive(clientRepo); break;
                    case 3: deleteClientInteractive(clientRepo); break;
                    case 4: readAllClientsInteractive(clientRepo); break;
                    case 5: createOrderInteractive(orderRepo, clientRepo, medicineRepo); break;
                    case 6: deleteOrderInteractive(orderRepo); break;
                    case 7: readDetailedOrdersByClientInteractive(orderRepo, clientRepo); break;
                    case 8: readAllDetailedOrdersInteractive(orderRepo); break;

                    // MEDICINE OPERATIONS (9-12)
                    case 9: addMedicineInteractive(medicineRepo); break;
                    case 10: deleteMedicineInteractive(medicineRepo); break;
                    case 11: readAllMedicinesInteractive(medicineRepo); break;
                    case 12: updateMedicineStockInteractive(medicineRepo); break;

                    // SUPPLIER OPERATIONS (13-17)
                    case 13: addSupplierInteractive(supplierRepo); break;
                    case 14: deleteSupplierInteractive(supplierRepo); break;
                    case 15: addMedicineToSupplierInteractive(supplierRepo); break;
                    case 16: getMedicineBySupplierInteractive(supplierRepo); break;
                    case 17: readAllSuppliersInteractive(supplierRepo); break;

                    case 20: // Exit
                        running = false;
                        System.out.println("Exiting application. Goodbye.");
                        break;
                    default:
                        System.out.println("Invalid choice. Please select a number between 1 and 20.");
                }
            } catch (DataNotFoundException | DataIntegrityViolationException e) {
                // Handle specific business/data layer exceptions gracefully
                System.err.println("\n### OPERATION FAILED: ###");
                System.err.println(e.getMessage());
            } catch (IllegalArgumentException e) {
                // Handle validation errors from Model/Repository
                System.err.println("\n### INPUT VALIDATION ERROR: ###");
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Prints the main menu options to the console.
     */
    private static void printMenu() {
        System.out.println("\n-------------------------------------------");
        System.out.println("--- CLIENTS & ORDERS ---");
        System.out.println("1. Add New Client");
        System.out.println("2. Update Client Address");
        System.out.println("3. Delete Client");
        System.out.println("4. View All Clients");
        System.out.println("5. Create New Order (Transaction)");
        System.out.println("6. Delete Order");
        System.out.println("7. View Detailed Order Summary by Client ID");
        System.out.println("8. View All Orders (Detailed Summary)");

        System.out.println("-------------------------------------------");
        System.out.println("--- MEDICINES ---");
        System.out.println("9. Add New Medicine");
        System.out.println("10. Delete Medicine by ID");
        System.out.println("11. View All Medicines");
        System.out.println("12. Update medicine stock");

        System.out.println("-------------------------------------------");
        System.out.println("--- SUPPLIERS & LINKS ---");
        System.out.println("13. Add New Supplier");
        System.out.println("14. Delete Supplier");
        System.out.println("15. Add/Update Medicine Link to Supplier");
        System.out.println("16. View Medicines by Supplier ID");
        System.out.println("17. View All Suppliers");

        System.out.println("-------------------------------------------");
        System.out.println("20. Exit");
        System.out.println("-------------------------------------------");
    }

    /**
     * Interactively prompts the user for medicine ID and a new stock value,
     * then updates the stock in the database.
     *
     * @param medicineRepo The repository for medicine operations.
     * @throws SQLException Thrown if a database access error occurs.
     * @throws DataNotFoundException Thrown if the medicine ID is not found.
     */
    private static void updateMedicineStockInteractive(MedicineRepository medicineRepo) throws SQLException, DataNotFoundException {
        System.out.print("Enter Medicine ID to update stock for: ");
        if (!SCANNER.hasNextLong()) {
            System.out.println("Invalid input. Please enter a valid numerical ID.");
            SCANNER.nextLine();
            return;
        }
        long medicineId = SCANNER.nextLong();

        System.out.print("Enter the NEW stock quantity: ");
        if (!SCANNER.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid numerical quantity.");
            SCANNER.nextLine();
            return;
        }
        int newStock = SCANNER.nextInt();
        SCANNER.nextLine();
        medicineRepo.updateMedicineStock(medicineId, newStock);
        System.out.printf("SUCCESS: Stock for Medicine ID %d updated to %d.\n", medicineId, newStock);
    }

    /**
     * Retrieves and displays a list of all suppliers currently in the database.
     *
     * @param supplierRepo The repository for supplier operations.
     * @throws SQLException Thrown if a database access error occurs.
     */
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

    /**
     * Interactively prompts the user for medicine details and adds a new medicine to the database.
     *
     * @param medicineRepo The repository for medicine operations.
     * @throws SQLException Thrown if a database access error occurs.
     */
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

    /**
     * Interactively prompts the user for a medicine ID and attempts to delete the record.
     *
     * @param medicineRepo The repository for medicine operations.
     * @throws SQLException Thrown if a database access error occurs.
     * @throws DataNotFoundException Thrown if the medicine ID is not found.
     * @throws DataIntegrityViolationException Thrown if the medicine is referenced elsewhere.
     */
    private static void deleteMedicineInteractive(MedicineRepository medicineRepo) throws SQLException {
        System.out.print("Enter Medicine ID to delete: ");
        long medicineId = SCANNER.nextLong();
        SCANNER.nextLine();

        medicineRepo.deleteMedicine(medicineId);
    }

    /**
     * Retrieves and displays a list of all medicine records currently in the database.
     *
     * @param medicineRepo The repository for medicine operations.
     * @throws SQLException Thrown if a database access error occurs.
     */
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

    /**
     * Interactively prompts the user for supplier and address details and adds a new supplier.
     *
     * @param supplierRepo The repository for supplier operations.
     * @throws SQLException Thrown if a database access error occurs.
     */
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

    /**
     * Interactively prompts the user for a supplier ID and attempts to delete the record.
     *
     * @param supplierRepo The repository for supplier operations.
     * @throws SQLException Thrown if a database access error occurs.
     * @throws DataNotFoundException Thrown if the supplier ID is not found.
     * @throws DataIntegrityViolationException Thrown if the supplier is referenced elsewhere.
     */
    private static void deleteSupplierInteractive(SupplierRepository supplierRepo) throws SQLException {
        System.out.print("Enter Supplier ID to delete: ");
        long supplierId = SCANNER.nextLong();
        SCANNER.nextLine();

        supplierRepo.deleteSupplier(supplierId);
    }

    /**
     * Interactively prompts the user for IDs and price to link a medicine to a supplier (UPSERT operation).
     *
     * @param supplierRepo The repository for supplier operations.
     * @throws SQLException Thrown if a database access error occurs.
     * @throws DataIntegrityViolationException Thrown if either ID does not exist.
     */
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

    /**
     * Interactively prompts the user for a supplier ID and displays all medicines they supply.
     *
     * @param supplierRepo The repository for supplier operations.
     * @throws SQLException Thrown if a database access error occurs.
     */
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

    /**
     * Interactively prompts the user for client and address details and adds a new client.
     *
     * @param clientRepo The repository for client operations.
     * @throws SQLException Thrown if a database access error occurs.
     */
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

    /**
     * Interactively prompts the user for a client ID and new address details, then updates the record.
     *
     * @param clientRepo The repository for client operations.
     * @throws SQLException Thrown if a database access error occurs.
     */
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

    /**
     * Interactively prompts the user for a client ID and attempts to delete the record.
     *
     * @param clientRepo The repository for client operations.
     * @throws SQLException Thrown if a database access error occurs.
     * @throws DataNotFoundException Thrown if the client ID is not found.
     * @throws DataIntegrityViolationException Thrown if the client has existing orders.
     */
    private static void deleteClientInteractive(ClientRepository clientRepo) throws SQLException {
        System.out.print("Enter Client ID to delete: ");
        long clientId = SCANNER.nextLong();
        SCANNER.nextLine();
        clientRepo.deleteClient(clientId);
    }

    /**
     * Retrieves and displays a list of all client records currently in the database.
     *
     * @param clientRepo The repository for client operations.
     * @throws SQLException Thrown if a database access error occurs.
     */
    private static void readAllClientsInteractive(ClientRepository clientRepo) throws SQLException {
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

    /**
     * Interactively guides the user through creating a new order.
     * This operation is handled as a database transaction (all-or-nothing).
     *
     * @param orderRepo The repository for order operations.
     * @param clientRepo The repository for client operations (for display).
     * @param medicineRepo The repository for medicine operations (for display).
     * @throws SQLException Thrown if a database access error occurs.
     * @throws DataIntegrityViolationException Thrown if the transaction fails (e.g., insufficient stock).
     */
    private static void createOrderInteractive(OrderRepository orderRepo, ClientRepository clientRepo, MedicineRepository medicineRepo) throws SQLException {
        // ... (metodo turinys - iškviečia orderRepo.createOrder) ...
        readAllClientsInteractive(clientRepo);
        System.out.print("Enter Client ID for the order: ");
        long clientId = SCANNER.nextLong();

        Map<Long, Integer> items = new HashMap<>();
        boolean addingItems = true;

        readAllMedicinesInteractive(medicineRepo);
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
            try {
                long newOrderId = orderRepo.createOrder(clientId, items);
                System.out.printf("SUCCESS: New Order created with ID %d.\n", newOrderId);
            } catch (Exception e){
                // Šis catch blokas ignoruos klaidas, kurios atskirai apdorojamos runMenu metode,
                // bet geresnė praktika būtų perduoti DataIntegrityViolationException aukštyn.
                // Atnaujinu jį, kad bent išvestų klaidos pranešimą, jei runMenu negali pilnai apdoroti.
                System.err.println("\n### ORDER TRANSACTION FAILED ###");
                System.err.println("Reason: " + e.getMessage());
            }

        } else {
            System.out.println("Order cancelled: No items were added.");
        }
    }

    /**
     * Interactively prompts the user for a client ID and displays all detailed order summaries for that client.
     *
     * @param orderRepo The repository for order operations.
     * @param clientRepo The repository for client operations (for display).
     * @throws SQLException Thrown if a database access error occurs.
     * @throws DataNotFoundException Thrown if the client has no orders or does not exist.
     */
    private static void readDetailedOrdersByClientInteractive(OrderRepository orderRepo, ClientRepository clientRepo) throws SQLException {
        readAllClientsInteractive(clientRepo);
        System.out.print("Enter Client ID to view all their orders: ");

        if (!SCANNER.hasNextLong()) {
            System.out.println("Invalid input. Please enter a valid numerical ID.");
            SCANNER.nextLine();
            return;
        }

        long clientId = SCANNER.nextLong();
        SCANNER.nextLine();

        try {
            List<OrderSummary> orders = orderRepo.getClientOrderSummaries(clientId);
            // ... (spausdinimo logika) ...
            if (orders.isEmpty()) {
                System.out.printf("\nClient ID %d has no orders or does not exist.\n", clientId);
                return;
            }

            System.out.printf("\n--- Detailed Order Summaries for Client ID %d (%s %s) ---\n",
                    clientId,
                    orders.getFirst().clientFirstName(),
                    orders.getFirst().clientLastName());

            System.out.printf("%-8s | %-12s | %-10s | %-12s\n",
                    "Order ID", "Date", "Items", "Total Price");
            System.out.println("-------------------------------------------------------");

            // Print every order
            for (OrderSummary order : orders) {
                System.out.printf("%-8d | %-12s | %-10d | %-12.2f\n",
                        order.orderId(),
                        order.orderDate(),
                        order.totalItemsCount(),
                        order.totalPrice());
            }
            System.out.println("-------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error occurred while fetching orders: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves and displays detailed summaries of all orders in the system.
     *
     * @param orderRepo The repository for order operations.
     * @throws SQLException Thrown if a database access error occurs.
     */
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

    /**
     * Interactively prompts the user for an order ID and attempts to delete the record.
     *
     * @param orderRepo The repository for order operations.
     * @throws SQLException Thrown if a database access error occurs.
     */
    private static void deleteOrderInteractive(OrderRepository orderRepo) throws SQLException {
        System.out.print("Enter Order ID to delete: ");
        long orderId = SCANNER.nextLong();
        SCANNER.nextLine();

        orderRepo.deleteOrder(orderId);
        System.out.printf("SUCCESS: Order ID %d deleted.\n", orderId);
    }
}