# ATM Simulation System

A **Core Java + JDBC + MySQL** dual-interface (Console CLI & Web Frontend) ATM Simulation project.

This project demonstrates practical Java programming, object-oriented programming, JDBC database connectivity, SQL, DAO/service architecture, transaction handling, input validation, account authentication, balance management, transaction history, and basic configuration/security practices.

---

## 1. Project Overview

The ATM Simulation System is a dual-interface application (Console & Web Frontend) that allows a user to:

- Log in using an account number and 4-digit PIN
- Check the current account balance
- Withdraw money
- Deposit money
- View the latest 5 transactions in a mini statement table
- Automatically lock an account after 3 incorrect PIN attempts
- Store account and transaction information in MySQL
- Use JDBC for database communication
- Use database transactions for money operations
- Access the ATM via either an interactive Console CLI or a clean HTML/CSS/JavaScript Web Interface

### Project Category

> **Core Java + JDBC + MySQL + Vanilla Web UI**

It demonstrates backend database programming with JDBC and MySQL, exposed via both CLI and a lightweight standard Java HTTP Server (`com.sun.net.httpserver`).

---

## 2. Technologies Used

| Technology | Purpose |
|---|---|
| Java | Core application logic, OOP, and standard HTTP Server (`com.sun.net.httpserver`) |
| Core Java | Classes, objects, encapsulation, exceptions, collections, input handling |
| HTML5 / CSS3 / Vanilla JS | Clean, responsive ATM Web Frontend (No external frameworks) |
| JDBC | Java-to-MySQL database communication |
| MySQL | Persistent data storage |
| SQL | Database schema and queries |
| MySQL Connector/J | JDBC driver |
| Git/GitHub | Version control and project hosting |


### Maven / `pom.xml`

This project currently does **not** use Maven or Gradle.

Therefore, there is currently **no `pom.xml` file**. The MySQL JDBC driver is added to the Eclipse build path manually.

---

## 3. Project Structure

```text
ATM-Simulation/
│
├── database/
│   └── atm_simulation.sql
│
├── frontend/
│   ├── index.html                   # ATM Web UI Layout
│   ├── style.css                    # Responsive Vanilla CSS Styling
│   └── script.js                    # REST API Communication & Dynamic Views
│
├── src/
│   ├── api/
│   │   └── ATMHttpServer.java       # Standard JDK Java HTTP API Server
│   │
│   ├── model/
│   │   ├── User.java
│   │   └── Transaction.java
│   │
│   ├── dao/
│   │   ├── UserDAO.java
│   │   └── TransactionDAO.java
│   │
│   ├── service/
│   │   └── ATMService.java
│   │
│   ├── util/
│   │   └── DatabaseConnection.java
│   │
│   └── Main.java
│
├── .gitignore
├── .project
├── .classpath
└── README.md
```

> `.classpath` and `.project` are Eclipse project files. They are ignored by Git through `.gitignore` and do not need to be uploaded to GitHub.

---

## 4. Architecture

The project supports both Web and Console interfaces through a layered architecture:

```text
HTML/CSS/JavaScript Frontend (frontend/)        Console Interface (CLI)
                    |                                     |
                    v                                     |
    ATMHttpServer.java (Port 8080)                        |
                    |                                     |
                    +------------------+------------------+
                                       |
                                       v
                                ATMService.java
                                       |
                                ----------------
                                |              |
                                v              v
                            UserDAO       TransactionDAO
                                |              |
                                -----------    |
                                         |      |
                                         v      v
                                     MySQL Database
                                         ^
                                         |
                                DatabaseConnection
```

### Responsibilities

#### `frontend/` (HTML / CSS / JavaScript)
Responsible for:
- User interface rendering (Login, Dashboard, Balance, Withdraw, Deposit, Mini Statement)
- Capturing user actions and making async `fetch()` API calls to `/api/...` endpoints
- Session state tracking (`sessionStorage`) and DOM updates

#### `ATMHttpServer.java`
Responsible for:
- Running standard JDK HTTP server on port 8080
- Serving static frontend web assets (`index.html`, `style.css`, `script.js`)
- Exposing REST API endpoints (`/api/login`, `/api/balance`, `/api/withdraw`, `/api/deposit`, `/api/ministatement`)
- Delegating API requests to `ATMService.java`

