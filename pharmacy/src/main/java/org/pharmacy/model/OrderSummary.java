package org.pharmacy.model;

import java.util.Date;

public record OrderSummary(
        long orderId,
        Date orderDate,
        String clientFirstName,
        String clientLastName,
        double totalPrice,
        long totalItemsCount // Must match the SUM(oi.quantity) result type
){}