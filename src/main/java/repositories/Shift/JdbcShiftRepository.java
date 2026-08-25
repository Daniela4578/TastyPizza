package repositories.Shift;

import db.DatabaseConnection;
import objects.Shift;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcShiftRepository implements ShiftRepository {

    @Override
    public Shift save(Shift shift) {
        String sql = "INSERT INTO shifts(employee_id, shift_date, start_time, end_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, shift.getEmployeeId());
            stmt.setDate(2, Date.valueOf(shift.getShiftDate()));
            stmt.setTime(3, Time.valueOf(shift.getStartTime()));
            stmt.setTime(4, Time.valueOf(shift.getEndTime()));
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return Shift.builder()
                            .id(keys.getLong(1))
                            .employeeId(shift.getEmployeeId())
                            .employeeName(shift.getEmployeeName())
                            .shiftDate(shift.getShiftDate())
                            .startTime(shift.getStartTime())
                            .endTime(shift.getEndTime())
                            .build();
                }
                throw new SQLException("Failed to get generated shift ID");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save shift", e);
        }
    }

    @Override
    public List<Shift> findByEmployeeId(Long employeeId) {
        String sql = "SELECT s.*, u.first_name, u.last_name FROM shifts s " +
                "JOIN users u ON u.id = s.employee_id " +
                "WHERE s.employee_id = ? ORDER BY s.shift_date, s.start_time";
        return query(sql, employeeId);
    }

    @Override
    public List<Shift> findByDate(LocalDate date) {
        String sql = "SELECT s.*, u.first_name, u.last_name FROM shifts s " +
                "JOIN users u ON u.id = s.employee_id " +
                "WHERE s.shift_date = ? ORDER BY s.start_time";
        List<Shift> shifts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) shifts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load shifts for date: " + date, e);
        }
        return shifts;
    }

    @Override
    public void deleteById(Long shiftId) {
        String sql = "DELETE FROM shifts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, shiftId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete shift: " + shiftId, e);
        }
    }

    private List<Shift> query(String sql, Long param) {
        List<Shift> shifts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) shifts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load shifts", e);
        }
        return shifts;
    }

    private Shift mapRow(ResultSet rs) throws SQLException {
        return Shift.builder()
                .id(rs.getLong("id"))
                .employeeId(rs.getLong("employee_id"))
                .employeeName(rs.getString("first_name") + " " + rs.getString("last_name"))
                .shiftDate(rs.getDate("shift_date").toLocalDate())
                .startTime(rs.getTime("start_time").toLocalTime())
                .endTime(rs.getTime("end_time").toLocalTime())
                .build();
    }
}