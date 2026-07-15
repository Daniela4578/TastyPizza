package repositories.Order;

import db.DatabaseConnection;
import objects.Order;
import objects.OrderItem;
import objects.OrderStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcOrderRepository implements OrderRepository {

    private final DatabaseConnection databaseConnection;

    public JdbcOrderRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public Order save(Order order) {
        String orderSql = "INSERT INTO orders(customer_id, address_id, status, delivery_fee, total_price) VALUES (?, ?, ?, ?, ?)";
        String itemSql  = "INSERT INTO order_items(order_id, product_id, product_size_id, quantity, unit_price, special_instructions) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = databaseConnection.getConnection();

        try {
            conn.setAutoCommit(false);
            Long orderId;

            try (PreparedStatement stmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, order.getCustomerId());
                stmt.setLong(2, order.getAddressId());
                stmt.setString(3, order.getStatus().name());
                stmt.setBigDecimal(4, order.getDeliveryFee());
                stmt.setBigDecimal(5, order.getTotalPrice());
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Failed to get generated order ID");
                    orderId = keys.getLong(1);
                }
            }

            for (OrderItem item : order.getItems()) {
                try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                    stmt.setLong(1, orderId);
                    stmt.setLong(2, item.getProductId());
                    if (item.getProductSizeId() != null) stmt.setLong(3, item.getProductSizeId());
                    else stmt.setNull(3, Types.BIGINT);
                    stmt.setInt(4, item.getQuantity());
                    stmt.setBigDecimal(5, item.getUnitPrice());
                    stmt.setString(6, item.getSpecialInstructions());
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            return Order.builder()
                    .id(orderId)
                    .customerId(order.getCustomerId())
                    .addressId(order.getAddressId())
                    .status(order.getStatus())
                    .deliveryFee(order.getDeliveryFee())
                    .totalPrice(order.getTotalPrice())
                    .items(order.getItems())
                    .build();

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Failed to save order", e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        String sql = "SELECT o.*, u.first_name, u.last_name, a.name AS address_name " +
                "FROM orders o JOIN users u ON u.id = o.customer_id " +
                "JOIN addresses a ON a.id = o.address_id WHERE o.id = ?";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapRow(rs);
                    List<OrderItem> items = findItemsByOrderId(id);
                    return Optional.of(Order.builder()
                            .id(order.getId()).customerId(order.getCustomerId())
                            .customerName(order.getCustomerName()).addressId(order.getAddressId())
                            .addressName(order.getAddressName()).processedBy(order.getProcessedBy())
                            .status(order.getStatus()).estimatedDeliveryMinutes(order.getEstimatedDeliveryMinutes())
                            .deliveryFee(order.getDeliveryFee()).totalPrice(order.getTotalPrice())
                            .createdAt(order.getCreatedAt()).updatedAt(order.getUpdatedAt())
                            .items(items).build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        String sql = "SELECT o.*, u.first_name, u.last_name, a.name AS address_name " +
                "FROM orders o JOIN users u ON u.id = o.customer_id " +
                "JOIN addresses a ON a.id = o.address_id " +
                "WHERE o.customer_id = ? ORDER BY o.created_at DESC";
        return queryOrders(sql, customerId);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        String sql = "SELECT o.*, u.first_name, u.last_name, a.name AS address_name " +
                "FROM orders o JOIN users u ON u.id = o.customer_id " +
                "JOIN addresses a ON a.id = o.address_id " +
                "WHERE o.status = ? ORDER BY o.created_at ASC";
        List<Order> orders = new ArrayList<>();
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) orders.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load orders by status: " + status, e);
        }
        return orders;
    }

    @Override
    public void updateStatus(Long orderId, OrderStatus status, Long processedBy, Integer estimatedMinutes) {
        String sql = "UPDATE orders SET status = ?, processed_by = ?, estimated_delivery = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            if (processedBy != null) stmt.setLong(2, processedBy);
            else stmt.setNull(2, Types.BIGINT);
            if (estimatedMinutes != null) stmt.setInt(3, estimatedMinutes);
            else stmt.setNull(3, Types.INTEGER);
            stmt.setLong(4, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order status: " + orderId, e);
        }
    }

    private List<Order> queryOrders(String sql, Long param) {
        List<Order> orders = new ArrayList<>();
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) orders.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load orders", e);
        }
        return orders;
    }

    private List<OrderItem> findItemsByOrderId(Long orderId) {
        String sql = "SELECT oi.*, p.name AS product_name, ps.size_label " +
                "FROM order_items oi JOIN products p ON p.id = oi.product_id " +
                "LEFT JOIN product_sizes ps ON ps.id = oi.product_size_id WHERE oi.order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement stmt = databaseConnection.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long sizeId = rs.getLong("product_size_id");
                    Long productSizeId = rs.wasNull() ? null : sizeId;
                    items.add(OrderItem.builder()
                            .id(rs.getLong("id")).orderId(orderId)
                            .productId(rs.getLong("product_id")).productName(rs.getString("product_name"))
                            .productSizeId(productSizeId).sizeName(rs.getString("size_label"))
                            .quantity(rs.getInt("quantity")).unitPrice(rs.getBigDecimal("unit_price"))
                            .specialInstructions(rs.getString("special_instructions")).build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load items for order: " + orderId, e);
        }
        return items;
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        long processedByVal = rs.getLong("processed_by");
        Long processedBy = rs.wasNull() ? null : processedByVal;
        int estVal = rs.getInt("estimated_delivery");
        Integer estimated = rs.wasNull() ? null : estVal;
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return Order.builder()
                .id(rs.getLong("id")).customerId(rs.getLong("customer_id"))
                .customerName(rs.getString("first_name") + " " + rs.getString("last_name"))
                .addressId(rs.getLong("address_id")).addressName(rs.getString("address_name"))
                .processedBy(processedBy).status(OrderStatus.valueOf(rs.getString("status")))
                .estimatedDeliveryMinutes(estimated)
                .deliveryFee(rs.getBigDecimal("delivery_fee")).totalPrice(rs.getBigDecimal("total_price"))
                .createdAt(createdAt != null ? createdAt.toLocalDateTime() : null)
                .updatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null)
                .build();
    }
}