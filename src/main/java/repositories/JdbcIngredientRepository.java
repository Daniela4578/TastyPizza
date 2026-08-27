package repositories;

import db.DatabaseConnection;
import exceptions.InsufficientStockException;
import objects.Ingredient;
import repositories.interfaces.IngredientRepository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * HOW OPTIMISTIC LOCKING WORKS:
 * Instead of locking the row (which blocks other threads), we:
 * 1. Read the current stock AND version number
 * 2. Check if stock is enough
 * 3. Update only if version hasn't changed since we read
 * 4. If 0 rows updated → another thread changed it → retry
 * <p>
 * Example with two threads both trying to use the last 1kg of flour:
 * Thread 1 reads: stock=1, version=5
 * Thread 2 reads: stock=1, version=5
 * Thread 1 updates WHERE version=5 → succeeds, version becomes 6
 * Thread 2 updates WHERE version=5 → 0 rows (version is now 6) → retries
 * Thread 2 retries: reads stock=0, version=6 → throws InsufficientStockException
 * <p>
 * No locks, threads never block each other.
 */
public class JdbcIngredientRepository implements IngredientRepository {

    private static final int MAX_RETRIES = 3;

    @Override
    public List<Ingredient> findAll() {
        return query("SELECT * FROM ingredients ORDER BY name");
    }

    @Override
    public List<Ingredient> findLowStock() {
        return query("SELECT * FROM ingredients WHERE stock_quantity < minimum_stock ORDER BY name");
    }

    @Override
    public Optional<Ingredient> findById(Long id) {
        String sql = "SELECT * FROM ingredients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find ingredient: " + id, e);
        }
        return Optional.empty();
    }

    /**
     * Deducts stock using optimistic locking with automatic retry.
     * Returns 1 if successful, throws InsufficientStockException if not enough stock.
     */
    @Override
    public int deductStock(Long id, BigDecimal amount) {
        return deductWithRetry(id, amount, 0);
    }

    private int deductWithRetry(Long id, BigDecimal amount, int attempt) {
        if (attempt >= MAX_RETRIES) {
            throw new InsufficientStockException(
                    "Could not deduct stock after " + MAX_RETRIES + " retries for ingredient: " + id);
        }

        //  1. Read the current stock AND version number
        Ingredient ingredient = findById(id).orElseThrow(() ->
                new IllegalArgumentException("Ingredient not found: " + id));

        // 2. Check if stock is enough
        if (ingredient.getStockQuantity().compareTo(amount) < 0) {
            throw new InsufficientStockException(
                    "Not enough stock for ingredient: " + ingredient.getName() +
                            " (need: " + amount + ", have: " + ingredient.getStockQuantity() + ")");
        }

        // 3. Update only if version hasn't changed since we read
        String sql = "UPDATE ingredients " +
                "SET stock_quantity = stock_quantity - ?, version = version + 1 " +
                "WHERE id = ? AND version = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, amount);
            stmt.setLong(2, id);
            stmt.setInt(3, ingredient.getVersion());
            int affected = stmt.executeUpdate();

            // 4. If 0 rows updated → another thread changed it → retry
            if (affected == 0) {
                return deductWithRetry(id, amount, attempt + 1);
            }
            return affected;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to deduct stock for ingredient: " + id, e);
        }
    }

    @Override
    public void updateStock(Long id, BigDecimal newQuantity) {
        updateField("stock_quantity", id, newQuantity);
    }

    @Override
    public void updateMinimumStock(Long id, BigDecimal newMinimum) {
        updateField("minimum_stock", id, newMinimum);
    }

    @Override
    public List<IngredientQuantity> findByProductId(Long productId) {
        String sql = "SELECT ingredient_id, standard_quantity FROM product_ingredients WHERE product_id = ?";
        List<IngredientQuantity> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(new IngredientQuantity(
                        rs.getLong("ingredient_id"), rs.getBigDecimal("standard_quantity")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load ingredients for product: " + productId, e);
        }
        return list;
    }

    private List<Ingredient> query(String sql) {
        List<Ingredient> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load ingredients", e);
        }
        return list;
    }

    private void updateField(String column, Long id, BigDecimal value) {
        String sql = "UPDATE ingredients SET " + column + " = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, value);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update " + column + " for ingredient: " + id, e);
        }
    }

    private Ingredient mapRow(ResultSet rs) throws SQLException {
        return Ingredient.builder()
                .id(rs.getLong("id")).name(rs.getString("name"))
                .unit(rs.getString("unit"))
                .stockQuantity(rs.getBigDecimal("stock_quantity"))
                .minimumStock(rs.getBigDecimal("minimum_stock"))
                .version(rs.getInt("version")) // read version from DB
                .build();
    }
}