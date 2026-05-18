package objects;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderItemTopping {

    private final Long id;
    private final Long orderItemId;
    private final Long ingredientId;
    private final String ingredientName;
    private final ToppingAction action;
    private final BigDecimal quantity;

    private OrderItemTopping(Builder builder) {
        this.id = builder.id;
        this.orderItemId = builder.orderItemId;
        this.ingredientId = builder.ingredientId;
        this.ingredientName = builder.ingredientName;
        this.action = builder.action;
        this.quantity = builder.quantity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public ToppingAction getAction() {
        return action;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItemTopping t)) return false;
        return Objects.equals(id, t.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        if (action == ToppingAction.REMOVE) {
            return String.format("No %s", ingredientName);
        }
        return String.format("Extra %s (%.0fg)", ingredientName,
                quantity != null ? quantity.multiply(BigDecimal.valueOf(1000)) : BigDecimal.ZERO);
    }

    public static class Builder {
        private Long id;
        private Long orderItemId;
        private Long ingredientId;
        private String ingredientName;
        private ToppingAction action;
        private BigDecimal quantity;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderItemId(Long orderItemId) {
            this.orderItemId = orderItemId;
            return this;
        }

        public Builder ingredientId(Long ingredientId) {
            this.ingredientId = ingredientId;
            return this;
        }

        public Builder ingredientName(String name) {
            this.ingredientName = name;
            return this;
        }

        public Builder action(ToppingAction action) {
            this.action = action;
            return this;
        }

        public Builder quantity(BigDecimal quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderItemTopping build() {
            Objects.requireNonNull(orderItemId, "Order item ID is required");
            Objects.requireNonNull(ingredientId, "Ingredient ID is required");
            Objects.requireNonNull(action, "Action is required");
            return new OrderItemTopping(this);
        }
    }
}