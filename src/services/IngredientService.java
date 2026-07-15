package services;

import objects.Ingredient;
import repositories.Ingredient.IngredientQuantity;
import repositories.Ingredient.IngredientRepository;

import java.math.BigDecimal;
import java.util.List;

public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final ProductService       productService;

    public IngredientService(IngredientRepository ingredientRepository,
                             ProductService productService) {
        this.ingredientRepository = ingredientRepository;
        this.productService       = productService;
    }

    public List<Ingredient> getAllIngredients()   { return ingredientRepository.findAll(); }
    public List<Ingredient> getLowStockIngredients(){ return ingredientRepository.findLowStock(); }

    public void restock(Long ingredientId, BigDecimal amountToAdd) {
        if (amountToAdd == null || amountToAdd.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");
        Ingredient ingredient = ingredientRepository.findById(ingredientId).orElseThrow(() ->
                new IllegalArgumentException("Ingredient not found: " + ingredientId));
        ingredientRepository.updateStock(ingredientId, ingredient.getStockQuantity().add(amountToAdd));
    }

    public void setMinimumStock(Long ingredientId, BigDecimal minimum) {
        if (minimum == null || minimum.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Minimum stock cannot be negative");
        ingredientRepository.findById(ingredientId).orElseThrow(() ->
                new IllegalArgumentException("Ingredient not found: " + ingredientId));
        ingredientRepository.updateMinimumStock(ingredientId, minimum);
    }


    public void deductStockForProduct(Long productId, int quantity) {
        List<IngredientQuantity> recipe = ingredientRepository.findByProductId(productId);

        for (IngredientQuantity iq : recipe) {
            ingredientRepository.findById(iq.getIngredientId()).ifPresent(ingredient -> {
                BigDecimal deduct   = iq.getStandardQuantity().multiply(BigDecimal.valueOf(quantity));
                BigDecimal newStock = ingredient.getStockQuantity().subtract(deduct);
                if (newStock.compareTo(BigDecimal.ZERO) < 0) newStock = BigDecimal.ZERO;
                ingredientRepository.updateStock(ingredient.getId(), newStock);

                // if out of stock — deactivate products that use this ingredient
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
}