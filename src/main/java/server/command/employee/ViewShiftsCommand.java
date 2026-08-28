package server.command.employee;

import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

import objects.Shift;

public class ViewShiftsCommand implements Command {
    @Override
    public String label() {
        return "My shifts";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        List<Shift> shifts = ctx.getServices().getEmployeeService().getShiftsForEmployee(ctx.getUser().getId());
        if (shifts.isEmpty()) {
            ctx.println("\nNo shifts assigned yet.");
            return;
        }
        ctx.println("\nMY SHIFTS:");
        shifts.forEach(s -> ctx.println(s.toString()));
    }
}