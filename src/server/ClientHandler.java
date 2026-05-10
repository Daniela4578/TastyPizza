package server;

import objects.Role;
import objects.User;
import services.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final UserService userService;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket clientSocket, UserService userService) {
        this.clientSocket = clientSocket;
        this.userService = userService;
    }

    @Override
    public void run() {
        try{
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("WELCOME TO THE TASTY PIZZA!");

            boolean running = true;
            while (running) {
                printMainMenu();
                String choice = in.readLine();

                if (choice == null) break;

                switch (choice.trim()) {
                    case "1" -> handleRegistration();
                    case "2" -> handleLogin();
                    case "3" -> {
                        out.println("Goodbye!");
                        running = false;
                    }
                    default -> out.println("Invalid choice. Enter 1, 2 or 3.");
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void printMainMenu() {
        out.println("MAIN MENU:");
        out.println("1 - Registration");
        out.println("2 - Login");
        out.println("3 - Exit");
        out.println("Choose an option: ");
    }

    private void handleRegistration() throws IOException {
        out.println("\nREGISTRATION: ");

        String email = readValidatedInput("Email:", userService::validateEmail);

        String password = readValidatedInput("Password (min 6 chars):", userService::validatePassword);

        String firstName = readValidatedInput("First name:", userService::validateName);

        String lastName = readValidatedInput("Last name:", userService::validateName);

        String phone = readValidatedInput("Phone number (+359...):", userService::validatePhoneNumber);

        LocalDate dateOfBirth = readValidatedDate();

        Role role = readValidatedRole();

        try {
            User registered = userService.register(
                    email, password, firstName, lastName,
                    phone, dateOfBirth, role);
            out.println("\nAccount created successfully!");
            out.println(registered);
        } catch (IllegalArgumentException e) {
            out.println("\nRegistration failed: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            out.println("\nSystem error: " + e.getMessage());
        }
    }


    private void handleLogin() throws IOException {
        out.println("\nLOGIN: ");

        String email = readValidatedInput("Email: ", userService::validateEmail);

        String password = readValidatedInput("Password: ", userService::validatePassword);

        try {
            User user = userService.login(email, password);
            out.println("\nLogin successful!");
            out.println("Welcome back, " + user.getFullName() + "!");
            out.println(user);
        } catch (IllegalArgumentException e) {
            out.println("\nLogin failed: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            out.println("\nSystem error: " + e.getMessage());
        }
    }

    private String readInput(String prompt) throws IOException {
        out.println(prompt);
        return in.readLine();
    }

    private String readValidatedInput(String prompt, Consumer<String> validator) throws IOException {
        while (true) {
            String input = readInput(prompt);
            if (input == null) throw new IOException("Client disconnected");
            try {
                validator.accept(input);
                return input;
            } catch (IllegalArgumentException e) {
                out.println("Error: " + e.getMessage() + " Try again.");
            }
        }
    }

    private LocalDate readValidatedDate() throws IOException {
        while (true) {
            out.println("Date of birth (YYYY-MM-DD):");
            String input = in.readLine();
            if (input == null) throw new IOException("Client disconnected");
            try {
                return LocalDate.parse(input.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                out.println("Invalid date format. Use YYYY-MM-DD. Try again.");
            }
        }
    }

    private Role readValidatedRole() throws IOException {
        while (true) {
            out.println("Role:");
            out.println("  1. Customer");
            out.println("  2. Employee");
            out.println("Choose:");
            String choice = in.readLine();
            if (choice == null) throw new IOException("Client disconnected");
            switch (choice.trim()) {
                case "1" -> { return Role.CUSTOMER; }
                case "2" -> { return Role.EMPLOYEE; }
                default  -> out.println("Invalid choice. Enter 1 or 2. Try again.");
            }
        }
    }

    private void closeConnection() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (clientSocket != null)
                clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}
