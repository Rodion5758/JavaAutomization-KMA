package org.example.order;

public interface Warehouse {
    boolean reserve(String itemId, int qty);

    void release(String itemId, int qty);
}
