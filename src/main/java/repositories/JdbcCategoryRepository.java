package repositories;

import db.DatabaseConnection;
import objects.Category;
import repositories.interfaces.CategoryRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCategoryRepository implements CategoryRepository {

    @Override
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(Category.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load categories", e);
        }
        return list;
    }
}