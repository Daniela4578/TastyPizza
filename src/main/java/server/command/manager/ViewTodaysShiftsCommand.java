package server.command.manager;

import objects.Shift;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class ViewTodaysShiftsCommand implements Command {
    @Override
    public String label() {
        return "Today's shifts";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        List<Shift> shifts = ctx.getServices().getEmployeeService().getTodaysShifts();
        if (shifts.isEmpty()) {
            ctx.println("\nNo shifts today.");
            return;
        }
        ctx.println("\nTODAY'S SHIFTS:");
        shifts.forEach(s -> ctx.println(s.toString()));
    }
}