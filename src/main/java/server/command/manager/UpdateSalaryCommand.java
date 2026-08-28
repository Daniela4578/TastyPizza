package server.command.manager;

import exceptions.UserNotFoundException;
import objects.User;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class UpdateSalaryCommand implements Command {
    @Override
    public String label() {
        return "Update salary";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        ctx.println("\nUPDATE SALARY:");
        List<User> active = ctx.getServices().getEmployeeService().getActiveEmployees();
        if (active.isEmpty()) {
            ctx.println("No active employees.");
            return;
        }
        active.forEach(u -> ctx.println(String.format("[%d] %s", u.getId(), u.getFullName())));
        Long userId = ctx.readLong("Enter employee user ID:");
        if (userId == null) return;
        ctx.getServices().getEmployeeService().getEmployeeDetails(userId)
                .ifPresent(d -> ctx.println(String.format("Current salary: %.2f EUR", d.getSalary())));
        BigDecimal salary = ctx.readDecimal("New salary:");
        if (salary == null) return;
        try {
            ctx.getServices().getEmployeeService().updateSalary(userId, salary);
            ctx.println(String.format("Salary updated to %.2f EUR.", salary));
        } catch (UserNotFoundException | IllegalArgumentException e) {
            ctx.println("Error: " + e.getMessage());
        }
    }
}