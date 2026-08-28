package server.command.customer;

import server.SessionContext;
import server.command.Command;

import java.io.IOException;

public class DeleteAccountCommand implements Command {
    @Override
    public String label() {
        return "Delete my account";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        if (!ctx.confirm("\nAre you sure? This cannot be undone.")) {
            ctx.println("Cancelled.");
            return;
        }
        ctx.getServices().getUserService().deactivateAccount(ctx.getUser().getId());
        ctx.println("Account deactivated. Goodbye!");
        ctx.logout();
    }
}