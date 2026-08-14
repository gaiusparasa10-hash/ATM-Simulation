import api.ATMHttpServer;
import model.Transaction;
import model.User;
import service.ATMService;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

/**
 * Main Application Launcher.
 * Allows users to choose between launching the Web ATM Server (HTML/CSS/JS Frontend)
 * or the original Console-based ATM Interface.
 */
public class Main {

    private static final ATMService atmService = new ATMService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public static void main(String[] args) {
        printHeader("ATM SIMULATION SYSTEM");
        System.out.println("Select Application Interface Mode:");
        System.out.println("1. Launch Web ATM Interface (HTML/CSS/JS Frontend)");
        System.out.println("2. Launch Console ATM Interface (CLI)");
        System.out.println("========================================");
        System.out.print("Enter mode choice (1 or 2): ");

        String choiceInput = scanner.nextLine().trim();

        if ("1".equals(choiceInput)) {
            System.out.println("\nStarting Web ATM Server...");
            ATMHttpServer.startServer();
        } else {
            System.out.println("\nStarting Console ATM Interface...\n");
            runConsoleMode();
        }
    }

    private static void runConsoleMode() {
        User currentUser = authenticateUser();

        if (currentUser != null) {
            runATMMenu(currentUser);
        }

        System.out.println("\nThank you for using the ATM Simulation System. Have a great day!");
        scanner.close();
    }

