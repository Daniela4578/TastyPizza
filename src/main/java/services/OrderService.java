package services;

import objects.*;
import repositories.Order.OrderRepository;

import java.math.BigDecimal;
import java.util.List;

public class OrderService {

    private static final BigDecimal DELIVERY_FEE = new BigDecimal("2.99");

    private final OrderRepository   orderRepository;
    private final IngredientService ingredientService;
    private final PaymentService    paymentService;

    public OrderService(OrderRepository orderRepository,
                        IngredientService ingredientService,
                        PaymentService paymentService) {
        this.orderRepository   = orderRepository;
        this.ingredientService = ingredientService;
        this.paymentService    = paymentService;
    }

    public Order placeOrder(Long customerId, Long addressId,
                            List<OrderItem> items, PaymentMethod paymentMethod) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Order must contain at least one item");

        BigDecimal itemsTotal = items.stream()
                .map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPrice = itemsTotal.add(DELIVERY_FEE);

        Order saved = orderRepository.save(Order.builder()
                .customerId(customerId).addressId(addressId)
                .status(OrderStatus.PENDING).deliveryFee(DELIVERY_FEE)
                .totalPrice(totalPrice).items(items).build());

        // deduct stock after saving
        items.forEach(item ->
                ingredientService.deductStockForProduct(item.getProductId(), item.getQuantity()));

        // create payment record
        paymentService.createPayment(saved.getId(), totalPrice, paymentMethod);

        return saved;
    }

    public List<Order> getMyOrders(Long customerId)  { return orderRepository.findByCustomerId(customerId); }
    public List<Order> getPendingOrders()            { return orderRepository.findByStatus(OrderStatus.PENDING); }
    public List<Order> getProcessingOrders()         { return orderRepository.findByStatus(OrderStatus.PROCESSING); }

    public void processOrder(Long orderId, Long employeeId, int estimatedMinutes) {
        if (estimatedMinutes <= 0)
            throw new IllegalArgumentException("Estimated delivery time must be greater than 0");
        orderRepository.findById(orderId).orElseThrow(() ->
                new IllegalArgumentException("Order not found: " + orderId));
        orderRepository.updateStatus(orderId, OrderStatus.PROCESSING, employeeId, estimatedMinutes);
    }

    public void deliverOrder(Long orderId) {
        orderRepository.findById(orderId).orElseThrow(() ->
                new IllegalArgumentException("Order not found: " + orderId));
        orderRepository.updateStatus(orderId, OrderStatus.DELIVERED, null, null);
        paymentService.completePayment(orderId);
    }

    public void cancelOrder(Long orderId) {
        orderRepository.findById(orderId).orElseThrow(() ->
                new IllegalArgumentException("Order not found: " + orderId));
        orderRepository.updateStatus(orderId, OrderStatus.CANCELLED, null, null);
    }
}