package org.example.order;

public interface PaymentGateway {
    boolean charge(String customerId, double amount);
}
