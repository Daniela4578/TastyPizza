package server.command.manager;

import exceptions.UserNotFoundException;
import objects.EmployeeDetails;
import server.SessionContext;
import server.command.Command;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ApproveEmployeeCommand implements Command {
    @Override
    public String label() {
        return "Approve employee";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        ctx.println("\nAPPROVE EMPLOYEE:");
        new ViewPendingEmployeesCommand().execute(ctx);
        Long userId = ctx.readLong("Enter employee user ID:");
        if (userId == null) return;
        BigDecimal salary = ctx.readDecimal("Enter salary:");
        if (salary == null) return;
        LocalDate hireDate = ctx.readDate("Hire date (YYYY-MM-DD):");
        try {
            EmployeeDetails d = ctx.getServices().getEmployeeService().approveEmployee(userId, salary, hireDate);
            ctx.println("Employee approved! " + d);
        } catch (UserNotFoundException | IllegalArgumentException e) {
            ctx.println("Error: " + e.getMessage());
        }
    }
}