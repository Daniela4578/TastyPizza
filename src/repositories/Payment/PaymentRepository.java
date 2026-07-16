package repositories.Payment;

import objects.Payment;
import objects.PaymentStatus;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByOrderId(Long orderId);
    void updateStatus(Long orderId, PaymentStatus status);
}