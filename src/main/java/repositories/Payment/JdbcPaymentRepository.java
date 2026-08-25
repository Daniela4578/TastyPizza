package repositories.Payment;

import db.DatabaseConnection;
import objects.Payment;
import objects.PaymentMethod;
import objects.PaymentStatus;

import java.sql.*;
import java.util.Optional;

public class JdbcPaymentRepository implements PaymentRepository {

    @Override
    public Payment save(Payment payment) {
        String sql = "INSERT INTO payments(order_id, method, status, amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, payment.getOrderId());
            stmt.setString(2, payment.getMethod().name());
            stmt.setString(3, payment.getStatus().name());
            stmt.setBigDecimal(4, payment.getAmount());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return Payment.builder()
                            .id(keys.getLong(1))
                            .orderId(payment.getOrderId())
                            .method(payment.getMethod())
                            .status(payment.getStatus())
                            .amount(payment.getAmount())
                            .build();
                }
                throw new SQLException("Failed to get generated payment ID");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save payment for order: " + payment.getOrderId(), e);
        }
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM payments WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payment for order: " + orderId, e);
        }
        return Optional.empty();
    }

    @Override
    public void updateStatus(Long orderId, PaymentStatus status) {
        String sql = "UPDATE payments SET status = ?, paid_at = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            if (status == PaymentStatus.COMPLETED) {
                stmt.setTimestamp(2, Timestamp.valueOf(java.time.LocalDateTime.now()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            stmt.setLong(3, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update payment status for order: " + orderId, e);
        }
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Timestamp paidAt = rs.getTimestamp("paid_at");
        return Payment.builder()
                .id(rs.getLong("id"))
                .orderId(rs.getLong("order_id"))
                .method(PaymentMethod.valueOf(rs.getString("method")))
                .status(PaymentStatus.valueOf(rs.getString("status")))
                .amount(rs.getBigDecimal("amount"))
                .paidAt(paidAt != null ? paidAt.toLocalDateTime() : null)
                .build();
    }
}