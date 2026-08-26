package services;

import objects.Ingredient;
import objects.Product;
import repositories.IngredientQuantity;
import repositories.interfaces.IngredientRepository;
import services.interfaces.IIngredientService;
import services.interfaces.IProductService;
import utilitis.ArgumentUtils;

import java.math.BigDecimal;
import java.util.List;

public class IngredientService implements IIngredientService {

    private final IngredientRepository ingredientRepository;
    private final IProductService productService;

    public IngredientService(IngredientRepository ingredientRepository,
                             IProductService productService) {
        this.ingredientRepository = ingredientRepository;
        this.productService = productService;
    }

    @Override
    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    @Override
    public List<Ingredient> getLowStockIngredients() {
        return ingredientRepository.findLowStock();
    }

    @Override
    public void restock(Long ingredientId, BigDecimal amountToAdd) {
        ArgumentUtils.requirePositive(amountToAdd, "Amount");

        Ingredient ingredient = ingredientRepository.findById(ingredientId).orElseThrow(() ->
                new IllegalArgumentException("Ingredient not found: " + ingredientId));

        ingredientRepository.updateStock(ingredientId,
                ingredient.getStockQuantity().add(amountToAdd));

        reactivateProductsIfPossible(ingredientId);
    }

    @Override
    public void setMinimumStock(Long ingredientId, BigDecimal minimum) {
        ArgumentUtils.requireNonNegative(minimum, "Minimum stock");
        ingredientRepository.findById(ingredientId).orElseThrow(() ->
                new IllegalArgumentException("Ingredient not found: " + ingredientId));
        ingredientRepository.updateMinimumStock(ingredientId, minimum);
    }

    @Override
    public void deductStockForProduct(Long productId, int quantity) {
        List<IngredientQuantity> recipe = ingredientRepository.findByProductId(productId);

        for (IngredientQuantity iq : recipe) {
            ingredientRepository.findById(iq.getIngredientId()).ifPresent(ingredient -> {
                BigDecimal deduct = iq.getStandardQuantity().multiply(BigDecimal.valueOf(quantity));
                BigDecimal newStock = ingredient.getStockQuantity().subtract(deduct);
                if (newStock.compareTo(BigDecimal.ZERO) < 0) newStock = BigDecimal.ZERO;
                ingredientRepository.updateStock(ingredient.getId(), newStock);

                if (newStock.compareTo(BigDecimal.ZERO) == 0) {
                    productService.getAllActiveProducts().forEach(product -> {
                        boolean usesIngredient = ingredientRepository
                                .findByProductId(product.getId()).stream()
                                .anyMatch(r -> r.getIngredientId().equals(ingredient.getId()));
                        if (usesIngredient) productService.deactivateProduct(product.getId());
                    });
                }
            });
        }
    }

    private void reactivateProductsIfPossible(Long restockedIngredientId) {
        List<Product> candidates = productService.getInactiveProductsByIngredient(restockedIngredientId);

        for (Product product : candidates) {
            List<IngredientQuantity> recipe = ingredientRepository.findByProductId(product.getId());

            boolean allInStock = recipe.stream().allMatch(iq ->
                    ingredientRepository.findById(iq.getIngredientId())
                            .map(ing -> ing.getStockQuantity().compareTo(BigDecimal.ZERO) > 0)
                            .orElse(false));

            if (allInStock) productService.activateProduct(product.getId());
        }
    }
}