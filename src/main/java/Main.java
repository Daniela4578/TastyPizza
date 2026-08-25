import config.AppConfig;
import db.DatabaseConnection;
import server.Server;
import services.ServiceContainer;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Start connection pool
            DatabaseConnection.init();

            // 2. Wire all dependencies
            ServiceContainer services = AppConfig.createServices();

            // 3. Start server
            new Server(services).start();

        } catch (IllegalStateException e) {
            System.err.println("Configuration error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to start: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.shutdown();
        }
    }
}