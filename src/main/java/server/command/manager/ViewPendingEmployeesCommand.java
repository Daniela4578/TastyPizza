package server.command.manager;

import objects.User;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class ViewPendingEmployeesCommand implements Command {
    @Override
    public String label() {
        return "View pending employees";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        List<User> pending = ctx.getServices().getEmployeeService().getPendingEmployees();
        if (pending.isEmpty()) {
            ctx.println("\nNo pending employees.");
            return;
        }
        ctx.println("\nPENDING EMPLOYEES:");
        pending.forEach(u -> ctx.println(String.format("[%d] %s — %s", u.getId(), u.getFullName(), u.getEmail())));
    }
}