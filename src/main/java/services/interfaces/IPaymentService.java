package services.interfaces;

import objects.Payment;
import objects.PaymentMethod;

import java.math.BigDecimal;
import java.util.Optional;

public interface IPaymentService {
    Payment createPayment(Long orderId, BigDecimal amount, PaymentMethod method);

    void completePayment(Long orderId);

    Optional<Payment> getPaymentForOrder(Long orderId);
}