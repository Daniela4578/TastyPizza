package server.command;

import server.SessionContext;

import java.io.IOException;

public class ExitCommand implements Command {
    @Override
    public String label() {
        return "Exit";
    }

    @Override
    public void execute(SessionContext ctx) throws IOException {
        ctx.println("Goodbye!");
        ctx.stop();
    }
}