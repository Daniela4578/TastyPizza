package services;

import db.DatabaseConnection;
import exceptions.InsufficientStockException;
import exceptions.OrderNotFoundException;
import objects.*;
import repositories.IngredientQuantity;
import repositories.interfaces.IngredientRepository;
import repositories.interfaces.OrderRepository;
import repositories.interfaces.PaymentRepository;
import services.interfaces.IOrderService;
import services.interfaces.IProductService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class OrderService implements IOrderService {

    private static final BigDecimal DELIVERY_FEE = new BigDecimal("2.99");

    private final OrderRepository      orderRepository;
    private final IngredientRepository ingredientRepository; // for transactional stock deduction
    private final PaymentRepository    paymentRepository;    // for transactional payment save
    private final IProductService      productService;       // for product deactivation after commit

    public OrderService(OrderRepository orderRepository,
                        IngredientRepository ingredientRepository,
                        PaymentRepository paymentRepository,
                        IProductService productService) {
        this.orderRepository      = orderRepository;
        this.ingredientRepository = ingredientRepository;
        this.paymentRepository    = paymentRepository;
        this.productService       = productService;
    }

    @Override
    public Order placeOrder(Long customerId, Long addressId,
                            List<OrderItem> items, PaymentMethod paymentMethod) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Order must contain at least one item");

        BigDecimal itemsTotal = items.stream()
                .map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPrice = itemsTotal.add(DELIVERY_FEE);

        Order saved;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {

                saved = orderRepository.save(Order.builder()
                        .customerId(customerId).addressId(addressId)
                        .status(OrderStatus.PENDING).deliveryFee(DELIVERY_FEE)
                        .totalPrice(totalPrice).items(items).build(), conn);


                for (OrderItem item : items) {
                    List<IngredientQuantity> recipe =
                            ingredientRepository.findByProductId(item.getProductId());
                    for (IngredientQuantity iq : recipe) {
                        BigDecimal deduct = iq.getStandardQuantity()
                                .multiply(BigDecimal.valueOf(item.getQuantity()));
                        ingredientRepository.deductStockInTransaction(
                                iq.getIngredientId(), deduct, conn);
                    }
                }

                savePaymentInTransaction(saved.getId(), totalPrice, paymentMethod, conn);

                conn.commit();

            } catch (InsufficientStockException | IllegalArgumentException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Failed to place order", e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to place order — database error", e);
        }

        deactivateProductsWithZeroStock(items);

        return saved;
    }

    private void savePaymentInTransaction(Long orderId, BigDecimal amount,
                                          PaymentMethod method, Connection conn) throws SQLException {
        String sql = "INSERT INTO payments(order_id, method, status, amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setString(2, method.name());
            stmt.setString(3, PaymentStatus.PENDING.name());
            stmt.setBigDecimal(4, amount);
            stmt.executeUpdate();
        }
    }

    private void deactivateProductsWithZeroStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            ingredientRepository.findByProductId(item.getProductId()).forEach(iq ->
                    ingredientRepository.findById(iq.getIngredientId()).ifPresent(ingredient -> {
                        if (ingredient.getStockQuantity().compareTo(BigDecimal.ZERO) == 0) {
                            productService.getAllActiveProducts().forEach(product -> {
                                boolean uses = ingredientRepository.findByProductId(product.getId())
                                        .stream().anyMatch(r -> r.getIngredientId().equals(ingredient.getId()));
                                if (uses) productService.deactivateProduct(product.getId());
                            });
                        }
                    })
            );
        }
    }

    @Override public List<Order> getMyOrders(Long customerId)  { return orderRepository.findByCustomerId(customerId); }
    @Override public List<Order> getPendingOrders()            { return orderRepository.findByStatus(OrderStatus.PENDING); }
    @Override public List<Order> getProcessingOrders()         { return orderRepository.findByStatus(OrderStatus.PROCESSING); }

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
        paymentRepository.updateStatus(orderId, PaymentStatus.COMPLETED);
    }

    @Override
    public void cancelOrder(Long orderId) {
        orderRepository.findById(orderId).orElseThrow(() ->
                new OrderNotFoundException("Order not found: " + orderId));
        orderRepository.updateStatus(orderId, OrderStatus.CANCELLED, null, null);
    }
}