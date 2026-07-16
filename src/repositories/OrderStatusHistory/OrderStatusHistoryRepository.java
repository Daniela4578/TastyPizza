package repositories.OrderStatusHistory;

import objects.OrderStatusHistory;
import java.util.List;

public interface OrderStatusHistoryRepository {
    List<OrderStatusHistory> findByOrderId(Long orderId);
}