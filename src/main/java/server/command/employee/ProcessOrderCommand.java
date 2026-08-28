package server.command.employee;

import exceptions.OrderNotFoundException;
import objects.Order;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class ProcessOrderCommand implements Command {
    @Override
    public String label() {
        return "Process order";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        List<Order> pending = ctx.getServices().getOrderService().getPendingOrders();
        if (pending.isEmpty()) {
            ctx.println("\nNo pending orders to process.");
            return;
        }
        ctx.println("\nPENDING ORDERS:");
        pending.forEach(o -> {
            ctx.println(o.toString());
            ctx.println("---");
        });
        Long orderId = ctx.readLong("Enter order ID to process:");
        if (orderId == null) return;
        ctx.println("Estimated delivery time (minutes):");
        int minutes;
        try {
            minutes = Integer.parseInt(ctx.readLine());
        } catch (NumberFormatException e) {
            ctx.println("Invalid time.");
            return;
        }
        try {
            ctx.getServices().getOrderService().processOrder(orderId, ctx.getUser().getId(), minutes);
            ctx.println("Order #" + orderId + " is now PROCESSING. ETA: " + minutes + " minutes.");
        } catch (OrderNotFoundException | IllegalArgumentException e) {
            ctx.println("Error: " + e.getMessage());
        }
    }
}