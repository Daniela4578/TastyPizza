import db.DatabaseConnection;
import repositories.JdbcUserRepository;
import repositories.UserRepository;
import server.Server;
import services.UserService;

public class Main {
    public static void main(String[] args) {
        try{
            DatabaseConnection db = DatabaseConnection.getInstance();
            UserRepository userRepository = new JdbcUserRepository(db);
            UserService userService = new UserService(userRepository);
            Server server = new Server(userService);
            server.start();
        } catch (Exception e) {
            System.out.println("Failed to start server: " + e.getMessage());
        }
    }
}
