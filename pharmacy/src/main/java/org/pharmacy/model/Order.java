package org.pharmacy.model;

import java.util.Date;

public class Order {
    private final long orderId;
    private final long clientId;
    private final Date orderDate;
    private final double totalPrice;

    public Order(long order_id, long client_id, Date order_date, double total_price){
        this.orderId = order_id;
        this.clientId = client_id;
        this.orderDate = order_date;
        this.totalPrice = total_price;
    }

    public long getOrderid() {
        return orderId;
    }

    public long getClientId() {
        return clientId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}