#### `Main.java`
Responsible for:
- Application launcher (prompts user to choose Web ATM Server mode or Console CLI mode)
- Console menu interface & interactive user inputs when in CLI mode
- Calling the service layer
- Displaying results

#### `ATMService.java`

Responsible for:

- ATM business rules
- Login-related operations
- Withdrawal validation
- Deposit validation
- Balance updates
- Transaction handling
- Account locking

#### `UserDAO.java`

Responsible for database operations related to users/accounts, such as:

- Finding a user by account number
- Updating balance
- Updating account status

#### `TransactionDAO.java`

Responsible for transaction history, such as:

- Inserting transactions
- Retrieving recent transactions

#### `DatabaseConnection.java`

Responsible for creating JDBC connections to MySQL.

#### Model classes

- `User.java` represents account/user data.
- `Transaction.java` represents transaction data.

---

## 5. Database Design

The project uses the MySQL database:

```text
atm_simulation
```

The main tables are:

```text
users
transactions
```

### `users` table

The `users` table stores account information.

| Column | Purpose |
|---|---|
| `id` | Primary key |
| `account_number` | ATM account number |
| `name` | Account holder name |
| `pin` | Demo ATM PIN |
| `balance` | Current account balance |
| `status` | Account status such as ACTIVE/LOCKED |

### `transactions` table

The `transactions` table stores transaction history.

| Column | Purpose |
|---|---|
| `id` | Primary key |
| `account_number` | Account associated with the transaction |
| `transaction_type` | DEPOSIT or WITHDRAWAL |
| `amount` | Transaction amount |
| `balance_after_transaction` | Balance after the transaction |
| `transaction_date` | Transaction date/time |

---

## 6. Database Setup

### Step 1: Start MySQL

Open MySQL Workbench and make sure the MySQL server is running.

### Step 2: Open the SQL script

Open:

```text
database/atm_simulation.sql
```

in MySQL Workbench.

### Step 3: Execute the script

Run the complete SQL script.

It creates the database, tables, and sample data supplied with the project.

### Step 4: Select the database

```sql
USE atm_simulation;
```

### Step 5: Check the tables

```sql
SHOW TABLES;
```

You should see:

```text
transactions
users
```

### Step 6: Check users

```sql
SELECT * FROM users;
```

### Step 7: Check transactions

```sql
SELECT * FROM transactions;
```

---

## 7. MySQL JDBC Driver

The application uses **MySQL Connector/J**.

The JDBC driver must be available in the Eclipse build path.

In Eclipse:

```text
Right-click project
    ↓
Properties
    ↓
Java Build Path
    ↓
Libraries
    ↓
Classpath
    ↓
Add External JARs...
```

Select your MySQL Connector/J `.jar` file.

The exact Connector/J version may vary.

After adding it, it should appear under:

```text
Referenced Libraries
```

---

## 8. Database Configuration

### Important: Never commit your MySQL password

The project uses environment variables instead of storing the database password directly in Java source code.

`DatabaseConnection.java` reads:

```java
private static final String USER =
        System.getenv("ATM_DB_USER");

private static final String PASSWORD =
        System.getenv("ATM_DB_PASSWORD");
```

Do **not** change this to:

```java
private static final String PASSWORD = "your-real-password";
```

Do not commit real passwords, `.env` files, or other secrets to GitHub.

### Current connection configuration

The application connects to:

```text
localhost:3306
```

using database:

```text
atm_simulation
```

The username and password come from:

```text
ATM_DB_USER
ATM_DB_PASSWORD
```

---

## 9. Configure Environment Variables on Windows PowerShell

Open PowerShell.

Set the username:

```powershell
$env:ATM_DB_USER="root"
```

Set your MySQL password:

```powershell
$env:ATM_DB_PASSWORD="YOUR_MYSQL_PASSWORD"
```

Example:

```powershell
$env:ATM_DB_USER="root"
$env:ATM_DB_PASSWORD="your_password"
```

Verify the username:

```powershell
$env:ATM_DB_USER
```

Verify that the password variable is set:

```powershell
$env:ATM_DB_PASSWORD
```

> Do not paste your real password into GitHub, screenshots, documentation, or public issue trackers.

These `$env:` commands apply to the **current PowerShell session**.

---

## 10. Permanent Windows Environment Variables

