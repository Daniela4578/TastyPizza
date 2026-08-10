package repositories.Product;

import objects.Product;
import objects.ProductSize;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> findAllActive();
    List<Product> findActiveByCategoryId(Long categoryId);
    Optional<Product> findById(Long id);
    List<ProductSize> findSizesByProductId(Long productId);
    Product save(Product product);
    void setActive(Long id, boolean active);
    List<Product> findInactiveByIngredientId(Long ingredientId);
}