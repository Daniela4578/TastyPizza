package server.command.manager;

import objects.*;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class RemoveShiftCommand implements Command {
    @Override
    public String label() {
        return "Remove shift";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        ctx.println("\nREMOVE SHIFT:");
        List<User> active = ctx.getServices().getEmployeeService().getActiveEmployees();
        if (active.isEmpty()) {
            ctx.println("No active employees.");
            return;
        }
        active.forEach(u -> ctx.println(String.format("[%d] %s", u.getId(), u.getFullName())));
        Long empId = ctx.readLong("Enter employee user ID:");
        if (empId == null) return;
        List<Shift> shifts = ctx.getServices().getEmployeeService().getShiftsForEmployee(empId);
        if (shifts.isEmpty()) {
            ctx.println("No shifts found.");
            return;
        }
        shifts.forEach(s -> ctx.println(String.format("[%d] %s", s.getId(), s)));
        Long shiftId = ctx.readLong("Enter shift ID to remove:");
        if (shiftId == null) return;
        try {
            ctx.getServices().getEmployeeService().removeShift(shiftId);
            ctx.println("Shift removed.");
        } catch (IllegalArgumentException e) {
            ctx.println("Error: " + e.getMessage());
        }
    }
}