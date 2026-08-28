package server.command.manager;

import exceptions.UserNotFoundException;
import objects.User;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class FireEmployeeCommand implements Command {
    @Override
    public String label() {
        return "Fire employee";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        ctx.println("\nFIRE EMPLOYEE:");
        List<User> active = ctx.getServices().getEmployeeService().getActiveEmployees();
        if (active.isEmpty()) {
            ctx.println("No active employees.");
            return;
        }
        ctx.println("ACTIVE EMPLOYEES:");
        active.forEach(u -> ctx.println(String.format("[%d] %s — %s", u.getId(), u.getFullName(), u.getEmail())));
        Long userId = ctx.readLong("Enter employee user ID:");
        if (userId == null) return;
        ctx.getServices().getEmployeeService().findUser(userId)
                .ifPresent(u -> ctx.println("You are about to fire: " + u.getFullName()));
        if (!ctx.confirm("Are you sure?")) {
            ctx.println("Cancelled.");
            return;
        }
        try {
            ctx.getServices().getEmployeeService().fireEmployee(userId);
            ctx.println("Employee terminated.");
        } catch (UserNotFoundException | IllegalArgumentException e) {
            ctx.println("Error: " + e.getMessage());
        }
    }
}