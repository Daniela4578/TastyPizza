package server.command.customer;

import objects.Address;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class MyAddressesCommand implements Command {
    @Override
    public String label() {
        return "My addresses";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        var as = ctx.getServices().getAddressService();
        List<Address> addresses = as.getAddresses(ctx.getUser().getId());
        ctx.println("\nMY ADDRESSES:");
        if (addresses.isEmpty()) ctx.println("No addresses saved.");
        else for (int i = 0; i < addresses.size(); i++)
            ctx.println(String.format("[%d] %s", i + 1, addresses.get(i).getName()));
        ctx.println("\n1 - Add new address");
        if (!addresses.isEmpty()) ctx.println("2 - Delete address");
        ctx.println("0 - Back\nChoose:");
        switch (ctx.readLine()) {
            case "1" -> {
                ctx.println("Name (e.g. Home, Work):");
                String name = ctx.readLine();
                if (name.isBlank()) {
                    ctx.println("Cancelled.");
                    return;
                }
                try {
                    ctx.println("Latitude:");
                    double lat = Double.parseDouble(ctx.readLine());
                    ctx.println("Longitude:");
                    double lng = Double.parseDouble(ctx.readLine());
                    ctx.println("Address saved: " + as.addAddress(ctx.getUser().getId(), name, lat, lng).getName());
                } catch (NumberFormatException e) {
                    ctx.println("Invalid coordinates.");
                } catch (IllegalArgumentException e) {
                    ctx.println("Error: " + e.getMessage());
                }
            }
            case "2" -> {
                if (addresses.isEmpty()) {
                    ctx.println("No addresses to delete.");
                    return;
                }
                ctx.println("Enter address number to delete:");
                try {
                    int idx = Integer.parseInt(ctx.readLine()) - 1;
                    if (idx < 0 || idx >= addresses.size()) {
                        ctx.println("Invalid.");
                        return;
                    }
                    as.deleteAddress(addresses.get(idx).getId(), ctx.getUser().getId());
                    ctx.println("Address deleted.");
                } catch (NumberFormatException e) {
                    ctx.println("Invalid.");
                } catch (IllegalArgumentException e) {
                    ctx.println("Error: " + e.getMessage());
                }
            }
        }
    }
}