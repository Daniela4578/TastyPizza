package server.command.manager;

import exceptions.OrderNotFoundException;
import objects.Order;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class ViewAllOrdersCommand implements Command {
    @Override
    public String label() {
        return "View all orders";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        List<Order> orders = ctx.getServices().getOrderService().getPendingOrders();
        if (orders.isEmpty()) {
            ctx.println("\nNo pending orders.");
            return;
        }
        ctx.println("\nPENDING ORDERS:");
        orders.forEach(o -> {
            ctx.println(o.toString());
            ctx.println("---");
        });
        Long orderId = ctx.readLong("Enter order ID:");
        if (orderId == null) return;
        ctx.println("1 - Mark as delivered\n2 - Cancel order\n0 - Back\nChoose:");
        try {
            switch (ctx.readLine()) {
                case "1" -> {
                    ctx.getServices().getOrderService().deliverOrder(orderId);
                    ctx.println("Order #" + orderId + " DELIVERED.");
                }
                case "2" -> {
                    ctx.getServices().getOrderService().cancelOrder(orderId);
                    ctx.println("Order #" + orderId + " CANCELLED.");
                }
            }
        } catch (OrderNotFoundException | IllegalArgumentException e) {
            ctx.println("Error: " + e.getMessage());
        }
    }
}