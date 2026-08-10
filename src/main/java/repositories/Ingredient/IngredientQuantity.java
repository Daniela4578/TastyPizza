package repositories.Ingredient;

import java.math.BigDecimal;

public class IngredientQuantity {
    private final Long ingredientId;
    private final BigDecimal standardQuantity;

    public IngredientQuantity(Long ingredientId, BigDecimal standardQuantity) {
        this.ingredientId     = ingredientId;
        this.standardQuantity = standardQuantity;
    }

    public Long getIngredientId()          { return ingredientId; }
    public BigDecimal getStandardQuantity(){ return standardQuantity; }
}