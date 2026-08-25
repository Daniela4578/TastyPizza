package repositories.Ingredient;

import objects.Ingredient;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IngredientRepository {
    List<Ingredient> findAll();
    List<Ingredient> findLowStock();
    Optional<Ingredient> findById(Long id);
    int deductStock(Long id, BigDecimal amount);
    void updateStock(Long id, BigDecimal newQuantity);
    void updateMinimumStock(Long id, BigDecimal newMinimum);
    List<IngredientQuantity> findByProductId(Long productId);
}