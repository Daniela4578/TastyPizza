package server.command;

import exceptions.EmailAlreadyExistsException;
import objects.AccountStatus;
import objects.Role;
import objects.User;
import server.SessionContext;

import java.io.IOException;
import java.time.LocalDate;

public class RegisterCommand implements Command {
    @Override
    public String label() {
        return "Register";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        ctx.println("\nREGISTRATION:");
        var us = ctx.getServices().getUserService();

        String email = ctx.readValidatedInput("Email:", us::validateEmail);
        String password = ctx.readValidatedInput("Password (min 6 chars):", us::validatePassword);
        String firstName = ctx.readValidatedInput("First name:", us::validateName);
        String lastName = ctx.readValidatedInput("Last name:", us::validateName);
        String phone = ctx.readValidatedInput("Phone number:", us::validatePhoneNumber);
        LocalDate dob = ctx.readDate("Date of birth (YYYY-MM-DD):");
        Role role = readRole(ctx);

        while (true) {
            try {
                us.validateAge(role, dob);
                break;
            } catch (IllegalArgumentException e) {
                ctx.println("Error: " + e.getMessage());
                dob = ctx.readDate("Date of birth (YYYY-MM-DD):");
            }
        }

        while (true) {
            try {
                User user = us.register(email, password, firstName, lastName, phone, dob, role);
                if (user.getStatus() == AccountStatus.PENDING)
                    ctx.println("\nRegistration submitted! Your account is pending manager approval.");
                else {
                    ctx.println("\nAccount created successfully!");
                    ctx.println(user.toString());
                }
                return;
            } catch (EmailAlreadyExistsException e) {
                ctx.println("Error: " + e.getMessage());
                email = ctx.readValidatedInput("Enter a different email:", us::validateEmail);
            } catch (IllegalArgumentException e) {
                ctx.println("\nRegistration failed: " + e.getMessage());
                return;
            }
        }
    }

    private Role readRole(SessionContext ctx) throws IOException {
        while (true) {
            ctx.println("Role:\n  1. Customer\n  2. Employee\nChoose:");
            String c = ctx.readLine();
            if ("1".equals(c)) return Role.CUSTOMER;
            if ("2".equals(c)) return Role.EMPLOYEE;
            ctx.println("Invalid. Enter 1 or 2.");
        }
    }
}