package repositories;

import db.DatabaseConnection;
import objects.AccountStatus;
import objects.Role;
import objects.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    private final DatabaseConnection databaseConnection;

    public JdbcUserRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users(email, password_hash, first_name, last_name, phone, role, status, date_of_birth) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try(PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
            statement.setString(5, user.getPhoneNumber());
            statement.setString(6, user.getRole().toString());
            statement.setString(7, user.getStatus().name());
            statement.setDate(8, Date.valueOf(user.getDateOfBirth()));

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    return User.builder()
                            .id(id)
                            .email(user.getEmail())
                            .passwordHash(user.getPasswordHash())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .phoneNumber(user.getPhoneNumber())
                            .role(user.getRole())
                            .dateOfBirth(user.getDateOfBirth())
                            .status(user.getStatus())
                            .build();
                }
                throw new SQLException("Failed to retrieve generated id after save.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user: " + user.getEmail(), e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try(PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)){
            statement.setString(1, email);

            try(ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    User user = User.builder()
                            .id(resultSet.getLong("id"))
                            .email(resultSet.getString("email"))
                            .passwordHash(resultSet.getString("password_hash"))
                            .firstName(resultSet.getString("first_name"))
                            .lastName(resultSet.getString("last_name"))
                            .phoneNumber(resultSet.getString("phone"))
                            .role(Role.valueOf(resultSet.getString("role")))
                            .dateOfBirth(resultSet.getDate("date_of_birth").toLocalDate())
                            .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                            .status(AccountStatus.valueOf(resultSet.getString("status")))
                            .build();

                    return Optional.of(user);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve user: " + email, e);
        }
    }
}
