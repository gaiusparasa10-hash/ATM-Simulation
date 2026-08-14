package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/atm_simulation"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC";

    private static final String USER =
            System.getenv("ATM_DB_USER");

    private static final String PASSWORD =
            System.getenv("ATM_DB_PASSWORD");

    private static final String DRIVER_CLASS =
            "com.mysql.cj.jdbc.Driver";

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL JDBC Driver not found.", e);
        }

        if (USER == null || PASSWORD == null) {
            throw new SQLException(
                    "Database environment variables are not configured.");
        }

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD);
    }
}