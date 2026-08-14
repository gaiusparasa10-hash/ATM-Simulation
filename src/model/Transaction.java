package model;

import java.sql.Timestamp;

/**
 * Transaction Entity Model Class
 * Represents a single financial transaction record (Deposit or Withdrawal).
 * Demonstrates Core Java OOP Concept: Encapsulation.
 */
public class Transaction {
    private int id;
    private String accountNumber;
    private String transactionType; // 'DEPOSIT' or 'WITHDRAWAL'
    private double amount;
    private double balanceAfterTransaction;
    private Timestamp transactionDate;

    // Default Constructor
    public Transaction() {
    }

    // Constructor for recording new transaction
    public Transaction(String accountNumber, String transactionType, double amount, double balanceAfterTransaction) {
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    // Full Parameterized Constructor
    public Transaction(int id, String accountNumber, String transactionType, double amount, double balanceAfterTransaction, Timestamp transactionDate) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.transactionDate = transactionDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public void setBalanceAfterTransaction(double balanceAfterTransaction) {
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", amount=" + amount +
                ", balanceAfterTransaction=" + balanceAfterTransaction +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
