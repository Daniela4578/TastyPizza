package server.command.customer;

import exceptions.InsufficientStockException;
import objects.*;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PlaceOrderCommand implements Command {
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("2.99");

    @Override
    public String label() {
        return "Place order";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        var as = ctx.getServices().getAddressService();
        var ps = ctx.getServices().getProductService();
        User customer = ctx.getUser();

        List<Address> addresses = as.getAddresses(customer.getId());
        if (addresses.isEmpty()) {
            ctx.println("\nYou need a delivery address first.");
            Address addr = addAddress(ctx, customer);
            if (addr == null) return;
            addresses = as.getAddresses(customer.getId());
        }

        List<OrderItem> cart = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        while (true) {
            if (!cart.isEmpty())
                ctx.println(String.format("\nCart: %d item(s) | %.2f EUR + %.2f delivery = %.2f EUR total",
                        cart.size(), total, DELIVERY_FEE, total.add(DELIVERY_FEE)));

            List<Category> cats = ps.getCategories();
            ctx.println("\nADD ITEM — choose category (0 to checkout):");
            for (int i = 0; i < cats.size(); i++) ctx.println((i + 1) + " - " + cats.get(i).getName());
            ctx.println("0 - Checkout\nChoose:");

            String catIn = ctx.readLine();
            if ("0".equals(catIn)) break;
            int catIdx;
            try {
                catIdx = Integer.parseInt(catIn) - 1;
            } catch (NumberFormatException e) {
                ctx.println("Invalid.");
                continue;
            }
            if (catIdx < 0 || catIdx >= cats.size()) {
                ctx.println("Invalid.");
                continue;
            }

            List<Product> products = ps.getProductsByCategory(cats.get(catIdx).getId());
            if (products.isEmpty()) {
                ctx.println("No products in this category.");
                continue;
            }
            ctx.println("\nProducts:");
            for (int i = 0; i < products.size(); i++)
                ctx.println((i + 1) + " - " + products.get(i).getName() + " - " + products.get(i).getPrice() + " EUR");
            ctx.println("0 - Back\nChoose:");

            String prodIn = ctx.readLine();
            if ("0".equals(prodIn)) continue;
            int prodIdx;
            try {
                prodIdx = Integer.parseInt(prodIn) - 1;
            } catch (NumberFormatException e) {
                ctx.println("Invalid.");
                continue;
            }
            if (prodIdx < 0 || prodIdx >= products.size()) {
                ctx.println("Invalid.");
                continue;
            }

            Product product = products.get(prodIdx);
            List<ProductSize> sizes = ps.getSizesByProduct(product.getId());
            Long sizeId = null;
            String sizeName = null;
            BigDecimal unitPrice = product.getPrice();

            if (!sizes.isEmpty()) {
                ctx.println("\nChoose size:");
                for (int i = 0; i < sizes.size(); i++)
                    ctx.println((i + 1) + " - " + sizes.get(i).getSizeLabel() + " - " + sizes.get(i).getPrice() + " EUR");
                ctx.println("Choose:");
                String sizeIn = ctx.readLine();
                int sizeIdx;
                try {
                    sizeIdx = Integer.parseInt(sizeIn) - 1;
                } catch (NumberFormatException e) {
                    ctx.println("Invalid.");
                    continue;
                }
                if (sizeIdx < 0 || sizeIdx >= sizes.size()) {
                    ctx.println("Invalid.");
                    continue;
                }
                sizeId = sizes.get(sizeIdx).getId();
                sizeName = sizes.get(sizeIdx).getSizeLabel();
                unitPrice = sizes.get(sizeIdx).getPrice();
            }

            ctx.println("Quantity:");
            int qty;
            try {
                qty = Integer.parseInt(ctx.readLine());
                if (qty <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                ctx.println("Invalid quantity.");
                continue;
            }

            ctx.println("Special instructions (Enter to skip):");
            String instr = ctx.readLine();
            if (instr.isBlank()) instr = null;

            OrderItem item = OrderItem.builder().productId(product.getId()).productName(product.getName())
                    .productSizeId(sizeId).sizeName(sizeName).quantity(qty).unitPrice(unitPrice)
                    .specialInstructions(instr).build();
            cart.add(item);
            total = total.add(item.getSubtotal());
            ctx.println(String.format("Added. Cart total: %.2f EUR", total));
        }

        if (cart.isEmpty()) {
            ctx.println("Cart is empty. Order cancelled.");
            return;
        }

        ctx.println("\nYOUR ORDER:");
        cart.forEach(i -> ctx.println(i.toString()));
        ctx.println(String.format("Items:    %.2f EUR\nDelivery: %.2f EUR\nTOTAL:    %.2f EUR",
                total, DELIVERY_FEE, total.add(DELIVERY_FEE)));

        ctx.println("\nDELIVERY ADDRESS:");
        for (int i = 0; i < addresses.size(); i++) ctx.println((i + 1) + " - " + addresses.get(i).getName());
        ctx.println((addresses.size() + 1) + " - Add new address\nChoose:");
        String addrIn = ctx.readLine();
        int addrIdx;
        try {
            addrIdx = Integer.parseInt(addrIn) - 1;
        } catch (NumberFormatException e) {
            ctx.println("Invalid. Order cancelled.");
            return;
        }
        Long addressId;
        if (addrIdx == addresses.size()) {
            Address newAddr = addAddress(ctx, customer);
            if (newAddr == null) {
                ctx.println("Order cancelled.");
                return;
            }
            addressId = newAddr.getId();
        } else if (addrIdx >= 0 && addrIdx < addresses.size()) {
            addressId = addresses.get(addrIdx).getId();
        } else {
            ctx.println("Invalid. Order cancelled.");
            return;
        }

        ctx.println("\nPAYMENT METHOD:\n1 - Cash on delivery\n2 - Card\nChoose:");
        PaymentMethod method = "2".equals(ctx.readLine()) ? PaymentMethod.CARD : PaymentMethod.CASH;
        ctx.println("Payment: " + (method == PaymentMethod.CARD ? "Card" : "Cash on delivery"));

        if (!ctx.confirm("\nConfirm order?")) {
            ctx.println("Order cancelled.");
            return;
        }

        try {
            Order order = ctx.getServices().getOrderService().placeOrder(customer.getId(), addressId, cart, method);
            ctx.println("\nOrder placed! Order #" + order.getId());
            ctx.println("Payment: " + (method == PaymentMethod.CARD ? "Card" : "Cash on delivery") + " | PENDING");
        } catch (InsufficientStockException e) {
            ctx.println("\nOrder failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.println("\nOrder failed: " + e.getMessage());
        }
    }

    private Address addAddress(SessionContext ctx, User customer) throws IOException {
        ctx.println("\nADD ADDRESS:\nName (e.g. Home, Work):");
        String name = ctx.readLine();
        if (name.isBlank()) {
            ctx.println("Cancelled.");
            return null;
        }
        try {
            ctx.println("Latitude:");
            double lat = Double.parseDouble(ctx.readLine());
            ctx.println("Longitude:");
            double lng = Double.parseDouble(ctx.readLine());
            Address addr = ctx.getServices().getAddressService().addAddress(customer.getId(), name, lat, lng);
            ctx.println("Address saved: " + addr.getName());
            return addr;
        } catch (NumberFormatException e) {
            ctx.println("Invalid coordinates. Cancelled.");
            return null;
        } catch (IllegalArgumentException e) {
            ctx.println("Error: " + e.getMessage());
            return null;
        }
    }
}