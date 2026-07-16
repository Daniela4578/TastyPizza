package objects;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Payment {

    private final Long id;
    private final Long orderId;
    private final PaymentMethod method;
    private final PaymentStatus status;
    private final BigDecimal amount;
    private final String transactionRef;
    private final LocalDateTime paidAt;
    private final LocalDateTime createdAt;

    private Payment(Builder builder) {
        this.id = builder.id;
        this.orderId = builder.orderId;
        this.method = builder.method;
        this.status = builder.status;
        this.amount = builder.amount;
        this.transactionRef = builder.transactionRef;
        this.paidAt = builder.paidAt;
        this.createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment p)) return false;
        return Objects.equals(id, p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Payment[orderId: %d, method: %s, status: %s, amount: %.2f EUR]",
                orderId, method, status, amount);
    }

    public static class Builder {
        private Long id;
        private Long orderId;
        private PaymentMethod method;
        private PaymentStatus status;
        private BigDecimal amount;
        private String transactionRef;
        private LocalDateTime paidAt;
        private LocalDateTime createdAt;

        public Builder id(Long id)
        {
            this.id = id;
            return this;
        }

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder method(PaymentMethod method) {
            this.method = method;
            return this;
        }

        public Builder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder transactionRef(String ref) {
            this.transactionRef = ref;
            return this;
        }

        public Builder paidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
            return this;
        }

        public Builder createdAt(LocalDateTime created) {
            this.createdAt = created;
            return this;
        }

        public Payment build() {
            Objects.requireNonNull(orderId, "Order ID is required");
            Objects.requireNonNull(method, "Payment method is required");
            Objects.requireNonNull(amount, "Amount is required");
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Amount cannot be negative");
            }
            return new Payment(this);
        }
    }
}