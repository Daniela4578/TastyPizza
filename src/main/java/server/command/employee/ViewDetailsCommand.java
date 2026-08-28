package server.command.employee;

import server.SessionContext;
import server.command.Command;

import java.io.IOException;

public class ViewDetailsCommand implements Command {
    @Override
    public String label() {
        return "My details";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        ctx.getServices().getEmployeeService().getEmployeeDetails(ctx.getUser().getId())
                .ifPresentOrElse(d -> ctx.println("\n" + d), () -> ctx.println("\nNo details on file yet."));
    }
}