If you want the variables to remain available for future terminals:

```powershell
[Environment]::SetEnvironmentVariable("ATM_DB_USER", "root", "User")
```

```powershell
[Environment]::SetEnvironmentVariable("ATM_DB_PASSWORD", "YOUR_MYSQL_PASSWORD", "User")
```

Close and reopen Eclipse/PowerShell after changing persistent environment variables so the new process can see them.

To check the saved user-level values:

```powershell
[Environment]::GetEnvironmentVariable("ATM_DB_USER", "User")
```

```powershell
[Environment]::GetEnvironmentVariable("ATM_DB_PASSWORD", "User")
```

---

## 11. PowerShell vs Command Prompt

### PowerShell

Use:

```powershell
$env:ATM_DB_USER
$env:ATM_DB_PASSWORD
```

### Command Prompt (`cmd`)

Use:

```cmd
echo %ATM_DB_USER%
echo %ATM_DB_PASSWORD%
```

Do not mix the two syntaxes.

---

## 12. Running the Project in Eclipse

### Step 1: Open Eclipse

Start Eclipse.

### Step 2: Open the project

Import/open the `ATM-Simulation` project.

### Step 3: Check the JDBC driver

Confirm that MySQL Connector/J appears under:

```text
Referenced Libraries
```

### Step 4: Configure environment variables

In PowerShell:

```powershell
$env:ATM_DB_USER="root"
$env:ATM_DB_PASSWORD="YOUR_MYSQL_PASSWORD"
```

If Eclipse was already running before the variables were configured, restart Eclipse.

### Step 5: Run the application

Open:

```text
src/Main.java
```

Right-click:

```text
Main.java
    ↓
Run As
    ↓
Java Application
```

---

## 13. Configure Environment Variables Directly in Eclipse

If Eclipse does not receive the variables from the PowerShell process, configure them in the run configuration.

Go to:

```text
Run
→ Run Configurations...
→ Java Application
→ Main
→ Environment
```

Add:

```text
ATM_DB_USER=YOUR_MYSQL_USERNAME
ATM_DB_PASSWORD=YOUR_MYSQL_PASSWORD
```

Then:

```text
Apply
→ Run
```

---

## 13B. Running the Web ATM Interface (HTML / CSS / JavaScript)

The application supports a modern, responsive Web UI powered by standard Java (`com.sun.net.httpserver.HttpServer`).

### How to Launch the Web Frontend:

1. Configure database environment variables (`ATM_DB_USER` and `ATM_DB_PASSWORD`).
2. Run `Main.java` (as a Java Application in Eclipse, IDE, or terminal).
3. When prompted in the console:
   ```text
   ========================================
          ATM SIMULATION SYSTEM
   ========================================
   Select Application Interface Mode:
   1. Launch Web ATM Interface (HTML/CSS/JS Frontend)
   2. Launch Console ATM Interface (CLI)
   ========================================
   Enter mode choice (1 or 2): 1
   ```
4. Enter `1` and press Enter.
5. Open your web browser and navigate to:
   ```text
   http://localhost:8080
   ```

### Web Interface Features & Screens:
- **Login Screen**: Enter Account Number (e.g. `1001`) & 4-digit PIN (`1234`).
- **Dashboard**: Displays real-time balance badge and welcome greeting.
- **Check Balance**: Instant balance view.
- **Withdraw Money**: Enter withdrawal amount with ₹20,000 transaction limit check.
- **Deposit Money**: Enter deposit amount with instant balance update.
- **Mini Statement**: Renders recent 5 transactions in a formatted HTML table.
- **Logout**: Clears session and returns to login screen.

---

## 14. Application Flow

When the program starts:

```text
========================================
       ATM SIMULATION SYSTEM
========================================
Select Application Interface Mode:
1. Launch Web ATM Interface (HTML/CSS/JS Frontend)
2. Launch Console ATM Interface (CLI)
========================================
Enter mode choice (1 or 2):
```

Select **`1`** for the **Web ATM Interface** (accessible at `http://localhost:8080`) or **`2`** for the **Console CLI**.

If running in Console mode:

```text
Enter Account Number: 1001
Enter 4-digit PIN: 1234

Login successful!
Welcome, Gaius.
```

The ATM menu is displayed.

---

