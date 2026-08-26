package services;

import objects.OrderStatusHistory;
import repositories.interfaces.OrderStatusHistoryRepository;
import services.interfaces.IOrderHistoryService;

import java.util.List;

public class OrderHistoryService implements IOrderHistoryService {

    private final OrderStatusHistoryRepository historyRepository;

    public OrderHistoryService(OrderStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public List<OrderStatusHistory> getHistory(Long orderId) {
        return historyRepository.findByOrderId(orderId);
    }
}