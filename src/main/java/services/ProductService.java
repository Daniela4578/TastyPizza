package services;

import objects.Category;
import objects.Product;
import objects.ProductSize;
import repositories.interfaces.CategoryRepository;
import repositories.interfaces.ProductRepository;
import services.interfaces.IProductService;

import java.math.BigDecimal;
import java.util.List;

public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findAllActive();
    }

    public List<Product> getProductsByCategory(Long catId) {
        return productRepository.findActiveByCategoryId(catId);
    }

    public List<ProductSize> getSizesByProduct(Long prodId) {
        return productRepository.findSizesByProductId(prodId);
    }

    public List<Product> getInactiveProductsByIngredient(Long ingredientId) {
        return productRepository.findInactiveByIngredientId(ingredientId);
    }

    public Product addProduct(String name, String description, BigDecimal price, Long categoryId) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Product name cannot be empty");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Price must be zero or greater");
        return productRepository.save(Product.builder()
                .name(name.trim()).description(description)
                .price(price).categoryId(categoryId).active(true).build());
    }

    public void deactivateProduct(Long productId) {
        productRepository.findById(productId).orElseThrow(() ->
                new IllegalArgumentException("Product not found: " + productId));
        productRepository.setActive(productId, false);
    }

    public void activateProduct(Long productId) {
        productRepository.findById(productId).orElseThrow(() ->
                new IllegalArgumentException("Product not found: " + productId));
        productRepository.setActive(productId, true);
    }
}