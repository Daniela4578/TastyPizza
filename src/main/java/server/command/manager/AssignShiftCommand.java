package server.command.manager;

import objects.*;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AssignShiftCommand implements Command {
    @Override
    public String label() {
        return "Assign shift";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        ctx.println("\nASSIGN SHIFT:");
        List<User> active = ctx.getServices().getEmployeeService().getActiveEmployees();
        if (active.isEmpty()) {
            ctx.println("No active employees.");
            return;
        }
        ctx.println("ACTIVE EMPLOYEES:");
        active.forEach(u -> ctx.println(String.format("[%d] %s", u.getId(), u.getFullName())));
        Long employeeId = ctx.readLong("Enter employee user ID:");
        if (employeeId == null) return;
        String name = ctx.getServices().getEmployeeService().findUser(employeeId).map(User::getFullName).orElse(null);
        if (name == null) {
            ctx.println("Employee not found.");
            return;
        }
        ctx.println("Assigning shift to: " + name);
        LocalDate date = ctx.readDate("Shift date (YYYY-MM-DD):");
        LocalTime start = ctx.readTime("Start time (HH:MM):");
        LocalTime end = ctx.readTime("End time (HH:MM):");
        try {
            ctx.println("Shift assigned: " + ctx.getServices().getEmployeeService().assignShift(employeeId, name, date, start, end));
        } catch (IllegalArgumentException e) {
            ctx.println("Error: " + e.getMessage());
        }
    }
}