package objects;

import java.math.BigDecimal;
import java.util.Objects;

public class Ingredient {

    private final Long id;
    private final String name;
    private final String unit;
    private final BigDecimal stockQuantity;
    private final BigDecimal minimumStock;

    private Ingredient(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.unit = builder.unit;
        this.stockQuantity = builder.stockQuantity;
        this.minimumStock = builder.minimumStock;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isLowStock() {
        return stockQuantity.compareTo(minimumStock) < 0;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getStockQuantity() {
        return stockQuantity;
    }

    public BigDecimal getMinimumStock() {
        return minimumStock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingredient i)) return false;
        return Objects.equals(id, i.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Ingredient[%s: %.2f %s (min: %.2f)]",
                name, stockQuantity, unit, minimumStock);
    }

    public static class Builder {
        private Long id;
        private String name;
        private String unit;
        private BigDecimal stockQuantity = BigDecimal.ZERO;
        private BigDecimal minimumStock = BigDecimal.ZERO;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }

        public Builder stockQuantity(BigDecimal stock) {
            this.stockQuantity = stock;
            return this;
        }

        public Builder minimumStock(BigDecimal minimum) {
            this.minimumStock = minimum;
            return this;
        }

        public Ingredient build() {
            Objects.requireNonNull(name, "Ingredient name is required");
            Objects.requireNonNull(unit, "Unit is required");
            return new Ingredient(this);
        }
    }
}