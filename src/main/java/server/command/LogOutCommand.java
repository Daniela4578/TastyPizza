package server.command;

import server.SessionContext;

import java.io.IOException;

public class LogOutCommand implements Command {
    @Override
    public String label() {
        return "Logout";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        ctx.println("Logged out.");
        ctx.logout();
    }
}