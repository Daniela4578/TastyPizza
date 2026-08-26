package services.interfaces;

import objects.Category;
import objects.Product;
import objects.ProductSize;

import java.math.BigDecimal;
import java.util.List;

public interface IProductService {
    List<Category> getCategories();

    List<Product> getAllActiveProducts();

    List<Product> getProductsByCategory(Long categoryId);

    List<ProductSize> getSizesByProduct(Long productId);

    List<Product> getInactiveProductsByIngredient(Long ingredientId);

    Product addProduct(String name, String description, BigDecimal price, Long categoryId);

    void deactivateProduct(Long productId);

    void activateProduct(Long productId);
}