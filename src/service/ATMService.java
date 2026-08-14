package service;

import dao.TransactionDAO;
import dao.UserDAO;
import model.Transaction;
import model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * ATM Business Service Layer.
 * Handles core banking operations including PIN authentication, balance check,
 * deposits, withdrawals with JDBC transaction management, and mini statement processing.
 */
public class ATMService {

    private final UserDAO userDAO;
    private final TransactionDAO transactionDAO;

    // Withdrawal limits
    public static final double MAX_WITHDRAWAL_LIMIT = 20000.00;
    public static final int MAX_PIN_ATTEMPTS = 3;

    public ATMService() {
        this.userDAO = new UserDAO();
        this.transactionDAO = new TransactionDAO();
    }

    /**
     * Authenticates account number and verifies user status.
     *
     * @param accountNumber Account number to verify
     * @return User object if account exists and is ACTIVE, null otherwise
     * @throws SQLException on database error
     */
    public User fetchUserForLogin(String accountNumber) throws SQLException {
        return userDAO.getUserByAccountNumber(accountNumber);
    }

    /**
     * Locks account status in MySQL database when 3 failed PIN attempts occur.
     *
     * @param accountNumber Account number to lock
     * @throws SQLException on database error
     */
    public void lockAccount(String accountNumber) throws SQLException {
        userDAO.updateStatus(accountNumber, "LOCKED");
    }

    /**
     * Retrieves current balance directly from MySQL database for real-time accuracy.
     *
     * @param accountNumber Account number
     * @return Current balance
     * @throws SQLException on database error
     */
    public double checkBalance(String accountNumber) throws SQLException {
        User user = userDAO.getUserByAccountNumber(accountNumber);
        if (user != null) {
            return user.getBalance();
        }
        throw new SQLException("Account not found for account number: " + accountNumber);
    }

    /**
     * Performs Withdrawal Operation using Atomic JDBC Database Transactions.
     *
     * Workflow:
     * 1. Begin JDBC Transaction (setAutoCommit(false))
     * 2. Deduct amount from account balance
     * 3. Update balance in 'users' table
     * 4. Record entry in 'transactions' table
     * 5. Commit transaction (commit())
     * 6. On any failure, execute Rollback (rollback())
     *
     * @param user Active User object
     * @param amount Amount to withdraw
     * @return boolean true if transaction succeeded
     * @throws SQLException on database error or transaction failure
     */
    public boolean withdraw(User user, double amount) throws SQLException {
        if (amount <= 0) {
            System.err.println("❌ Invalid withdrawal amount. Amount must be greater than zero.");
            return false;
        }

        if (amount > MAX_WITHDRAWAL_LIMIT) {
            System.out.printf("❌ Withdrawal limit exceeded. Maximum allowed per transaction is ₹%,.2f%n", MAX_WITHDRAWAL_LIMIT);
            return false;
        }

        // Check real-time balance from DB
        double currentBalance = checkBalance(user.getAccountNumber());
        if (amount > currentBalance) {
            System.out.printf("❌ Insufficient balance! Available balance: ₹%,.2f%n", currentBalance);
            return false;
        }

        double newBalance = currentBalance - amount;
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            // STEP 1: Turn off auto-commit to start transaction block
            conn.setAutoCommit(false);

            // STEP 2: Update balance in 'users' table
            boolean isBalanceUpdated = userDAO.updateBalance(conn, user.getAccountNumber(), newBalance);

            // STEP 3: Record transaction in 'transactions' table
            Transaction transaction = new Transaction(user.getAccountNumber(), "WITHDRAWAL", amount, newBalance);
            boolean isTxRecorded = transactionDAO.recordTransaction(conn, transaction);

            // STEP 4: Commit if both steps succeeded
            if (isBalanceUpdated && isTxRecorded) {
                conn.commit(); // Save changes permanently
                user.setBalance(newBalance); // Update local object state
                return true;
            } else {
                conn.rollback(); // Undo changes if any step failed
                return false;
            }

        } catch (SQLException e) {
            // STEP 5: Rollback on exception
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("⚠️ Transaction rolled back due to error: " + e.getMessage());
                } catch (SQLException ex) {
                    System.err.println("Error during transaction rollback: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            // STEP 6: Reset auto-commit and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error resetting connection auto-commit: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Performs Deposit Operation using Atomic JDBC Database Transactions.
     *
     * Workflow:
     * 1. Begin JDBC Transaction (setAutoCommit(false))
     * 2. Add amount to account balance
     * 3. Update balance in 'users' table
     * 4. Record entry in 'transactions' table
     * 5. Commit transaction (commit())
     * 6. On any failure, execute Rollback (rollback())
     *
     * @param user Active User object
     * @param amount Amount to deposit
     * @return boolean true if transaction succeeded
     * @throws SQLException on database error or transaction failure
     */
    public boolean deposit(User user, double amount) throws SQLException {
        if (amount <= 0) {
            System.err.println("❌ Invalid deposit amount. Amount must be greater than zero.");
            return false;
        }

        double currentBalance = checkBalance(user.getAccountNumber());
        double newBalance = currentBalance + amount;
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            // STEP 1: Start Transaction Block
            conn.setAutoCommit(false);

            // STEP 2: Update balance in DB
            boolean isBalanceUpdated = userDAO.updateBalance(conn, user.getAccountNumber(), newBalance);

            // STEP 3: Insert transaction record in DB
            Transaction transaction = new Transaction(user.getAccountNumber(), "DEPOSIT", amount, newBalance);
            boolean isTxRecorded = transactionDAO.recordTransaction(conn, transaction);

            // STEP 4: Commit changes
            if (isBalanceUpdated && isTxRecorded) {
                conn.commit();
                user.setBalance(newBalance);
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("⚠️ Transaction rolled back due to error: " + e.getMessage());
                } catch (SQLException ex) {
                    System.err.println("Error during transaction rollback: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error resetting connection auto-commit: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Fetches recent transactions for mini statement output.
     *
     * @param accountNumber Account number
     * @param limit Number of transactions (e.g. 5)
     * @return List of recent Transaction records
     * @throws SQLException on database error
     */
    public List<Transaction> getMiniStatement(String accountNumber, int limit) throws SQLException {
        return transactionDAO.getRecentTransactions(accountNumber, limit);
    }
}