## 15. ATM Menu

```text
========================================
               ATM MENU
========================================
1. Check Balance
2. Withdraw Money
3. Deposit Money
4. Mini Statement
5. Exit
========================================
```

---

## 16. Option 1 - Check Balance

Enter:

```text
1
```

The application reads the current balance from MySQL.

Example:

```text
CHECK BALANCE
Available Balance: ₹35,000.00
```

---

## 17. Option 2 - Withdraw Money

Enter:

```text
2
```

Then enter an amount, for example:

```text
5000
```

Example result:

```text
Withdrawal successful!
Withdrawn Amount: ₹5,000.00
Remaining Balance: ₹30,000.00
```

The balance is updated in MySQL and a transaction record is inserted.

---

## 18. Option 3 - Deposit Money

Enter:

```text
3
```

Then enter an amount, for example:

```text
15000
```

The application updates the balance and stores the deposit in transaction history.

---

## 19. Option 4 - Mini Statement

Enter:

```text
4
```

The application retrieves the latest 5 transactions.

Example:

```text
----------------------------------------
           MINI STATEMENT
----------------------------------------
Date & Time        Type         Amount          Balance After
----------------------------------------------------------------
15-08-2026 03:10   WITHDRAWAL   ₹      5,000.00 ₹     30,000.00
15-08-2026 03:02   DEPOSIT      ₹     15,000.00 ₹     35,000.00
14-08-2026 21:15   DEPOSIT      ₹      7,000.00 ₹     25,000.00
14-08-2026 16:50   WITHDRAWAL   ₹      5,000.00 ₹     18,000.00
13-08-2026 14:30   DEPOSIT      ₹     10,000.00 ₹     23,000.00
----------------------------------------------------------------
Current Balance: ₹30,000.00
```

The exact values and dates depend on the current database state.

---

## 20. Option 5 - Exit

Enter:

```text
5
```

The application logs out:

```text
Logging out...

Thank you for using the ATM Simulation System. Have a great day!
```

---

## 21. Sample Accounts

The supplied SQL script contains demo accounts.

| Account | PIN | Name | Initial Balance |
|---|---|---|---:|
| 1001 | 1234 | Gaius | ₹25,000 |
| 1002 | 4321 | Jane Doe | ₹50,000 |
| 1003 | 9999 | Alex Smith | ₹10,000 |

These are educational/demo credentials, not real banking credentials.

---

## 22. Authentication and Account Locking

The application allows up to 3 incorrect PIN attempts.

Example:

```text
Enter 4-digit PIN: 1111
Incorrect PIN. Attempts remaining: 2

Enter 4-digit PIN: 2222
Incorrect PIN. Attempts remaining: 1

Enter 4-digit PIN: 3333
Maximum 3 failed PIN attempts reached.

Your account has been LOCKED for security reasons.
```

The account status is updated in the database.

---

## 23. JDBC Flow

The application communicates with MySQL through JDBC:

```text
Java Application / Web Server
       |
       v
DatabaseConnection
       |
       v
MySQL JDBC Driver
       |
       v
MySQL Server
       |
       v
atm_simulation
       |
       +---- users
       |
       +---- transactions
```

Typical JDBC operations include:

1. Load the JDBC driver
2. Create a `Connection`
3. Create a `PreparedStatement`
4. Execute SQL
5. Process the `ResultSet`
6. Close resources

---

## 24. DAO Layer

DAO means:

> Data Access Object

The DAO layer separates database code from business logic.

For example:

```text
UserDAO
```

handles SQL related to users/accounts.

```text
TransactionDAO
```

handles transaction history.

This separation makes the application easier to understand and maintain.

---

## 25. Service Layer

`ATMService.java` contains the main ATM business logic.

For a withdrawal, the application needs to validate the operation before changing the balance.

Conceptually:

```text
Check amount
     |
     v
Amount valid?
     |
     v
Enough balance?
     |
     v
Update balance
     |
     v
Insert transaction
```

---

## 26. Database Transaction Handling

Money operations can involve multiple database operations.

For example, a withdrawal can require:

```text
1. Update account balance
2. Insert transaction history
```

The service layer uses JDBC transaction handling so these operations can be treated as one logical unit.

Conceptually:

```text
START TRANSACTION
       |
       +--> Update balance
       |
       +--> Insert transaction
       |
       v
     COMMIT
```

