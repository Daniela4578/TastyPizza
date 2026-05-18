package objects;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {

    private final Long id;
    private final Long orderId;
    private final Long productId;
    private final String productName;
    private final Long productSizeId;
    private final String sizeName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final String specialInstructions;

    private OrderItem(Builder builder) {
        this.id = builder.id;
        this.orderId = builder.orderId;
        this.productId = builder.productId;
        this.productName = builder.productName;
        this.productSizeId = builder.productSizeId;
        this.sizeName = builder.sizeName;
        this.quantity = builder.quantity;
        this.unitPrice = builder.unitPrice;
        this.specialInstructions = builder.specialInstructions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getProductSizeId() {
        return productSizeId;
    }

    public String getSizeName() {
        return sizeName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem i)) return false;
        return Objects.equals(id, i.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String size = sizeName != null ? " (" + sizeName + ")" : "";
        String note = specialInstructions != null && !specialInstructions.isBlank()
                ? " [" + specialInstructions + "]" : "";
        return String.format("  %dx %s%s — %.2f BGN%s",
                quantity, productName, size, getSubtotal(), note);
    }

    public static class Builder {
        private Long id;
        private Long orderId;
        private Long productId;
        private String productName;
        private Long productSizeId;
        private String sizeName;
        private int quantity;
        private BigDecimal unitPrice;
        private String specialInstructions;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public Builder productName(String name) {
            this.productName = name;
            return this;
        }

        public Builder productSizeId(Long sizeId) {
            this.productSizeId = sizeId;
            return this;
        }

        public Builder sizeName(String sizeName) {
            this.sizeName = sizeName;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder unitPrice(BigDecimal price) {
            this.unitPrice = price;
            return this;
        }

        public Builder specialInstructions(String instr) {
            this.specialInstructions = instr;
            return this;
        }

        public OrderItem build() {
            Objects.requireNonNull(productId, "Product ID is required");
            Objects.requireNonNull(unitPrice, "Unit price is required");
            if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than 0");
            return new OrderItem(this);
        }
    }
}