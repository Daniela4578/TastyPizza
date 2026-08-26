package repositories.interfaces;

import objects.Order;
import objects.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByStatus(OrderStatus status);

    void updateStatus(Long orderId, OrderStatus status, Long processedBy, Integer estimatedMinutes);
}