If an operation fails:

```text
START TRANSACTION
       |
       +--> Update balance
       |
       +--> Error
       |
       v
    ROLLBACK
```

This helps prevent partial updates.

---

## 27. Input Validation

The application validates inputs such as:

- Empty account number
- Invalid menu choice
- Non-numeric amount
- Invalid withdrawal amount
- Invalid deposit amount
- Insufficient balance
- Incorrect PIN

---

## 28. Exception Handling

The project uses Java exception handling for database and input-related errors.

For example:

```java
try {
    // database operation
} catch (SQLException e) {
    // handle database error
}
```

This allows the application to report problems instead of failing without explanation.

---

## 29. Security and Configuration

The project has been configured so database credentials are not hard-coded in Java source code.

The application uses:

```text
ATM_DB_USER
ATM_DB_PASSWORD
```

instead.

The repository does not hardcode passwords.

The `.gitignore` also excludes:

```text
.env
```

for local environment/configuration files.

---

## 30. `.gitignore`

The project uses `.gitignore` to exclude unnecessary or local files.

Recommended contents:

```gitignore
# Eclipse
.classpath
.project
.settings/

# Compiled Java
bin/
target/
*.class

# IDE
.vscode/
.idea/

# Logs
*.log

# Environment/configuration
.env

# OS files
.DS_Store
Thumbs.db
```

---

## 31. Git Setup

From the project directory:

```powershell
cd "C:\Users\GAIUS\.gemini\antigravity\scratch\ATM-Simulation"
```

Check:

```powershell
git status
```

---

## 32. Check for Credentials Before Committing

From the project directory, check for passwords or other known secrets.

```powershell
Get-ChildItem -Recurse -File | Select-String "YOUR_OLD_PASSWORD"
```

---

## 33. Git Commit

After reviewing the files:

```powershell
git add .
git commit -m "Add basic ATM frontend"
```

---

## 34. Connect to GitHub

```powershell
git push origin main
```

---

## 35. How Another Person Can Run the Project

A person cloning the repository should:

### Step 1
Clone the repository.

### Step 2
Open/import the project in Eclipse / VS Code / IntelliJ.

### Step 3
Install and start MySQL.

### Step 4
Execute:
```text
database/atm_simulation.sql
```

### Step 5
Add MySQL Connector/J to the build path.

### Step 6
Configure the database environment variables (`ATM_DB_USER` and `ATM_DB_PASSWORD`).

### Step 7
Run `Main.java`. Select `1` to start the Web ATM Server, then open `http://localhost:8080` in a browser.

---

## 36. Troubleshooting

### MySQL JDBC Driver not found
Make sure MySQL Connector/J is included under Referenced Libraries.

### Database environment variables are not configured
Set `ATM_DB_USER` and `ATM_DB_PASSWORD` in your terminal or Run Configurations.

---

## 37. Testing Checklist

### Login & UI
- [x] Correct account number & PIN (Web & Console)
- [x] Incorrect PIN & 3-attempt lockout
- [x] Balance display card
- [x] Withdrawal with validation
- [x] Deposit with validation
- [x] Mini Statement HTML table rendering
- [x] Logout navigation

---

## 38. Concepts Demonstrated

### Core Java & Web
- Classes and objects
- Encapsulation
- Standard Java HTTP Server (`com.sun.net.httpserver`)
- HTML5 / Vanilla CSS3 / Vanilla JS REST API integration
- JDBC PreparedStatements & Transactions (`commit`/`rollback`)
- Exception handling & Environment variables

---

## 39. Resume Description

### ATM Simulation System | Core Java, JDBC, MySQL, HTML/CSS/JS

Developed a dual-interface ATM simulation application in Core Java and MySQL, featuring both a interactive CLI and a responsive HTML/CSS/JavaScript Web Interface powered by a built-in Java HTTP Server. Implemented PIN authentication with 3-attempt lockout protection, real-time balance inquiry, deposits, withdrawals, mini statements, and explicit JDBC transaction management (`commit`/`rollback`).

---

## 40. Final Notes

This project demonstrates practical skills across both Core Java backend development (JDBC, MySQL, DAO pattern, transactions) and clean, framework-free web frontend development (HTML, CSS, JavaScript REST API fetch).
