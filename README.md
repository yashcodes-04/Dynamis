# Dynamis Gym - Multi-Role Authentication & Workspace Portal

> **Date:** 2026-11-16  |  **Author:** Yash

A modern desktop application built using **Java Swing** and **MySQL Workbench (`gymdb`)** featuring role-based authentication and 4 dedicated role workspaces:
1. **Gym Member**
2. **Receptionist**
3. **Fitness Trainer**
4. **Gym Owner**

---

## 📁 Project Architecture

```
java try/
├── db.properties               # MySQL database connection configuration
├── schema.sql                  # Database table definitions for gymdb
├── pom.xml                     # Maven project descriptor (for IntelliJ / Eclipse / VS Code)
├── compile.bat                 # Script to compile all Java source files
├── run.bat                     # Script to launch the application
├── lib/
│   └── mysql-connector-j.jar   # MySQL JDBC Driver
└── src/
    └── com/gym/
        ├── Main.java           # Application Entry Point
        ├── config/
        │   └── DBConnection.java  # JDBC Singleton & auto-table initialiser
        ├── model/
        │   ├── Role.java       # Enum: MEMBER, RECEPTIONIST, TRAINER, OWNER
        │   └── User.java       # User entity model
        ├── dao/
        │   └── UserDAO.java    # Registration, Authentication & DB queries
        ├── util/
        │   └── PasswordUtil.java # PBKDF2 cryptographic salt & hashing
        ├── session/
        │   └── UserSession.java# Active session state management
        └── ui/
            ├── Theme.java      # Modern gym dark-mode UI styles & palette
            ├── MainFrame.java  # CardLayout window orchestrator
            ├── LoginPanel.java # Sign In screen
            ├── RegisterPanel.java # Registration screen
            └── dashboard/
                ├── BaseDashboard.java         # Shared navbar & metric cards
                ├── MemberDashboard.java       # Screen 1: Member Workspace
                ├── ReceptionistDashboard.java # Screen 2: Receptionist Workspace
                ├── TrainerDashboard.java      # Screen 3: Trainer Workspace
                └── OwnerDashboard.java        # Screen 4: Gym Owner Workspace
```

---

## ⚙️ Database Configuration

1. In MySQL Workbench, make sure your database `gymdb` is created:
   ```sql
   CREATE DATABASE IF NOT EXISTS gymdb;
   ```
2. Check `db.properties` and verify your MySQL credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/gymdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=YOUR_MYSQL_PASSWORD
   ```
   *(Note: The table `users` will be automatically created on application startup if it doesn't already exist)*.

---

## 🚀 How to Run

### Option 1: Double-click or run the batch script (Windows)
```cmd
run.bat
```

### Option 2: Run via your favorite IDE
- Open the folder in **IntelliJ IDEA**, **Eclipse**, or **VS Code**.
- Run `com.gym.Main.java`.
