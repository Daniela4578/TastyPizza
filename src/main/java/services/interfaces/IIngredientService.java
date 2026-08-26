package services.interfaces;

import objects.Ingredient;

import java.math.BigDecimal;
import java.util.List;

public interface IIngredientService {
    List<Ingredient> getAllIngredients();

    List<Ingredient> getLowStockIngredients();

    void restock(Long ingredientId, BigDecimal amountToAdd);

    void setMinimumStock(Long ingredientId, BigDecimal minimum);

    void deductStockForProduct(Long productId, int quantity);
}