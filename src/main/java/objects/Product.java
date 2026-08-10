package objects;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {

    private final Long       id;
    private final String     name;
    private final String     description;
    private final BigDecimal price;
    private final BigDecimal baseGrammage;
    private final Long       categoryId;
    private final String     categoryName;
    private final boolean    active;

    private Product(Builder builder) {
        this.id           = builder.id;
        this.name         = builder.name;
        this.description  = builder.description;
        this.price        = builder.price;
        this.baseGrammage = builder.baseGrammage;
        this.categoryId   = builder.categoryId;
        this.categoryName = builder.categoryName;
        this.active       = builder.active;
    }

    public static Builder builder() { return new Builder(); }

    public Long       getId()           { return id; }
    public String     getName()         { return name; }
    public String     getDescription()  { return description; }
    public BigDecimal getPrice()        { return price; }
    public BigDecimal getBaseGrammage() { return baseGrammage; }
    public Long       getCategoryId()   { return categoryId; }
    public String     getCategoryName() { return categoryName; }
    public boolean    isActive()        { return active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product p)) return false;
        return Objects.equals(id, p.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("Product[id: %d, name: '%s', price: %.2f EUR, category: '%s']",
                id, name, price, categoryName);
    }

    public static class Builder {
        private Long       id;
        private String     name;
        private String     description;
        private BigDecimal price;
        private BigDecimal baseGrammage;
        private Long       categoryId;
        private String     categoryName;
        private boolean    active = true;

        public Builder id(Long id)                  { this.id = id;                 return this; }
        public Builder name(String name)            { this.name = name;             return this; }
        public Builder description(String desc)     { this.description = desc;      return this; }
        public Builder price(BigDecimal price)      { this.price = price;           return this; }
        public Builder baseGrammage(BigDecimal g)   { this.baseGrammage = g;        return this; }
        public Builder categoryId(Long categoryId)  { this.categoryId = categoryId; return this; }
        public Builder categoryName(String catName) { this.categoryName = catName;  return this; }
        public Builder active(boolean active)       { this.active = active;         return this; }

        public Product build() {
            Objects.requireNonNull(name,       "Product name is required");
            Objects.requireNonNull(price,      "Product price is required");
            Objects.requireNonNull(categoryId, "Category ID is required");
            return new Product(this);
        }
    }
}