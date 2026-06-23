package org.example.order;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private Warehouse warehouse;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private Notifier notifier;

    @InjectMocks
    private OrderService service;

    private static final Order ORDER = new Order("ord-1", "cust-1", "item-1", 2, 150.0);

    @Test
    void successfulOrderReturnsOk() {
        when(warehouse.reserve("item-1", 2)).thenReturn(true);
        when(paymentGateway.charge(eq("cust-1"), anyDouble())).thenReturn(true);

        OrderResult result = service.processOrder(ORDER);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("OK");
    }

    @Test
    void outOfStockReturnsFailure() {
        when(warehouse.reserve("item-1", 2)).thenReturn(false);

        OrderResult result = service.processOrder(ORDER);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Out of stock");
    }

    @Test
    void paymentFailureReturnsFailure() {
        when(warehouse.reserve("item-1", 2)).thenReturn(true);
        when(paymentGateway.charge(eq("cust-1"), anyDouble())).thenReturn(false);

        OrderResult result = service.processOrder(ORDER);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Payment failed");
    }

    @Test
    void successfulOrderNotifiesOnce() {
        when(warehouse.reserve(anyString(), anyInt())).thenReturn(true);
        when(paymentGateway.charge(anyString(), anyDouble())).thenReturn(true);

        service.processOrder(ORDER);

        verify(notifier).notify(eq("cust-1"), anyString());
    }

    @Test
    void twoOrdersNotifyTwice() {
        when(warehouse.reserve(anyString(), anyInt())).thenReturn(true);
        when(paymentGateway.charge(anyString(), anyDouble())).thenReturn(true);
        Order second = new Order("ord-2", "cust-1", "item-2", 1, 50.0);

        service.processOrder(ORDER);
        service.processOrder(second);

        verify(notifier, times(2)).notify(eq("cust-1"), anyString());
    }

    @Test
    void successfulOrderNeverReleasesStock() {
        when(warehouse.reserve(anyString(), anyInt())).thenReturn(true);
        when(paymentGateway.charge(anyString(), anyDouble())).thenReturn(true);

        service.processOrder(ORDER);

        verify(warehouse, never()).release(anyString(), anyInt());
    }

    @Test
    void successfulOrderSoftAssertions() {
        when(warehouse.reserve("item-1", 2)).thenReturn(true);
        when(paymentGateway.charge(eq("cust-1"), anyDouble())).thenReturn(true);

        OrderResult result = service.processOrder(ORDER);

        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(result.isSuccess()).isTrue();
        soft.assertThat(result.getMessage()).isEqualTo("OK");
        soft.assertThat(result.getOrderedItems()).hasSize(1);
        soft.assertAll();
    }

    @Test
    void successfulOrderItemsListAssertions() {
        when(warehouse.reserve(anyString(), anyInt())).thenReturn(true);
        when(paymentGateway.charge(anyString(), anyDouble())).thenReturn(true);

        List<String> items = service.processOrder(ORDER).getOrderedItems();

        assertThat(items).hasSize(1);
        assertThat(items).containsExactly("item-1");
        assertThat(items).doesNotContain("item-2");
    }
}
