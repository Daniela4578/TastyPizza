package repositories.Ingredient;

import db.DatabaseConnection;
import objects.Ingredient;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcIngredientRepository implements IngredientRepository {

    private final DatabaseConnection databaseConnection;

    public JdbcIngredientRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

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
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find ingredient: " + id, e);
        }
        return Optional.empty();
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
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new IngredientQuantity(
                            rs.getLong("ingredient_id"),
                            rs.getBigDecimal("standard_quantity")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load ingredients for product: " + productId, e);
        }
        return list;
    }

    private List<Ingredient> query(String sql) {
        List<Ingredient> list = new ArrayList<>();
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load ingredients", e);
        }
        return list;
    }

    private void updateField(String column, Long id, BigDecimal value) {
        String sql = "UPDATE ingredients SET " + column + " = ? WHERE id = ?";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
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
                .unit(rs.getString("unit")).stockQuantity(rs.getBigDecimal("stock_quantity"))
                .minimumStock(rs.getBigDecimal("minimum_stock")).build();
    }
}