package objects;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order {

    private final Long id;
    private final Long customerId;
    private final String customerName;
    private final Long addressId;
    private final String addressName;
    private final Long processedBy;
    private final OrderStatus status;
    private final Integer estimatedDeliveryMinutes;
    private final BigDecimal deliveryFee;
    private final BigDecimal totalPrice;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<OrderItem> items;

    private Order(Builder builder) {
        this.id = builder.id;
        this.customerId = builder.customerId;
        this.customerName = builder.customerName;
        this.addressId = builder.addressId;
        this.addressName = builder.addressName;
        this.processedBy = builder.processedBy;
        this.status = builder.status;
        this.estimatedDeliveryMinutes = builder.estimatedDeliveryMinutes;
        this.deliveryFee = builder.deliveryFee;
        this.totalPrice = builder.totalPrice;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.items = Collections.unmodifiableList(new ArrayList<>(builder.items));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Long getAddressId() {
        return addressId;
    }

    public String getAddressName() {
        return addressName;
    }

    public Long getProcessedBy() {
        return processedBy;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Integer getEstimatedDeliveryMinutes() {
        return estimatedDeliveryMinutes;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Order #%d | %s | Total: %.2f EUR", id, status, totalPrice));
        if (estimatedDeliveryMinutes != null) {
            sb.append(String.format(" | ETA: %d min", estimatedDeliveryMinutes));
        }
        for (OrderItem item : items) {
            sb.append("\n").append(item);
        }
        return sb.toString();
    }

    public static class Builder {
        private Long id;
        private Long customerId;
        private String customerName;
        private Long addressId;
        private String addressName;
        private Long processedBy;
        private OrderStatus status = OrderStatus.PENDING;
        private Integer estimatedDeliveryMinutes;
        private BigDecimal deliveryFee = BigDecimal.ZERO;
        private BigDecimal totalPrice = BigDecimal.ZERO;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<OrderItem> items = new ArrayList<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder customerId(Long customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder customerName(String name) {
            this.customerName = name;
            return this;
        }

        public Builder addressId(Long addressId) {
            this.addressId = addressId;
            return this;
        }

        public Builder addressName(String name) {
            this.addressName = name;
            return this;
        }

        public Builder processedBy(Long processedBy) {
            this.processedBy = processedBy;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder estimatedDeliveryMinutes(Integer mins) {
            this.estimatedDeliveryMinutes = mins;
            return this;
        }

        public Builder deliveryFee(BigDecimal fee) {
            this.deliveryFee = fee;
            return this;
        }

        public Builder totalPrice(BigDecimal total) {
            this.totalPrice = total;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public Order build() {
            Objects.requireNonNull(customerId, "Customer ID is required");
            Objects.requireNonNull(addressId, "Address ID is required");
            Objects.requireNonNull(status, "Status is required");
            return new Order(this);
        }
    }
}