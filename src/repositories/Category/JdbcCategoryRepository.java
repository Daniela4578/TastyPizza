package repositories.Category;

import db.DatabaseConnection;
import objects.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCategoryRepository implements CategoryRepository {

    private final DatabaseConnection databaseConnection;

    public JdbcCategoryRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql);
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