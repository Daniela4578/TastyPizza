package services;

import exceptions.OrderNotFoundException;
import objects.*;
import repositories.interfaces.OrderRepository;
import services.interfaces.IIngredientService;
import services.interfaces.IOrderService;
import services.interfaces.IPaymentService;

import java.math.BigDecimal;
import java.util.List;

public class OrderService implements IOrderService {

    private static final BigDecimal DELIVERY_FEE = new BigDecimal("2.99");

    private final OrderRepository orderRepository;
    private final IIngredientService ingredientService; // interface, not concrete
    private final IPaymentService paymentService;    // interface, not concrete

    public OrderService(OrderRepository orderRepository,
                        IIngredientService ingredientService,
                        IPaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.ingredientService = ingredientService;
        this.paymentService = paymentService;
    }

    @Override
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

    @Override
    public List<Order> getMyOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING);
    }

    @Override
    public List<Order> getProcessingOrders() {
        return orderRepository.findByStatus(OrderStatus.PROCESSING);
    }

    @Override
    public void processOrder(Long orderId, Long employeeId, int estimatedMinutes) {
        if (estimatedMinutes <= 0)
            throw new IllegalArgumentException("Estimated delivery time must be greater than 0");
        orderRepository.findById(orderId).orElseThrow(() ->
                new OrderNotFoundException("Order not found: " + orderId));
        orderRepository.updateStatus(orderId, OrderStatus.PROCESSING, employeeId, estimatedMinutes);
    }

    @Override
    public void deliverOrder(Long orderId) {
        orderRepository.findById(orderId).orElseThrow(() ->
                new OrderNotFoundException("Order not found: " + orderId));
        orderRepository.updateStatus(orderId, OrderStatus.DELIVERED, null, null);
        paymentService.completePayment(orderId);
    }

    @Override
    public void cancelOrder(Long orderId) {
        orderRepository.findById(orderId).orElseThrow(() ->
                new OrderNotFoundException("Order not found: " + orderId));
        orderRepository.updateStatus(orderId, OrderStatus.CANCELLED, null, null);
    }
}