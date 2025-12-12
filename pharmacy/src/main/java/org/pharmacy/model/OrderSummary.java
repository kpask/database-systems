package org.pharmacy.model;

import java.util.Date;

/**
 * A summary view of an order, often used to display data aggregated from the database.
 * This record corresponds closely to the 'detailed_order_summary' SQL view.
 *
 * @param orderId The unique identifier of the order.
 * @param orderDate The date the order was placed.
 * @param clientFirstName The first name of the client.
 * @param clientLastName The last name of the client.
 * @param totalPrice The calculated total price of the order.
 * @param totalItemsCount The total number of unique items/units in the order.
 */
public record OrderSummary(
        long orderId,
        Date orderDate,
        String clientFirstName,
        String clientLastName,
        double totalPrice,
        long totalItemsCount // Must match the SUM(oi.quantity) result type
){}