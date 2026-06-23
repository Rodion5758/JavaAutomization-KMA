package org.example.order;

import java.util.List;

public class OrderResult {
    private final boolean success;
    private final String message;
    private final List<String> orderedItems;

    public OrderResult(boolean success, String message, List<String> orderedItems) {
        this.success = success;
        this.message = message;
        this.orderedItems = orderedItems;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getOrderedItems() {
        return orderedItems;
    }
}
