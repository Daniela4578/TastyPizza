package objects;

import java.math.BigDecimal;
import java.util.Objects;

public class ProductSize {

    private final Long       id;
    private final String     sizeLabel;
    private final BigDecimal price;

    private ProductSize(Builder builder) {
        this.id        = builder.id;
        this.sizeLabel = builder.sizeLabel;
        this.price     = builder.price;
    }

    public static Builder builder() { return new Builder(); }

    public Long       getId()        { return id; }
    public String     getSizeLabel() { return sizeLabel; }
    public BigDecimal getPrice()     { return price; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductSize s)) return false;
        return Objects.equals(id, s.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("%s — %.2f EUR", sizeLabel, price);
    }

    public static class Builder {
        private Long       id;
        private String     sizeLabel;
        private BigDecimal price;

        public Builder id(Long id)             { this.id = id;             return this; }
        public Builder sizeLabel(String label) { this.sizeLabel = label;   return this; }
        public Builder price(BigDecimal price) { this.price = price;       return this; }

        public ProductSize build() {
            Objects.requireNonNull(sizeLabel, "Size label is required");
            Objects.requireNonNull(price,     "Price is required");
            return new ProductSize(this);
        }
    }
}