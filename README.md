# ATM Simulation System

A **Core Java + JDBC + MySQL** ATM simulation with a responsive
**HTML/CSS/Vanilla JavaScript** web interface.

The project intentionally uses **Core Java only**. It uses Java's built-in `HttpServer`, JDBC, DAO/service architecture, MySQL transactions, Docker, Render, and Aiven.

## 🚀 Live Demo

**Live application:** https://atm-simulation-cgua.onrender.com

> The demo uses free-tier services. The first request after inactivity
> may take longer because the Render service can spin down.

### Demo Accounts

  Account   PIN    Name           Initial Balance
  --------- ------ ------------ -----------------
  1001      1234   Gaius                  ₹25,000
  1002      4321   Jane Doe               ₹50,000
  1003      9999   Alex Smith             ₹10,000

These are educational/demo credentials only.

------------------------------------------------------------------------

## ✨ Features

-   Account number + 4-digit PIN authentication
-   Account lock after 3 incorrect PIN attempts
-   Check balance
-   Withdraw money with validation
-   Deposit money with validation
-   Mini statement with recent transactions
-   Logout/session handling
-   MySQL persistent storage
-   JDBC connectivity
-   JDBC transaction handling with commit/rollback
-   Responsive framework-free web UI
-   Docker deployment
-   Live Render deployment
-   Cloud MySQL database on Aiven
-   Environment-based database configuration

------------------------------------------------------------------------

## 🛠️ Technology Stack

  Technology           Purpose
  -------------------- ---------------------------------
  Java / Core Java     Application and business logic
  Java `HttpServer`    Lightweight HTTP server and API
  JDBC                 Java-to-MySQL communication
  MySQL                Persistent database
  MySQL Connector/J    JDBC driver
  HTML5 / CSS3         Web interface
  Vanilla JavaScript   Frontend logic and API calls
  Docker               Containerized deployment
  Render               Cloud application hosting
  Aiven                Cloud MySQL hosting
  Git / GitHub         Version control

**No Spring, Spring Boot, Maven, or Gradle is used.**

------------------------------------------------------------------------

## 🏗️ Architecture

``` text
Browser
   │
   │ HTTP / JSON
   ▼
ATMHttpServer.java
   │
   ▼
ATMService.java
   │
   ├── UserDAO
   └── TransactionDAO
            │
            ▼
   DatabaseConnection
            │
           JDBC
            │
            ▼
       MySQL Database
       ├── users
       └── transactions
```

### Layer Responsibilities

-   **Frontend:** Login, dashboard, balance, withdrawal, deposit, mini
    statement and logout.
-   **ATMHttpServer:** Serves frontend files and exposes `/api/...`
    endpoints.
-   **ATMService:** Contains ATM business rules and transaction
    handling.
-   **UserDAO:** User/account SQL operations.
-   **TransactionDAO:** Transaction history SQL operations.
-   **Model:** `User` and `Transaction` data objects.
-   **DatabaseConnection:** Creates JDBC connections from configuration.

------------------------------------------------------------------------

## 📁 Project Structure

``` text
ATM-Simulation/
├── database/
│   └── atm_simulation.sql
├── frontend/
│   ├── index.html
│   ├── script.js
│   └── style.css
├── lib/
│   └── mysql-connector-j-26.7.0.jar
├── src/
│   ├── api/
│   │   └── ATMHttpServer.java
│   ├── dao/
│   │   ├── UserDAO.java
│   │   └── TransactionDAO.java
│   ├── model/
│   │   ├── User.java
│   │   └── Transaction.java
│   ├── service/
│   │   └── ATMService.java
│   ├── util/
│   │   └── DatabaseConnection.java
│   └── Main.java
├── Dockerfile
├── .gitignore
└── README.md
```

------------------------------------------------------------------------

## 🗄️ Database

Database:

``` text
atm_simulation
```

Tables:

``` text
users
transactions
```

### `users`

-   `id` --- primary key
-   `account_number` --- unique account number
-   `name` --- account holder
-   `pin` --- demo PIN
-   `balance` --- current balance
-   `status` --- `ACTIVE` / `LOCKED`

### `transactions`

-   `id` --- primary key
-   `account_number` --- related account
-   `transaction_type` --- `DEPOSIT` / `WITHDRAWAL`
-   `amount`
-   `balance_after_transaction`
-   `transaction_date`

------------------------------------------------------------------------

## 🔄 Transaction Handling

A withdrawal is handled as a logical database transaction:

``` text
Validate amount
      ↓
Check balance
      ↓
Update balance
      ↓
Insert transaction
      ↓
COMMIT
```

If a database operation fails, the application can roll back the
transaction to avoid partial updates.

------------------------------------------------------------------------

## 🌐 API Endpoints

  Endpoint                   Purpose
  -------------------------- -------------------------
  `POST /api/login`          Authenticate account
  `GET /api/balance`         Get current balance
  `POST /api/withdraw`       Withdraw money
  `POST /api/deposit`        Deposit money
  `GET /api/ministatement`   Get recent transactions

The frontend communicates with these endpoints using JavaScript
`fetch()`.

------------------------------------------------------------------------

