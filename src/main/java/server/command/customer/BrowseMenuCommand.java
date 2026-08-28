package server.command.customer;

import objects.*;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class BrowseMenuCommand implements Command {
    @Override
    public String label() {
        return "Browse menu";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        var ps = ctx.getServices().getProductService();
        List<Category> cats = ps.getCategories();
        if (cats.isEmpty()) {
            ctx.println("\nNo categories available.");
            return;
        }
        ctx.println("\nMENU:");
        for (int i = 0; i < cats.size(); i++) ctx.println((i + 1) + " - " + cats.get(i).getName());
        ctx.println("0 - Back\nChoose category:");
        String input = ctx.readLine();
        if ("0".equals(input)) return;
        int idx;
        try {
            idx = Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            ctx.println("Invalid.");
            return;
        }
        if (idx < 0 || idx >= cats.size()) {
            ctx.println("Invalid.");
            return;
        }
        List<Product> products = ps.getProductsByCategory(cats.get(idx).getId());
        if (products.isEmpty()) {
            ctx.println("No products in this category.");
            return;
        }
        ctx.println("\n--- " + cats.get(idx).getName().toUpperCase() + " ---");
        for (Product p : products) {
            List<ProductSize> sizes = ps.getSizesByProduct(p.getId());
            if (sizes.isEmpty())
                ctx.println(String.format("  [%d] %s - %.2f EUR", p.getId(), p.getName(), p.getPrice()));
            else {
                ctx.println(String.format("  [%d] %s", p.getId(), p.getName()));
                sizes.forEach(s -> ctx.println(String.format("       %s - %.2f EUR", s.getSizeLabel(), s.getPrice())));
            }
            if (p.getDescription() != null && !p.getDescription().isBlank())
                ctx.println("       " + p.getDescription());
        }
    }
}