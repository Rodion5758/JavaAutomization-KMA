package org.example.order;

public class Order {
    private final String orderId;
    private final String customerId;
    private final String itemId;
    private final int qty;
    private final double amount;

    public Order(String orderId, String customerId, String itemId, int qty, double amount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.itemId = itemId;
        this.qty = qty;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQty() {
        return qty;
    }

    public double getAmount() {
        return amount;
    }
}
