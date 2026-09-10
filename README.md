# 🏧 ATM Simulation System

A dual-interface (**Console CLI** & **Web Frontend**) ATM Simulation application built with **Core Java**, **JDBC**, and **MySQL**. It features a modern, framework-free HTML/CSS/JavaScript web UI served directly via Java's built-in `com.sun.net.httpserver.HttpServer`.

[![Live Demo](https://img.shields.io/badge/Live%20Demo-Render-brightgreen?style=for-the-badge&logo=render)](https://atm-simulation-cgua.onrender.com)
[![Java](https://img.shields.io/badge/Java-21+-orange?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/Database-MySQL-blue?style=for-the-badge&logo=mysql)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker)](https://www.docker.com/)

---

## 🚀 Live Demo & Credentials

**[Live Demo](https://atm-simulation-cgua.onrender.com)**

> ℹ️ *Deployed on Render free tier. Initial request may take a few seconds if service is sleeping.*

### Demo Accounts

| Account Number | 4-Digit PIN | Account Holder | Initial Balance |
|---|---|---|---|
| **1001** | `1234` | Gaius | ₹25,000 |
| **1002** | `4321` | Jane Doe | ₹50,000 |
| **1003** | `9999` | Alex Smith | ₹10,000 |

---

## ✨ Features

- 🔐 **PIN Authentication:** Account login with 3-attempt auto-lockout protection.
- 💵 **Banking Operations:** Real-time balance inquiry, deposits, and withdrawal limit validation.
- 📊 **Mini Statement:** Dynamic table displaying recent 5 transactions.
- 🔄 **ACID Transaction Management:** JDBC commit and rollback ensuring atomicity for money operations.
- 🌐 **Dual Interface:** Interactive CLI console or responsive Vanilla Web UI.
- 🐳 **Cloud Deployment:** Dockerized app hosted on **Render** with **Aiven Cloud MySQL**.

---

## 🛠️ Tech Stack & Architecture

| Layer | Technology | Purpose |
|---|---|---|
| **Frontend** | HTML5, CSS3, Vanilla JS | Clean, responsive UI with async REST `fetch()` calls |
| **Backend** | Java 21 (Core Java) | Application logic, standard JDK HTTP Server (`HttpServer`) |
| **Persistence** | JDBC, MySQL | PreparedStatements, Connection pooling, Transaction handling |
| **Deployment** | Docker, Render, Aiven | Containerization, cloud web server, and cloud MySQL database |

> 📌 *Built using pure Core Java without Spring, Spring Boot, Maven, or Gradle frameworks.*

### Architecture Overview

```text
[ Web Browser / CLI ] ──> [ Java HttpServer / Main ] ──> [ ATMService ] ──> [ UserDAO / TransactionDAO ] ──> [ MySQL Database ]
```

---

## 📁 Project Structure

```text
ATM-Simulation/
├── src/
│   ├── api/          # ATMHttpServer.java (JDK Web Server & REST API)
│   ├── dao/          # UserDAO.java, TransactionDAO.java (Database access)
│   ├── model/        # User.java, Transaction.java (Data models)
│   ├── service/      # ATMService.java (Business logic & transactions)
│   ├── util/         # DatabaseConnection.java (JDBC manager)
│   └── Main.java     # Main entry point (CLI/Web mode selector)
├── frontend/         # index.html, style.css, script.js (Vanilla Web UI)
├── database/         # atm_simulation.sql (Schema & sample data)
├── lib/              # mysql-connector-j-26.7.0.jar (JDBC driver)
└── Dockerfile        # Container deployment setup
```

---

## 🔌 REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/login` | Authenticate account number & PIN |
| `GET` | `/api/balance` | Retrieve current account balance |
| `POST` | `/api/withdraw` | Process withdrawal & update balance |
| `POST` | `/api/deposit` | Process deposit & update balance |
| `GET` | `/api/ministatement` | Fetch latest 5 transaction records |

---

## 🚀 Quick Start (Local Setup)

### 1. Prerequisites
- JDK 21+
- MySQL Server

### 2. Database Setup
Run `database/atm_simulation.sql` in MySQL Workbench or CLI:
```sql
SOURCE database/atm_simulation.sql;
```

### 3. Environment Variables
Set database credentials in your terminal:

**PowerShell:**
```powershell
$env:ATM_DB_URL="jdbc:mysql://localhost:3306/atm_simulation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:ATM_DB_USER="root"
$env:ATM_DB_PASSWORD="your_mysql_password"
```

**Command Prompt:**
```cmd
set ATM_DB_URL=jdbc:mysql://localhost:3306/atm_simulation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set ATM_DB_USER=root
set ATM_DB_PASSWORD=your_mysql_password
```

### 4. Compile & Run

```bash
# Compile
dir /s /b src\*.java > sources.txt
javac --add-modules jdk.httpserver -cp "lib\mysql-connector-j-26.7.0.jar" -d out @sources.txt

# Run
java --add-modules jdk.httpserver -cp "out;lib\mysql-connector-j-26.7.0.jar" Main
```

Access Web UI at **`http://localhost:8080`**.

---

## 🐳 Docker Deployment

```bash
# Build image
docker build -t atm-simulation .

# Run container
docker run -p 8080:8080 -e ATM_DB_URL="..." -e ATM_DB_USER="..." -e ATM_DB_PASSWORD="..." atm-simulation
```

---

## 👨‍💻 Author

**Gaius Parasa**
- **GitHub:** [@gaiusparasa10-hash](https://github.com/gaiusparasa10-hash)
- **Repository:** [ATM-Simulation](https://github.com/gaiusparasa10-hash/ATM-Simulation)
