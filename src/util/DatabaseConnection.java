package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Utility Class
 * Responsible for establishing JDBC connections to the MySQL database.
 * Supports environment variables ATM_DB_URL, ATM_DB_USER, and ATM_DB_PASSWORD
 * to prevent hardcoding sensitive database credentials.
 */
public class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/atm_simulation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    /**
     * Obtains a new JDBC Connection to MySQL database.
     * Uses environment variables if set, otherwise falls back to defaults.
     *
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found in classpath!");
            System.err.println("   Please ensure 'mysql-connector-j.jar' is added to your build path/classpath.");
            throw new SQLException("Driver class not found: " + e.getMessage(), e);
        }

        String url = System.getenv("ATM_DB_URL") != null && !System.getenv("ATM_DB_URL").trim().isEmpty()
                ? System.getenv("ATM_DB_URL").trim() : DEFAULT_URL;
        String user = System.getenv("ATM_DB_USER") != null && !System.getenv("ATM_DB_USER").trim().isEmpty()
                ? System.getenv("ATM_DB_USER").trim() : DEFAULT_USER;
        String password = System.getenv("ATM_DB_PASSWORD") != null
                ? System.getenv("ATM_DB_PASSWORD") : DEFAULT_PASSWORD;

        return DriverManager.getConnection(url, user, password);
    }
}