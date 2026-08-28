package server.command;

import exceptions.AccountNotActiveException;
import objects.User;
import server.SessionContext;

import java.io.IOException;

public class LogInCommand implements Command {
    @Override
    public String label() {
        return "Login";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        ctx.println("\nLOGIN:");
        var us = ctx.getServices().getUserService();
        String email = ctx.readValidatedInput("Email:", us::validateEmail);
        String password = ctx.readValidatedInput("Password:", us::validatePassword);
        try {
            User user = us.login(email, password);
            ctx.setUser(user);
            ctx.println("\nLogin successful! Welcome, " + user.getFullName() + "!");
        } catch (AccountNotActiveException e) {
            ctx.println("\n" + e.getMessage()); // specific message per status
        } catch (IllegalArgumentException e) {
            ctx.println("\nLogin failed: " + e.getMessage());
        }
    }
}