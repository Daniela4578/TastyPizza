package server.command.manager;

import objects.Ingredient;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class ManageStockCommand implements Command {
    @Override
    public String label() {
        return "Manage stock";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        ctx.println("\nMANAGE STOCK:\n1 - View all\n2 - Restock\n3 - Set minimum stock\n0 - Back\nChoose:");
        var is = ctx.getServices().getIngredientService();
        switch (ctx.readLine()) {
            case "1" -> {
                List<Ingredient> all = is.getAllIngredients();
                if (all.isEmpty()) ctx.println("No ingredients.");
                else all.forEach(i -> ctx.println(i.toString()));
            }
            case "2" -> {
                is.getAllIngredients().forEach(i -> ctx.println(i.toString()));
                Long id = ctx.readLong("Ingredient ID to restock:");
                if (id == null) return;
                BigDecimal amount = ctx.readDecimal("Amount to add:");
                if (amount == null) return;
                try {
                    is.restock(id, amount);
                    ctx.println("Restocked. Products with all ingredients available have been reactivated.");
                } catch (IllegalArgumentException e) {
                    ctx.println("Error: " + e.getMessage());
                }
            }
            case "3" -> {
                is.getAllIngredients().forEach(i -> ctx.println(i.toString()));
                Long id = ctx.readLong("Ingredient ID:");
                if (id == null) return;
                BigDecimal min = ctx.readDecimal("New minimum stock:");
                if (min == null) return;
                try {
                    is.setMinimumStock(id, min);
                    ctx.println("Minimum stock updated.");
                } catch (IllegalArgumentException e) {
                    ctx.println("Error: " + e.getMessage());
                }
            }
        }
    }
}