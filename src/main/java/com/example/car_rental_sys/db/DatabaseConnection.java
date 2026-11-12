package com.example.car_rental_sys.db;

import com.example.car_rental_sys.util.EnvConfig;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe Singleton JDBC Connection Manager.
 *
 * <p>Replaces the legacy SQL2TXT text file engine with raw JDBC connections to MySQL.
 * Loads configuration dynamically from {@code db.properties} on the classpath.</p>
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String CONFIG_FILE = "db.properties";

    private static volatile DatabaseConnection instance;
    private Connection connection;

    private String url;
    private String username;
    private String password;
    private String driver;

    private DatabaseConnection() {
        loadConfiguration();
        initializeDriver();
    }

    /**
     * Retrieves the singleton instance of the DatabaseConnection using double-checked locking.
     *
     * @return singleton DatabaseConnection instance
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Loads database credentials and parameters from db.properties.
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                LOGGER.log(Level.SEVERE, "Could not find {0} on classpath. Falling back to defaults.", CONFIG_FILE);
                this.driver = EnvConfig.get("DB_DRIVER", "com.mysql.cj.jdbc.Driver");
                this.url = EnvConfig.get("DB_URL", "jdbc:mysql://localhost:3306/fleet_management_db?useSSL=false&serverTimezone=UTC");
                this.username = EnvConfig.get("DB_USERNAME", "root");
                this.password = EnvConfig.get("DB_PASSWORD", "");
                return;
            }
            props.load(in);
            this.driver = EnvConfig.get("DB_DRIVER", props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
            this.url = EnvConfig.get("DB_URL", props.getProperty("db.url"));
            this.username = EnvConfig.get("DB_USERNAME", props.getProperty("db.username"));
            this.password = EnvConfig.get("DB_PASSWORD", props.getProperty("db.password"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load database configuration file: " + CONFIG_FILE, e);
            throw new RuntimeException("Database configuration error", e);
        }
    }

    /**
     * Loads the MySQL JDBC driver class.
     */
    private void initializeDriver() {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found on classpath: " + driver, e);
            throw new RuntimeException("JDBC Driver not found", e);
        }
    }

    /**
     * Returns an active SQL connection. Returns an isolated, thread-safe connection
     * for caller methods utilizing try-with-resources.
     *
     * @return active {@link Connection}
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Creates and returns a brand-new connection instance (useful for multi-threaded worker tasks).
     *
     * @return new {@link Connection}
     * @throws SQLException if connection fails
     */
    public Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Closes the active singleton connection safely.
     */
    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    LOGGER.info("Database connection closed successfully.");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            } finally {
                connection = null;
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
