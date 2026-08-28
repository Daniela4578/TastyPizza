package server.command.employee;

import exceptions.OrderNotFoundException;
import objects.Order;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class DeliverOrderCommand implements Command {
    @Override
    public String label() {
        return "Mark order as delivered";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        List<Order> processing = ctx.getServices().getOrderService().getProcessingOrders();
        if (processing.isEmpty()) {
            ctx.println("\nNo orders currently being processed.");
            return;
        }
        ctx.println("\nPROCESSING ORDERS:");
        processing.forEach(o -> {
            ctx.println(o.toString());
            ctx.println("---");
        });
        Long orderId = ctx.readLong("Enter order ID to mark as delivered:");
        if (orderId == null) return;
        try {
            ctx.getServices().getOrderService().deliverOrder(orderId);
            ctx.println("Order #" + orderId + " DELIVERED. Payment COMPLETED.");
        } catch (OrderNotFoundException | IllegalArgumentException e) {
            ctx.println("Error: " + e.getMessage());
        }
    }
}