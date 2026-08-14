package dao;

import model.Transaction;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Transaction entity.
 * Handles insertion and retrieval of financial audit transactions using JDBC PreparedStatements.
 */
public class TransactionDAO {

    /**
     * Records a new transaction into the database.
     * Accepts an active Connection object to support atomic JDBC transactions.
     *
     * @param conn Active JDBC Connection
     * @param transaction Transaction model containing transaction details
     * @return boolean true if inserted successfully
     * @throws SQLException on database error
     */
    public boolean recordTransaction(Connection conn, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (account_number, transaction_type, amount, balance_after_transaction) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, transaction.getAccountNumber());
            stmt.setString(2, transaction.getTransactionType());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setDouble(4, transaction.getBalanceAfterTransaction());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Retrieves recent transactions for a given account.
     * Demonstrates Core Java Collections (List<Transaction>).
     *
     * @param accountNumber User's account number
     * @param limit Number of recent records to fetch (e.g., 5 for mini statement)
     * @return List of Transaction objects
     * @throws SQLException on database error
     */
    public List<Transaction> getRecentTransactions(String accountNumber, int limit) throws SQLException {
        List<Transaction> transactionList = new ArrayList<>();
        String sql = "SELECT id, account_number, transaction_type, amount, balance_after_transaction, transaction_date " +
                     "FROM transactions WHERE account_number = ? ORDER BY transaction_date DESC, id DESC LIMIT ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, accountNumber);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction(
                        rs.getInt("id"),
                        rs.getString("account_number"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getDouble("balance_after_transaction"),
                        rs.getTimestamp("transaction_date")
                    );
                    transactionList.add(tx);
                }
            }
        }
        return transactionList;
    }
}
