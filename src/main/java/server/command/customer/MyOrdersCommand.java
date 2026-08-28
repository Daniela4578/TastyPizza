package server.command.customer;

import objects.*;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class MyOrdersCommand implements Command {
    @Override
    public String label() {
        return "My orders";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        List<Order> orders = ctx.getServices().getOrderService().getMyOrders(ctx.getUser().getId());
        if (orders.isEmpty()) {
            ctx.println("\nNo orders yet.");
            return;
        }
        ctx.println("\nMY ORDERS:");
        for (Order o : orders) {
            ctx.println(o.toString());
            ctx.getServices().getPaymentService().getPaymentForOrder(o.getId()).ifPresent(p ->
                    ctx.println(String.format("  Payment: %s | %s",
                            p.getMethod() == PaymentMethod.CASH ? "Cash on delivery" : "Card", p.getStatus())));
            List<OrderStatusHistory> history = ctx.getServices().getOrderHistoryService().getHistory(o.getId());
            if (!history.isEmpty()) {
                ctx.println("Timeline:");
                history.forEach(h -> ctx.println(String.format("    %s \u2192 %s at %s",
                        h.getOldStatus() != null ? h.getOldStatus() : "NEW", h.getNewStatus(),
                        h.getChangedAt() != null ? h.getChangedAt().toLocalTime() : "unknown")));
            }
            ctx.println("---");
        }
    }
}