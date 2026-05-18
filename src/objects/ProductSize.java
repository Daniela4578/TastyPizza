package objects;

import java.math.BigDecimal;
import java.util.Objects;

public class ProductSize {

    private final Long id;
    private final Long productId;
    private final String sizeLabel;
    private final BigDecimal price;
    private final BigDecimal grammage;

    private ProductSize(Builder builder) {
        this.id = builder.id;
        this.productId = builder.productId;
        this.sizeLabel = builder.sizeLabel;
        this.price = builder.price;
        this.grammage = builder.grammage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSizeLabel() {
        return sizeLabel;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getGrammage() {
        return grammage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductSize s)) return false;
        return Objects.equals(id, s.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s — %.2f BGN", sizeLabel, price);
    }

    public static class Builder {
        private Long id;
        private Long productId;
        private String sizeLabel;
        private BigDecimal price;
        private BigDecimal grammage;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public Builder sizeLabel(String label) {
            this.sizeLabel = label;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder grammage(BigDecimal g) {
            this.grammage = g;
            return this;
        }

        public ProductSize build() {
            Objects.requireNonNull(productId, "Product ID is required");
            Objects.requireNonNull(sizeLabel, "Size label is required");
            Objects.requireNonNull(price, "Price is required");
            return new ProductSize(this);
        }
    }
}