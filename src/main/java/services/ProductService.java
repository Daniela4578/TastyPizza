package services;

import objects.Category;
import objects.Product;
import objects.ProductSize;
import repositories.interfaces.CategoryRepository;
import repositories.interfaces.ProductRepository;
import services.interfaces.IProductService;
import utilitis.ArgumentUtils;

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

    @Override
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Product> getAllActiveProducts() {
        return productRepository.findAllActive();
    }

    @Override
    public List<Product> getProductsByCategory(Long catId) {
        return productRepository.findActiveByCategoryId(catId);
    }

    @Override
    public List<ProductSize> getSizesByProduct(Long prodId) {
        return productRepository.findSizesByProductId(prodId);
    }

    @Override
    public List<Product> getInactiveProductsByIngredient(Long ingredientId) {
        return productRepository.findInactiveByIngredientId(ingredientId);
    }

    @Override
    public Product addProduct(String name, String description, BigDecimal price, Long categoryId) {
        ArgumentUtils.requireNonBlank(name, "Product name");
        ArgumentUtils.requireNonNegative(price, "Price");
        return productRepository.save(Product.builder()
                .name(name.trim()).description(description)
                .price(price).categoryId(categoryId).active(true).build());
    }

    @Override
    public void deactivateProduct(Long productId) {
        productRepository.findById(productId).orElseThrow(() ->
                new IllegalArgumentException("Product not found: " + productId));
        productRepository.setActive(productId, false);
    }

    @Override
    public void activateProduct(Long productId) {
        productRepository.findById(productId).orElseThrow(() ->
                new IllegalArgumentException("Product not found: " + productId));
        productRepository.setActive(productId, true);
    }
}