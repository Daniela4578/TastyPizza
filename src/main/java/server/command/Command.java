package server.command;

import server.SessionContext;
import java.io.IOException;


public interface Command {
    String label();
    void execute(SessionContext ctx) throws IOException;
}