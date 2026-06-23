package org.example.order;

import java.util.List;

public class OrderService {
    private final Warehouse warehouse;
    private final PaymentGateway paymentGateway;
    private final Notifier notifier;

    public OrderService(Warehouse warehouse, PaymentGateway paymentGateway, Notifier notifier) {
        this.warehouse = warehouse;
        this.paymentGateway = paymentGateway;
        this.notifier = notifier;
    }

    public OrderResult processOrder(Order order) {
        if (!warehouse.reserve(order.getItemId(), order.getQty())) {
            notifier.notify(order.getCustomerId(), "Out of stock: " + order.getItemId());
            return new OrderResult(false, "Out of stock", List.of());
        }
        double fee = calculateFee(order.getAmount());
        if (!paymentGateway.charge(order.getCustomerId(), order.getAmount() + fee)) {
            warehouse.release(order.getItemId(), order.getQty());
            notifier.notify(order.getCustomerId(), "Payment failed");
            return new OrderResult(false, "Payment failed", List.of());
        }
        notifier.notify(order.getCustomerId(), "Order confirmed: " + order.getItemId());
        return new OrderResult(true, "OK", List.of(order.getItemId()));
    }

    public double calculateFee(double amount) {
        if (amount > 100.0) {
            return amount * 0.05;
        } else {
            return amount * 0.02;
        }
    }
}
