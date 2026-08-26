package repositories;

import db.DatabaseConnection;
import objects.AccountStatus;
import objects.Role;
import objects.User;
import repositories.interfaces.UserRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users(email, password_hash, first_name, last_name, " +
                "phone, role, status, date_of_birth) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getPhoneNumber());
            stmt.setString(6, user.getRole().name());
            stmt.setString(7, user.getStatus().name());
            stmt.setDate(8, Date.valueOf(user.getDateOfBirth()));
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return User.builder()
                            .id(keys.getLong(1))
                            .email(user.getEmail())
                            .passwordHash(user.getPasswordHash())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .phoneNumber(user.getPhoneNumber())
                            .role(user.getRole())
                            .status(user.getStatus())
                            .dateOfBirth(user.getDateOfBirth())
                            .build();
                }
                throw new SQLException("Failed to retrieve generated user ID");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user: " + user.getEmail(), e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email: " + email, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findByStatus(AccountStatus status) {
        String sql = "SELECT * FROM users WHERE status = ? ORDER BY created_at";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find users by status: " + status, e);
        }
        return users;
    }

    @Override
    public void updateStatus(Long userId, AccountStatus status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update status for user: " + userId, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("password_hash"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .phoneNumber(rs.getString("phone"))
                .role(Role.valueOf(rs.getString("role")))
                .status(AccountStatus.valueOf(rs.getString("status")))
                .dateOfBirth(rs.getDate("date_of_birth").toLocalDate())
                .build();
    }
}