package db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private static HikariDataSource dataSource;

    private DatabaseConnection() {
        throw new AssertionError("Use DatabaseConnection.getConnection()");
    }

    public static synchronized void init() {
        if (dataSource != null) return;

        Properties config = loadConfig();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getProperty("db.url"));
        hikariConfig.setUsername(config.getProperty("db.user"));
        hikariConfig.setPassword(config.getProperty("db.password"));
        hikariConfig.setMaximumPoolSize(80);
        hikariConfig.setMinimumIdle(10);
        hikariConfig.setConnectionTimeout(30_000);
        hikariConfig.setIdleTimeout(600_000);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName("TastyPizzaPool");

        dataSource = new HikariDataSource(hikariConfig);
        System.out.println("Connection pool ready: max=" + hikariConfig.getMaximumPoolSize());
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null)
            throw new IllegalStateException("Call DatabaseConnection.init() first.");
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Connection pool shut down.");
        }
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("local.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "local.properties not found in project root. " +
                            "Create it with:\n" +
                            "  db.url=jdbc:mysql://localhost:3306/tastypizza\n" +
                            "  db.user=root\n" +
                            "  db.password=YOUR_PASSWORD");
        }
        validateConfig(props);
        return props;
    }

    /**
     * Fails fast if any required property is missing.
     */
    private static void validateConfig(Properties props) {
        for (String key : new String[]{"db.url", "db.user", "db.password"}) {
            if (props.getProperty(key) == null || props.getProperty(key).isBlank()) {
                throw new IllegalStateException(
                        "Missing property '" + key + "' in local.properties");
            }
        }
    }
}