    /**
     * Handles account number entry and PIN verification with a maximum of 3 attempts.
     */
    private static User authenticateUser() {
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine().trim();

        if (accountNumber.isEmpty()) {
            System.err.println("❌ Account number cannot be empty.");
            return null;
        }

        User user = null;
        try {
            user = atmService.fetchUserForLogin(accountNumber);
        } catch (SQLException e) {
            System.err.println("❌ Database connection error: " + e.getMessage());
            System.err.println("   Please check if MySQL server is running and credentials in DatabaseConnection.java or ATM_DB_USER / ATM_DB_PASSWORD are correct.");
            return null;
        }

        if (user == null) {
            System.err.println("❌ Account not found. Please verify your account number.");
            return null;
        }

        if (user.isLocked()) {
            System.err.println("\n🔒 ACCOUNT LOCKED!");
            System.err.println("   Your account is currently locked due to 3 consecutive failed PIN attempts.");
            System.err.println("   Please contact customer support for assistance.");
            return null;
        }

        // PIN Verification Loop (Max 3 attempts)
        int attempts = 0;
        while (attempts < ATMService.MAX_PIN_ATTEMPTS) {
            System.out.print("Enter 4-digit PIN: ");
            String enteredPin = scanner.nextLine().trim();

            if (user.getPin().equals(enteredPin)) {
                System.out.println("\n✅ Login successful!");
                System.out.println("Welcome, " + user.getName() + ".");
                return user;
            } else {
                attempts++;
                int remaining = ATMService.MAX_PIN_ATTEMPTS - attempts;

                if (remaining > 0) {
                    System.out.printf("❌ Incorrect PIN. Attempts remaining: %d%n%n", remaining);
                } else {
                    System.err.println("\n🔒 Maximum 3 failed PIN attempts reached.");
                    try {
                        atmService.lockAccount(accountNumber);
                        System.err.println("❌ Your account has been LOCKED for security reasons.");
                    } catch (SQLException e) {
                        System.err.println("Error updating account lock status: " + e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    private static void runATMMenu(User user) {
        boolean exit = false;

        while (!exit) {
            printATMMenu();
            System.out.print("Enter your choice (1-5): ");

            int choice = -1;
            try {
                String input = scanner.nextLine().trim();
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("\n❌ Invalid choice! Please enter a valid number between 1 and 5.");
                continue;
            }

            switch (choice) {
                case 1:
                    handleCheckBalance(user);
                    break;
                case 2:
                    handleWithdraw(user);
                    break;
                case 3:
                    handleDeposit(user);
                    break;
                case 4:
                    handleMiniStatement(user);
                    break;
                case 5:
                    System.out.println("\nLogging out...");
                    exit = true;
                    break;
                default:
                    System.out.println("\n❌ Invalid choice! Please enter a number between 1 and 5.");
            }
        }
    }

    private static void handleCheckBalance(User user) {
        printSubHeader("CHECK BALANCE");
        try {
            double currentBalance = atmService.checkBalance(user.getAccountNumber());
            System.out.printf("Available Balance: ₹%,.2f%n", currentBalance);
        } catch (SQLException e) {
            System.err.println("❌ Unable to fetch balance from database: " + e.getMessage());
        }
        printDivider();
    }

    private static void handleWithdraw(User user) {
        printSubHeader("WITHDRAW MONEY");
        System.out.print("Enter withdrawal amount: ₹");

        double amount = -1;
        try {
            String input = scanner.nextLine().trim();
            amount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("\n❌ Invalid amount. Please enter a valid numeric value.");
            printDivider();
            return;
        }

        try {
            boolean success = atmService.withdraw(user, amount);
            if (success) {
                System.out.println("\n✅ Withdrawal successful!");
                System.out.printf("Withdrawn Amount: ₹%,.2f%n", amount);
                System.out.printf("Remaining Balance: ₹%,.2f%n", user.getBalance());
            }
        } catch (SQLException e) {
            System.err.println("❌ Withdrawal transaction failed: " + e.getMessage());
        }
        printDivider();
    }

    private static void handleDeposit(User user) {
        printSubHeader("DEPOSIT MONEY");
        System.out.print("Enter deposit amount: ₹");

        double amount = -1;
        try {
            String input = scanner.nextLine().trim();
            amount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("\n❌ Invalid amount. Please enter a valid numeric value.");
            printDivider();
            return;
        }

        try {
            boolean success = atmService.deposit(user, amount);
            if (success) {
                System.out.println("\n✅ Deposit successful!");
                System.out.printf("Deposited Amount: ₹%,.2f%n", amount);
                System.out.printf("Updated Balance: ₹%,.2f%n", user.getBalance());
            }
        } catch (SQLException e) {
            System.err.println("❌ Deposit transaction failed: " + e.getMessage());
        }
        printDivider();
    }

    private static void handleMiniStatement(User user) {
        printSubHeader("MINI STATEMENT");
        try {
            List<Transaction> transactions = atmService.getMiniStatement(user.getAccountNumber(), 5);

            if (transactions.isEmpty()) {
                System.out.println("No transaction history available.");
            } else {
                System.out.printf("%-18s %-12s %-15s %-15s%n", "Date & Time", "Type", "Amount", "Balance After");
                System.out.println("----------------------------------------------------------------");

                for (Transaction tx : transactions) {
                    String dateStr = tx.getTransactionDate() != null ? dateFormat.format(tx.getTransactionDate()) : "N/A";
                    System.out.printf("%-18s %-12s ₹%-14,.2f ₹%-14,.2f%n",
                            dateStr,
                            tx.getTransactionType(),
                            tx.getAmount(),
                            tx.getBalanceAfterTransaction());
                }
            }

            double currentBalance = atmService.checkBalance(user.getAccountNumber());
            System.out.println("----------------------------------------------------------------");
            System.out.printf("Current Balance: ₹%,.2f%n", currentBalance);

        } catch (SQLException e) {
            System.err.println("❌ Unable to fetch mini statement: " + e.getMessage());
        }
        printDivider();
    }

    private static void printHeader(String title) {
        System.out.println("\n========================================");
        System.out.printf("       %s%n", title);
        System.out.println("========================================");
    }

    private static void printSubHeader(String title) {
        System.out.println("\n----------------------------------------");
        System.out.printf("           %s%n", title);
        System.out.println("----------------------------------------");
    }

    private static void printATMMenu() {
        System.out.println("\n========================================");
        System.out.println("               ATM MENU");
        System.out.println("========================================");
        System.out.println("1. Check Balance");
        System.out.println("2. Withdraw Money");
        System.out.println("3. Deposit Money");
        System.out.println("4. Mini Statement");
        System.out.println("5. Exit");
        System.out.println("========================================");
    }

    private static void printDivider() {
        System.out.println("========================================");
    }
}