# 💻 Run Locally

## Requirements

-   JDK 21 or later
-   MySQL
-   MySQL Workbench (recommended)
-   Git
-   Any Java IDE/editor

No Maven or Gradle is required.

## 1. Clone

``` bash
git clone https://github.com/gaiusparasa10-hash/ATM-Simulation.git
cd ATM-Simulation
```

## 2. Create the Database

Open:

``` text
database/atm_simulation.sql
```

Run the complete script in MySQL Workbench.

## 3. Configure Database Variables

### Windows Command Prompt

``` cmd
set ATM_DB_URL=jdbc:mysql://localhost:3306/atm_simulation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set ATM_DB_USER=root
set ATM_DB_PASSWORD=YOUR_MYSQL_PASSWORD
```

### PowerShell

``` powershell
$env:ATM_DB_URL="jdbc:mysql://localhost:3306/atm_simulation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:ATM_DB_USER="root"
$env:ATM_DB_PASSWORD="YOUR_MYSQL_PASSWORD"
```

Never commit a real password to GitHub.

## 4. Compile

From the repository root:

``` cmd
dir /s /b src\*.java > sources.txt
javac --add-modules jdk.httpserver -cp "lib\mysql-connector-j-26.7.0.jar" -d out @sources.txt
```

## 5. Run

``` cmd
java --add-modules jdk.httpserver -cp "out;lib\mysql-connector-j-26.7.0.jar" Main
```

The application automatically starts the web server.

Open:

``` text
http://localhost:8080
```

Test with:

``` text
Account: 1001
PIN:     1234
```

------------------------------------------------------------------------

# 🐳 Docker Deployment

The repository includes a `Dockerfile`.

The container:

1.  Uses a Java JDK
2.  Copies `src`, `frontend`, and `lib`
3.  Compiles the Java application with `javac`
4.  Includes MySQL Connector/J
5.  Starts `Main`

The Java server reads the `PORT` environment variable supplied by the
hosting platform.

------------------------------------------------------------------------

# ☁️ Cloud Deployment

## Render

Render hosts the Core Java web application.

``` text
GitHub
   ↓
Render Docker Build
   ↓
Core Java HTTP Server
   ↓
Public HTTPS URL
```

Live URL:

**https://atm-simulation-cgua.onrender.com**

The free instance may spin down after inactivity, which can cause a
cold-start delay.

## Aiven

Aiven hosts the cloud MySQL database.

``` text
Browser
   ↓
Render
   ↓
Core Java
   ↓
JDBC
   ↓
Aiven MySQL
```

Render uses these environment variables:

``` text
ATM_DB_URL
ATM_DB_USER
ATM_DB_PASSWORD
```

The database password is not stored in the GitHub repository.

------------------------------------------------------------------------

# 🔐 Configuration and Security

The application reads database configuration using environment variables
rather than requiring cloud credentials to be committed to source
control.

Important variables:

``` text
ATM_DB_URL
ATM_DB_USER
ATM_DB_PASSWORD
```

The `.gitignore` excludes local environment files such as:

``` text
.env
```

This is an educational project, not production banking software. A real
banking system would require stronger security such as hashed
PINs/passwords, secure sessions, rate limiting, audit logging,
encryption, secrets management, and stronger authorization.

------------------------------------------------------------------------

# 🧪 Testing Checklist

-   [x] Login with valid account and PIN
-   [x] Invalid PIN handling
-   [x] 3-attempt account lockout
-   [x] Balance display
-   [x] Withdrawal validation
-   [x] Deposit validation
-   [x] Balance updates
-   [x] Transaction history
-   [x] Mini statement
-   [x] Logout
-   [x] Local Java execution
-   [x] Docker build
-   [x] Render deployment
-   [x] Aiven MySQL connectivity

------------------------------------------------------------------------

# 🧠 Concepts Demonstrated

### Core Java

-   OOP
-   Encapsulation
-   Collections
-   Exception handling
-   Input validation
-   Standard Java HTTP server

### JDBC / MySQL

-   JDBC driver
-   `Connection`
-   `PreparedStatement`
-   `ResultSet`
-   SQL queries
-   Commit / rollback
-   Foreign keys and indexes

### Architecture

-   Model layer
-   DAO pattern
-   Service layer
-   API layer
-   Separation of concerns

### Web

-   HTML5
-   CSS3
-   Vanilla JavaScript
-   HTTP APIs
-   JSON
-   `fetch()`

### Deployment

-   Docker
-   GitHub
-   Render
-   Aiven
-   Environment variables

------------------------------------------------------------------------

## 👨‍💻 Author

**Gaius Parasa**

GitHub: https://github.com/gaiusparasa10-hash

Repository: https://github.com/gaiusparasa10-hash/ATM-Simulation

------------------------------------------------------------------------

## ⭐ Status

**Live and deployed**

-   ✅ Core Java backend
-   ✅ JDBC + MySQL
-   ✅ DAO + Service architecture
-   ✅ HTML/CSS/JavaScript frontend
-   ✅ Docker
-   ✅ Render deployment
-   ✅ Aiven cloud MySQL
-   ✅ Live demo
