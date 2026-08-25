package repositories.OrderStatusHistory;

import db.DatabaseConnection;
import objects.OrderStatus;
import objects.OrderStatusHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcOrderStatusHistoryRepository implements OrderStatusHistoryRepository {

    @Override
    public List<OrderStatusHistory> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM order_status_history WHERE order_id = ? ORDER BY changed_at ASC";
        List<OrderStatusHistory> history = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) history.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load status history for order: " + orderId, e);
        }
        return history;
    }

    private OrderStatusHistory mapRow(ResultSet rs) throws SQLException {
        String oldStatusStr = rs.getString("old_status");
        OrderStatus oldStatus = oldStatusStr != null ? OrderStatus.valueOf(oldStatusStr) : null;
        Timestamp changedAt = rs.getTimestamp("changed_at");

        return OrderStatusHistory.builder()
                .id(rs.getLong("id"))
                .orderId(rs.getLong("order_id"))
                .oldStatus(oldStatus)
                .newStatus(OrderStatus.valueOf(rs.getString("new_status")))
                .changedAt(changedAt != null ? changedAt.toLocalDateTime() : null)
                .build();
    }
}