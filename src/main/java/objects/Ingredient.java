package objects;

import java.math.BigDecimal;
import java.util.Objects;

public class Ingredient {

    private final Long id;
    private final String name;
    private final String unit;
    private final BigDecimal stockQuantity;
    private final BigDecimal minimumStock;
    private final int version;

    private Ingredient(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.unit = builder.unit;
        this.stockQuantity = builder.stockQuantity;
        this.minimumStock = builder.minimumStock;
        this.version = builder.version;
    }

    public static Builder builder() {
        return new Builder();
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

    public int getVersion() {
        return version;
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
        private int version = 0;

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

        public Builder minimumStock(BigDecimal min) {
            this.minimumStock = min;
            return this;
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public Ingredient build() {
            Objects.requireNonNull(name, "Ingredient name is required");
            Objects.requireNonNull(unit, "Unit is required");
            return new Ingredient(this);
        }
    }
}