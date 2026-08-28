package repositories.interfaces;

import objects.Ingredient;
import repositories.IngredientQuantity;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IngredientRepository {
    List<Ingredient> findAll();
    List<Ingredient> findLowStock();
    Optional<Ingredient> findById(Long id);

    int deductStockInTransaction(Long id, BigDecimal amount, Connection conn) throws SQLException;

    int deductStock(Long id, BigDecimal amount);

    void updateStock(Long id, BigDecimal newQuantity);
    void updateMinimumStock(Long id, BigDecimal newMinimum);
    List<IngredientQuantity> findByProductId(Long productId);
}