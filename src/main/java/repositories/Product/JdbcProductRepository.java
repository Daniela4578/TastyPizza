package repositories.Product;

import db.DatabaseConnection;
import objects.Product;
import objects.ProductSize;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductRepository implements ProductRepository {

    @Override
    public List<Product> findAllActive() {
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                "JOIN categories c ON c.id = p.category_id " +
                "WHERE p.is_active = TRUE ORDER BY c.name, p.name";
        return queryProducts(sql, null);
    }

    @Override
    public List<Product> findActiveByCategoryId(Long categoryId) {
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                "JOIN categories c ON c.id = p.category_id " +
                "WHERE p.is_active = TRUE AND p.category_id = ? ORDER BY p.name";
        return queryProducts(sql, categoryId);
    }

    @Override
    public Optional<Product> findById(Long id) {
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                "JOIN categories c ON c.id = p.category_id WHERE p.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find product: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ProductSize> findSizesByProductId(Long productId) {
        String sql = "SELECT * FROM product_sizes WHERE product_id = ? ORDER BY price";
        List<ProductSize> sizes = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sizes.add(ProductSize.builder()
                            .id(rs.getLong("id"))
                            .sizeLabel(rs.getString("size_label"))
                            .price(rs.getBigDecimal("price"))
                            .build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load sizes for product: " + productId, e);
        }
        return sizes;
    }

    @Override
    public Product save(Product product) {
        String sql = "INSERT INTO products(name, description, price, base_grammage, category_id, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setBigDecimal(4, product.getBaseGrammage());
            stmt.setLong(5, product.getCategoryId());
            stmt.setBoolean(6, product.isActive());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return Product.builder()
                            .id(keys.getLong(1)).name(product.getName())
                            .description(product.getDescription()).price(product.getPrice())
                            .baseGrammage(product.getBaseGrammage()).categoryId(product.getCategoryId())
                            .categoryName(product.getCategoryName()).active(true).build();
                }
                throw new SQLException("Failed to get generated product ID");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save product: " + product.getName(), e);
        }
    }

    @Override
    public void setActive(Long id, boolean active) {
        String sql = "UPDATE products SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update product active status: " + id, e);
        }
    }

    @Override
    public List<Product> findInactiveByIngredientId(Long ingredientId) {
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                "JOIN categories c ON c.id = p.category_id " +
                "JOIN product_ingredients pi ON pi.product_id = p.id " +
                "WHERE pi.ingredient_id = ? AND p.is_active = FALSE";
        return queryProducts(sql, ingredientId);
    }

    private List<Product> queryProducts(String sql, Long param) {
        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (param != null) stmt.setLong(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load products", e);
        }
        return list;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return Product.builder()
                .id(rs.getLong("id")).name(rs.getString("name"))
                .description(rs.getString("description")).price(rs.getBigDecimal("price"))
                .baseGrammage(rs.getBigDecimal("base_grammage"))
                .categoryId(rs.getLong("category_id")).categoryName(rs.getString("category_name"))
                .active(rs.getBoolean("is_active")).build();
    }
}