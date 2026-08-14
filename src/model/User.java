package model;

/**
 * User Entity Model Class
 * Represents an ATM Account User.
 * Demonstrates Core Java OOP Concept: Encapsulation (private fields with public getters/setters).
 */
public class User {
    private int id;
    private String accountNumber;
    private String name;
    private String pin;
    private double balance;
    private String status; // 'ACTIVE' or 'LOCKED'

    // Default Constructor
    public User() {
    }

    // Parameterized Constructor (without ID for creation)
    public User(String accountNumber, String name, String pin, double balance, String status) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.pin = pin;
        this.balance = balance;
        this.status = status;
    }

    // Full Parameterized Constructor
    public User(int id, String accountNumber, String name, String pin, double balance, String status) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.name = name;
        this.pin = pin;
        this.balance = balance;
        this.status = status;
    }

    // Getters and Setters (Encapsulation)
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isLocked() {
        return "LOCKED".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", status='" + status + '\'' +
                '}';
    }
}
