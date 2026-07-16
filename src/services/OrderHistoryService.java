package services;

import objects.OrderStatusHistory;
import repositories.OrderStatusHistory.OrderStatusHistoryRepository;

import java.util.List;

public class OrderHistoryService {

    private final OrderStatusHistoryRepository historyRepository;

    public OrderHistoryService(OrderStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public List<OrderStatusHistory> getHistory(Long orderId) {
        return historyRepository.findByOrderId(orderId);
    }
}