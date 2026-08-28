package server.command.employee;

import objects.Order;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class ViewPendingOrdersCommand implements Command {
    @Override
    public String label() {
        return "View pending orders";
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
    }
}