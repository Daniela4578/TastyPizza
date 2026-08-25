package repositories.EmployeeDetails;

import db.DatabaseConnection;
import objects.EmployeeDetails;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEmployeeDetailsRepository implements EmployeeDetailsRepository {

    @Override
    public EmployeeDetails save(EmployeeDetails details) {
        String sql = "INSERT INTO employee_details(user_id, salary, hire_date) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, details.getUserId());
            stmt.setBigDecimal(2, details.getSalary());
            stmt.setDate(3, Date.valueOf(details.getHireDate()));
            stmt.executeUpdate();
            return details;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save employee details for user: " + details.getUserId(), e);
        }
    }

    @Override
    public Optional<EmployeeDetails> findByUserId(Long userId) {
        String sql = "SELECT * FROM employee_details WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find employee details: " + userId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<EmployeeDetails> findAll() {
        String sql = "SELECT * FROM employee_details";
        List<EmployeeDetails> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all employee details", e);
        }
        return list;
    }

    @Override
    public void updateSalary(Long userId, BigDecimal newSalary) {
        String sql = "UPDATE employee_details SET salary = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newSalary);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update salary for user: " + userId, e);
        }
    }

    private EmployeeDetails mapRow(ResultSet rs) throws SQLException {
        return EmployeeDetails.builder()
                .userId(rs.getLong("user_id"))
                .salary(rs.getBigDecimal("salary"))
                .hireDate(rs.getDate("hire_date").toLocalDate())
                .build();
    }
}