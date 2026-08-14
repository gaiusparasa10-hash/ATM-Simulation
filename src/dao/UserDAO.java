package dao;

import model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) for User entity.
 * Handles database operations for user accounts using JDBC PreparedStatements.
 */
public class UserDAO {

    /**
     * Retrieves a user by their unique account number.
     * Uses PreparedStatement to prevent SQL Injection attacks.
     *
     * @param accountNumber User's account number
     * @return User object if found, null otherwise
     * @throws SQLException on database error
     */
    public User getUserByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT id, account_number, name, pin, balance, status FROM users WHERE account_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, accountNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("account_number"),
                        rs.getString("name"),
                        rs.getString("pin"),
                        rs.getDouble("balance"),
                        rs.getString("status")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Updates the account balance of a user.
     * Accepts an active Connection object to participate in JDBC transaction management.
     *
     * @param conn Active JDBC Connection
     * @param accountNumber Account number to update
     * @param newBalance New balance value
     * @return boolean true if update successful
     * @throws SQLException on database error
     */
    public boolean updateBalance(Connection conn, String accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE users SET balance = ? WHERE account_number = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, accountNumber);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Updates account status (e.g., to 'LOCKED' or 'ACTIVE').
     *
     * @param accountNumber Account number
     * @param status Target status string
     * @return boolean true if updated
     * @throws SQLException on database error
     */
    public boolean updateStatus(String accountNumber, String status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE account_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setString(2, accountNumber);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}
