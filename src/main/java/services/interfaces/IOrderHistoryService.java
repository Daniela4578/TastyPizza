package services.interfaces;

import objects.OrderStatusHistory;

import java.util.List;

public interface IOrderHistoryService {
    List<OrderStatusHistory> getHistory(Long orderId);
}