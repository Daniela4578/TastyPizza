package objects;

import java.time.LocalDateTime;
import java.util.Objects;

public class OrderStatusHistory {

    private final Long id;
    private final Long orderId;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;
    private final Long changedBy;
    private final String note;
    private final LocalDateTime changedAt;

    private OrderStatusHistory(Builder builder) {
        this.id = builder.id;
        this.orderId = builder.orderId;
        this.oldStatus = builder.oldStatus;
        this.newStatus = builder.newStatus;
        this.changedBy = builder.changedBy;
        this.note = builder.note;
        this.changedAt = builder.changedAt;
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

    public OrderStatus getOldStatus() {
        return oldStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderStatusHistory h)) return false;
        return Objects.equals(id, h.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String from = oldStatus != null ? oldStatus.name() : "NEW";
        return String.format("StatusChange[%s → %s at %s]", from, newStatus, changedAt);
    }

    public static class Builder {
        private Long id;
        private Long orderId;
        private OrderStatus oldStatus;
        private OrderStatus newStatus;
        private Long changedBy;
        private String note;
        private LocalDateTime changedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder oldStatus(OrderStatus old) {
            this.oldStatus = old;
            return this;
        }

        public Builder newStatus(OrderStatus newStatus) {
            this.newStatus = newStatus;
            return this;
        }

        public Builder changedBy(Long changedBy) {
            this.changedBy = changedBy;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public Builder changedAt(LocalDateTime time) {
            this.changedAt = time;
            return this;
        }

        public OrderStatusHistory build() {
            Objects.requireNonNull(orderId, "Order ID is required");
            Objects.requireNonNull(newStatus, "New status is required");
            return new OrderStatusHistory(this);
        }
    }
}