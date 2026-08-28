package server.command.employee;

import objects.Ingredient;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class ViewLowStockCommand implements Command {
    @Override
    public String label() {
        return "View low stock";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        List<Ingredient> low = ctx.getServices().getIngredientService().getLowStockIngredients();
        if (low.isEmpty()) {
            ctx.println("\nAll ingredients sufficiently stocked.");
            return;
        }
        ctx.println("\nLOW STOCK ALERT:");
        low.forEach(i -> ctx.println(i.toString()));
    }
}