package services;

import objects.Payment;
import objects.PaymentMethod;
import objects.PaymentStatus;
import repositories.interfaces.PaymentRepository;
import services.interfaces.IPaymentService;

import java.math.BigDecimal;
import java.util.Optional;

public class PaymentService implements IPaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createPayment(Long orderId, BigDecimal amount, PaymentMethod method) {
        return paymentRepository.save(Payment.builder()
                .orderId(orderId)
                .method(method)
                .status(PaymentStatus.PENDING)
                .amount(amount)
                .build());
    }

    public void completePayment(Long orderId) {
        paymentRepository.findByOrderId(orderId).orElseThrow(() ->
                new IllegalArgumentException("No payment found for order: " + orderId));
        paymentRepository.updateStatus(orderId, PaymentStatus.COMPLETED);
    }

    public Optional<Payment> getPaymentForOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}