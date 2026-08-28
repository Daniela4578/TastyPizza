package server;

import server.command.Command;
import server.command.ExitCommand;
import server.command.LogInCommand;
import server.command.LogOutCommand;
import server.command.RegisterCommand;
import server.command.customer.*;
import server.command.employee.*;
import server.command.manager.*;
import services.ServiceContainer;

import java.io.*;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;


public class ClientHandler implements Runnable {

    private static final Map<String, Command> MAIN_COMMANDS = buildMainCommands();
    private static final Map<String, Command> CUSTOMER_COMMANDS = buildCustomerCommands();
    private static final Map<String, Command> EMPLOYEE_COMMANDS = buildEmployeeCommands();
    private static final Map<String, Command> MANAGER_COMMANDS = buildManagerCommands();

    private final Socket clientSocket;
    private final ServiceContainer services;

    public ClientHandler(Socket clientSocket, ServiceContainer services) {
        this.clientSocket = clientSocket;
        this.services = services;
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            out.println("================================");
            out.println("   WELCOME TO TASTY PIZZA!     ");
            out.println("================================");

            SessionContext ctx = new SessionContext(out, in, services);

            while (ctx.isRunning()) {
                Map<String, Command> commands = currentCommands(ctx);
                printMenu(ctx, commands);

                String choice;
                try {
                    choice = ctx.readLine();
                } catch (IOException e) {
                    break;
                }

                Command cmd = commands.get(choice);
                if (cmd != null) {
                    try {
                        cmd.execute(ctx);
                    } catch (IOException e) {
                        break;
                    } catch (RuntimeException e) {
                        System.err.println("Error in " + cmd.getClass().getSimpleName() + ": " + e.getMessage());
                        out.println("An unexpected error occurred. Please try again.");
                    }
                } else {
                    out.println("Invalid choice.");
                }
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static Map<String, Command> currentCommands(SessionContext ctx) {
        if (!ctx.isLoggedIn()) return MAIN_COMMANDS;
        return switch (ctx.getUser().getRole()) {
            case CUSTOMER -> CUSTOMER_COMMANDS;
            case EMPLOYEE -> EMPLOYEE_COMMANDS;
            case MANAGER -> MANAGER_COMMANDS;
        };
    }

    private static void printMenu(SessionContext ctx, Map<String, Command> commands) {
        ctx.println("");
        commands.forEach((key, cmd) -> ctx.println(key + " - " + cmd.label()));
        ctx.println("Choose:");
    }

    private static Map<String, Command> buildMainCommands() {
        Map<String, Command> m = new LinkedHashMap<>();
        m.put("1", new RegisterCommand());
        m.put("2", new LogInCommand());
        m.put("3", new ExitCommand());
        return m;
    }

    private static Map<String, Command> buildCustomerCommands() {
        Map<String, Command> m = new LinkedHashMap<>();
        m.put("1", new BrowseMenuCommand());
        m.put("2", new PlaceOrderCommand());
        m.put("3", new MyOrdersCommand());
        m.put("4", new MyAddressesCommand());
        m.put("5", new DeleteAccountCommand());
        m.put("6", new LogOutCommand());
        return m;
    }

    private static Map<String, Command> buildEmployeeCommands() {
        Map<String, Command> m = new LinkedHashMap<>();
        m.put("1", new ViewShiftsCommand());
        m.put("2", new ViewDetailsCommand());
        m.put("3", new ViewPendingOrdersCommand());
        m.put("4", new ProcessOrderCommand());
        m.put("5", new DeliverOrderCommand());
        m.put("6", new ViewLowStockCommand());
        m.put("7", new LogOutCommand());
        return m;
    }

    private static Map<String, Command> buildManagerCommands() {
        Map<String, Command> m = new LinkedHashMap<>();
        m.put("1", new ViewPendingEmployeesCommand());
        m.put("2", new ApproveEmployeeCommand());
        m.put("3", new FireEmployeeCommand());
        m.put("4", new ViewAllEmployeesCommand());
        m.put("5", new AssignShiftCommand());
        m.put("6", new RemoveShiftCommand());
        m.put("7", new ViewTodaysShiftsCommand());
        m.put("8", new UpdateSalaryCommand());
        m.put("9", new ManageProductsCommand());
        m.put("10", new ManageStockCommand());
        m.put("11", new ViewAllOrdersCommand());
        m.put("12", new LogOutCommand());
        return m;
    }
}