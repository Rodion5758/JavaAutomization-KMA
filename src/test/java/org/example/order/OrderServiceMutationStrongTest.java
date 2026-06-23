package org.example.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

@ExtendWith(MockitoExtension.class)
class OrderServiceMutationStrongTest {

    @Mock
    private Warehouse warehouse;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private Notifier notifier;

    @InjectMocks
    private OrderService service;

    @Test
    void feeForLargeOrder() {
        assertThat(service.calculateFee(150.0)).isEqualTo(7.5, offset(0.001));
    }

    @Test
    void feeForSmallOrder() {
        assertThat(service.calculateFee(50.0)).isEqualTo(1.0, offset(0.001));
    }

    @Test
    void feeAtBoundaryIsSmallRate() {
        assertThat(service.calculateFee(100.0)).isEqualTo(2.0, offset(0.001));
    }

    @Test
    void feeJustAboveBoundaryIsLargeRate() {
        assertThat(service.calculateFee(100.01)).isEqualTo(5.0005, offset(0.001));
    }
}
