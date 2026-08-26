package services.interfaces;

import objects.*;

import java.util.List;

public interface IOrderService {
    Order placeOrder(Long customerId, Long addressId, List<OrderItem> items, PaymentMethod paymentMethod);

    List<Order> getMyOrders(Long customerId);

    List<Order> getPendingOrders();

    List<Order> getProcessingOrders();

    void processOrder(Long orderId, Long employeeId, int estimatedMinutes);

    void deliverOrder(Long orderId);

    void cancelOrder(Long orderId);
}