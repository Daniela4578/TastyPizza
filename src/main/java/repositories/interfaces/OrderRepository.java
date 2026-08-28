package repositories.interfaces;

import objects.Order;
import objects.OrderStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order, Connection conn) throws SQLException;

    Optional<Order> findById(Long id);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByStatus(OrderStatus status);

    void updateStatus(Long orderId, OrderStatus status, Long processedBy, Integer estimatedMinutes);
}