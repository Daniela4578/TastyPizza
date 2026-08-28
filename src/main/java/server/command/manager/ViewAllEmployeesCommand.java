package server.command.manager;

import objects.User;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.util.List;

public class ViewAllEmployeesCommand implements Command {
    @Override
    public String label() {
        return "View all employees";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        List<User> active = ctx.getServices().getEmployeeService().getActiveEmployees();
        if (active.isEmpty()) {
            ctx.println("\nNo active employees.");
            return;
        }
        ctx.println("\nALL ACTIVE EMPLOYEES:");
        for (User u : active) {
            ctx.getServices().getEmployeeService().getEmployeeDetails(u.getId()).ifPresentOrElse(
                    d -> ctx.println(String.format("[%d] %s | Salary: %.2f EUR | Hired: %s", u.getId(), u.getFullName(), d.getSalary(), d.getHireDate())),
                    () -> ctx.println(String.format("[%d] %s | No details on file", u.getId(), u.getFullName())));
        }
    }
}