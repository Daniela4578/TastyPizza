package repositories.EmployeeDetails;

import db.DatabaseConnection;
import objects.EmployeeDetails;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEmployeeDetailsRepository implements EmployeeDetailsRepository {
    private final DatabaseConnection databaseConnection;

    public JdbcEmployeeDetailsRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public EmployeeDetails save(EmployeeDetails details) {
        String sql = "INSERT INTO employee_details(user_id, salary, hire_date) VALUES (?, ?, ?)";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setLong(1, details.getUserId());
            statement.setBigDecimal(2, details.getSalary());
            statement.setDate(3, Date.valueOf(details.getHireDate()));

            statement.executeUpdate();

            return details;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save employee details for user: " + details.getUserId(), e);
        }
    }

    @Override
    public Optional<EmployeeDetails> findByUserId(Long userId) {
        String sql = "SELECT * FROM employee_details WHERE user_id = ?";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
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
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all employee details", e);
        }
        return list;
    }

    @Override
    public void updateSalary(Long userId, BigDecimal newSalary) {
        String sql = "UPDATE employee_details SET salary = ? WHERE user_id = ?";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setBigDecimal(1, newSalary);
            statement.setLong(2, userId);

            statement.executeUpdate();

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
