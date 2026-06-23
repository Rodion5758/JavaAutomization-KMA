package org.example.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrderServiceMutationWeakTest {

    @Mock
    private Warehouse warehouse;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private Notifier notifier;

    @InjectMocks
    private OrderService service;

    @Test
    void feeIsPositive() {
        assertThat(service.calculateFee(150.0)).isPositive();
    }
}
