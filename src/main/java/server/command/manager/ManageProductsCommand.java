package server.command.manager;

import objects.*;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class ManageProductsCommand implements Command {
    @Override
    public String label() {
        return "Manage products";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        ctx.println("\nMANAGE PRODUCTS:\n1 - Add product\n2 - Deactivate product\n0 - Back\nChoose:");
        var ps = ctx.getServices().getProductService();
        switch (ctx.readLine()) {
            case "1" -> {
                List<Category> cats = ps.getCategories();
                if (cats.isEmpty()) {
                    ctx.println("No categories.");
                    return;
                }
                for (int i = 0; i < cats.size(); i++) ctx.println((i + 1) + " - " + cats.get(i).getName());
                ctx.println("Choose category:");
                int catIdx;
                try {
                    catIdx = Integer.parseInt(ctx.readLine()) - 1;
                } catch (NumberFormatException e) {
                    ctx.println("Invalid.");
                    return;
                }
                if (catIdx < 0 || catIdx >= cats.size()) {
                    ctx.println("Invalid.");
                    return;
                }
                ctx.println("Product name:");
                String name = ctx.readLine();
                ctx.println("Description (Enter to skip):");
                String desc = ctx.readLine();
                if (desc.isBlank()) desc = null;
                BigDecimal price = ctx.readDecimal("Price:");
                if (price == null) return;
                try {
                    ctx.println("Added: " + ps.addProduct(name, desc, price, cats.get(catIdx).getId()));
                } catch (IllegalArgumentException e) {
                    ctx.println("Error: " + e.getMessage());
                }
            }
            case "2" -> {
                List<Product> active = ps.getAllActiveProducts();
                if (active.isEmpty()) {
                    ctx.println("No active products.");
                    return;
                }
                active.forEach(p -> ctx.println(String.format("[%d] %s — %.2f EUR", p.getId(), p.getName(), p.getPrice())));
                Long pid = ctx.readLong("Product ID to deactivate:");
                if (pid == null) return;
                try {
                    ps.deactivateProduct(pid);
                    ctx.println("Product deactivated.");
                } catch (IllegalArgumentException e) {
                    ctx.println("Error: " + e.getMessage());
                }
            }
        }
    }
}