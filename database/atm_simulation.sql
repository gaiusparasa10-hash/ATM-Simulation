-- ========================================================
-- ATM SIMULATION SYSTEM DATABASE SCHEMA & SAMPLE DATA
-- Database Management System: MySQL
-- Database Name: atm_simulation
-- ========================================================

-- Step 1: Create Database
CREATE DATABASE IF NOT EXISTS atm_simulation;
USE atm_simulation;

-- Step 2: Drop Tables if already exist (Clean execution setup)
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS users;

-- Step 3: Create 'users' Table
-- Stores user account details, credentials (PIN), balance, and status
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    pin VARCHAR(10) NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    INDEX idx_account_number (account_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 4: Create 'transactions' Table
-- Stores history of deposits and withdrawals associated with account numbers
CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- 'DEPOSIT' or 'WITHDRAWAL'
    amount DECIMAL(15, 2) NOT NULL,
    balance_after_transaction DECIMAL(15, 2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_account FOREIGN KEY (account_number) 
        REFERENCES users(account_number) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    INDEX idx_tx_account (account_number),
    INDEX idx_tx_date (transaction_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 5: Insert Sample Users
INSERT INTO users (account_number, name, pin, balance, status) VALUES
('1001', 'Gaius', '1234', 25000.00, 'ACTIVE'),
('1002', 'Jane Doe', '4321', 50000.00, 'ACTIVE'),
('1003', 'Alex Smith', '9999', 10000.00, 'ACTIVE');

-- Step 6: Insert Sample Transactions for Testing Mini Statement
INSERT INTO transactions (account_number, transaction_type, amount, balance_after_transaction, transaction_date) VALUES
('1001', 'DEPOSIT', 5000.00, 15000.00, '2026-08-11 10:15:00'),
('1001', 'WITHDRAWAL', 2000.00, 13000.00, '2026-08-12 14:30:00'),
('1001', 'DEPOSIT', 10000.00, 23000.00, '2026-08-13 09:00:00'),
('1001', 'WITHDRAWAL', 5000.00, 18000.00, '2026-08-14 11:20:00'),
('1001', 'DEPOSIT', 7000.00, 25000.00, '2026-08-14 15:45:00');

-- Verify inserted data
SELECT * FROM users;
SELECT * FROM transactions;
