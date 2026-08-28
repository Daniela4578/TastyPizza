package server;

import objects.User;
import services.ServiceContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

public class SessionContext {

    private final PrintWriter out;
    private final BufferedReader in;
    private final ServiceContainer services;

    private User loggedInUser = null;
    private boolean running = true;

    public SessionContext(PrintWriter out, BufferedReader in, ServiceContainer services) {
        this.out = out;
        this.in = in;
        this.services = services;
    }

    public void println(String msg) {
        out.println(msg);
    }

    public String readLine() throws IOException {
        String line = in.readLine();
        if (line == null) throw new IOException("Client disconnected");
        return line.trim();
    }

    public String readValidatedInput(String prompt, Consumer<String> validator) throws IOException {
        while (true) {
            println(prompt);
            String input = readLine();
            try {
                validator.accept(input);
                return input;
            } catch (IllegalArgumentException e) {
                println("Error: " + e.getMessage() + " Try again.");
            }
        }
    }

    public LocalDate readDate(String prompt) throws IOException {
        while (true) {
            println(prompt);
            try {
                return LocalDate.parse(readLine(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                println("Invalid date. Use YYYY-MM-DD. Try again.");
            }
        }
    }

    public LocalTime readTime(String prompt) throws IOException {
        while (true) {
            println(prompt);
            try {
                return LocalTime.parse(readLine(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e) {
                println("Invalid time. Use HH:MM. Try again.");
            }
        }
    }

    public Long readLong(String prompt) throws IOException {
        println(prompt);
        try {
            return Long.parseLong(readLine());
        } catch (NumberFormatException e) {
            println("Invalid ID.");
            return null;
        }
    }

    public BigDecimal readDecimal(String prompt) throws IOException {
        println(prompt);
        try {
            return new BigDecimal(readLine());
        } catch (NumberFormatException e) {
            println("Invalid number.");
            return null;
        }
    }

    public boolean confirm(String prompt) throws IOException {
        println(prompt + " (yes/no):");
        return "yes".equalsIgnoreCase(readLine());
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public User getUser() {
        return loggedInUser;
    }

    public void setUser(User user) {
        this.loggedInUser = user;
    }

    public void logout() {
        this.loggedInUser = null;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        this.running = false;
    }

    public ServiceContainer getServices() {
        return services;
    }
}