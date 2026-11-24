# Dynamis Gym - Enterprise Management & Multi-Role Workspace Portal

> **Author:** Yash | **Platform:** Java 17 (Adoptium / HotSpot) & MySQL Workbench (`gymdb`) | **Architecture:** MVC + DAO Pattern + Dark High-Contrast UI

**Dynamis Gym** is a role-based desktop ERP application engineered with **Java Swing** and **MySQL**. It features cryptographic authentication, active session lifecycle management, high-contrast dark-mode design, multithreaded background processing, and 4 dedicated operational dashboards:

1. **🏋️ Member Workspace**: Active membership monitoring, weekly workout regimen, booked class management, and live slot capacity booking.
2. **📋 Receptionist Workspace**: Membership fee collection, automated invoice generation, renewal logs, today's live check-in desk with attendance deletion (CRUD), and desk event queues.
3. **🥗 Trainer Workspace**: Client trainee roster, fitness assessment and target setting, curated nutrition protocols for all body types (*Ectomorph*, *Mesomorph*, *Endomorph*), and CSV/Text report exports.
4. **📊 Gym Owner Workspace**: Staff roster administration, financial analytics, and key performance metrics.

---

## 📚 Academic Syllabus & Java Concepts Mapping

This project is aligned with core Java programming concepts:

| Unit & Topic | Theoretical Concept | Concrete Implementation in Codebase |
| :--- | :--- | :--- |
| **Unit 1: OOP & Exceptions** | Inheritance, Polymorphism, Abstraction, Custom Exceptions, 2D Arrays | `BaseDashboard` inheritance, `UserNotFoundException`, `MemberNotFoundException`, `ValidationException`, `GymException`, 2D Array matrix extraction (`getAttendanceTableDataMatrix()`). |
| **Unit 2: Collections Framework** | `ArrayList`, `HashMap`, `HashSet`, `Comparator`, Custom Sorting | `IN_MEMORY_TRAINEES`, `WEEKLY_SCHEDULE` (`LinkedHashMap`), Trainee sorting (`Trainee.BY_NAME_ASC`, `BY_CURRENT_WEIGHT_DESC`, `BY_BODY_TYPE`). |
| **Unit 3: Multithreading & Concurrency** | `Runnable`, Daemon Threads, Intrinsic Locks, Producer-Consumer Pattern | `LiveDeskTracker` (Background Daemon Thread), `SwingWorker` for non-blocking DB queries, `ReceptionDeskEventQueue` (thread-safe event queue with guarded notification). |
| **Unit 4: JDBC & Database Programming** | `DriverManager`, `Connection`, `PreparedStatement`, CRUD Operations | `DBConnection` Singleton, auto-migration SQL schemas, CRUD mapping in `UserDAO`, `ReceptionistDAO`, `TrainerDAO`, `BookingDAO`. |
| **Unit 5: Generics & Stream API** | Generic Interfaces, Lambda Expressions, Stream Pipelines (`filter`, `map`, `count`) | `GenericDAO<T, ID>`, Stream API aggregations for body-type metrics (`list.stream().filter(...).count()`), lambda action listeners. |
| **Unit 6: File I/O & Character Streams** | `BufferedWriter`, `FileWriter`, `JFileChooser`, CSV Export | `TrainerDAO.exportTraineesToCSV()`, `TrainerDAO.exportDietPlanToText()`, `FileExportUtil` with try-with-resources resource safety. |

---

## 📁 Project Architecture & File Tree

```
Dynamis/
├── db.properties               # MySQL database connection configuration
├── schema.sql                  # Database table definitions for gymdb
├── pom.xml                     # Maven project descriptor
├── compile.bat                 # Script to compile all Java source files
├── run.bat                     # Script to launch the application
├── lib/
│   └── mysql-connector-j.jar   # MySQL JDBC Driver (Connector/J)
└── src/
    └── com/gym/
        ├── Main.java           # Application entry point with dark theme initialization
        ├── config/
        │   └── DBConnection.java       # JDBC Singleton & auto-table creation/migration
        ├── model/
        │   ├── Role.java               # Enum: MEMBER, RECEPTIONIST, TRAINER, OWNER
        │   ├── User.java               # User account entity
        │   ├── Trainee.java            # Trainee client entity with BMI calculation
        │   ├── DietPlan.java           # Nutrition protocol model with macro breakdown
        │   ├── FeePayment.java         # Membership renewal transaction model
        │   ├── AttendanceRecord.java   # Member check-in log model
        │   ├── FitnessClass.java       # Gym group class catalog model
        │   └── BookedSession.java      # Member booked class reservation model
        ├── dao/
        │   ├── GenericDAO.java         # Generic CRUD DAO interface <T, ID>
        │   ├── UserDAO.java            # User authentication & registration queries
        │   ├── ReceptionistDAO.java    # Fee transactions, today's attendance & delete CRUD
        │   ├── TrainerDAO.java         # Trainee rosters, diet plans & CSV/Text exports
        │   └── BookingDAO.java         # Class catalog and member session reservations
        ├── util/
        │   ├── PasswordUtil.java       # PBKDF2 cryptographic password hashing
        │   ├── LiveDeskTracker.java    # Multithreaded background clock daemon thread
        │   ├── ReceptionDeskEventQueue.java # Inter-thread event feed queue
        │   └── FileExportUtil.java     # Safe character-stream CSV & text file writers
        ├── exception/
        │   ├── GymException.java             # Base checked gym application exception
        │   ├── AuthenticationException.java  # Failed login / credential mismatch
        │   ├── MemberNotFoundException.java  # Member lookup failure
        │   └── ValidationException.java      # Input validation & constraint errors
        ├── session/
        │   └── UserSession.java        # Thread-safe logged-in user session state
        └── ui/
            ├── Theme.java              # Dark palette, custom fonts, high-contrast combobox styler
            ├── ThemedDialog.java       # High-contrast modal dialogs (eliminates native OS white-on-white text)
            ├── MainFrame.java          # Primary CardLayout frame (1280 × 820 resolution)
            ├── LoginPanel.java         # Sign-in authentication view
            ├── RegisterPanel.java      # Role-based account creation view
            ├── ChooseMembershipPanel.java # Membership selection during registration
            ├── BookClassDialog.java    # Class booking modal with real-time seat counter
            └── dashboard/
                ├── BaseDashboard.java         # Shared navbar, header, and metric tiles
                ├── MemberDashboard.java       # Member workspace, weekly regimen, booked classes
                ├── ReceptionistDashboard.java # Fee collection, invoice generator, live check-ins
                ├── TrainerDashboard.java      # Trainee management, BMI badge, body-type diet plans
                └── OwnerDashboard.java        # Staff roster, financial KPIs, gym statistics
```

---

## ⚙️ Database Configuration

1. In MySQL Workbench or CLI, ensure the database exists:
   ```sql
   CREATE DATABASE IF NOT EXISTS gymdb;
   USE gymdb;
   ```
2. Check `db.properties` in the project root:
   ```properties
   db.url=jdbc:mysql://localhost:3306/gymdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=YOUR_MYSQL_PASSWORD
   ```
   *(Note: `DBConnection.java` will automatically verify and initialize all database tables (`users`, `fee_payments`, `attendance`, `booked_sessions`, `trainees`, `diet_plans`) on first launch).*

---

## 🚀 How to Build & Run

### Prerequisites
- **Java JDK 17** or newer (Eclipse Adoptium recommended).
- **MySQL Server 8.0+**.

### Step 1: Compile with UTF-8 Support
```powershell
& "C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot\bin\javac.exe" -encoding UTF-8 -cp "lib/mysql-connector-j.jar" -d bin src/com/gym/exception/*.java src/com/gym/model/*.java src/com/gym/util/*.java src/com/gym/config/*.java src/com/gym/dao/*.java src/com/gym/session/*.java src/com/gym/ui/*.java src/com/gym/ui/dashboard/*.java src/com/gym/*.java ; Copy-Item "db.properties" -Destination "bin\db.properties" -Force
```

### Step 2: Run the Application
```powershell
& "C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot\bin\java.exe" -cp "bin;lib/mysql-connector-j.jar" com.gym.Main
```
*(Alternatively, execute `compile.bat` followed by `run.bat` on Windows).*

---

## 🔑 Default Credentials & Role Testing Guide

| Role | Email | Password | Primary Capabilities to Test |
| :--- | :--- | :--- | :--- |
| **Member** | *Register via UI* | *Your password* | View weekly workout table, view remaining membership days, book fitness classes with capacity counts. |
| **Receptionist** | `receptionist@dynamis.com` | `password123` | Collect fee renewal, generate printed receipt, view today's check-in log, delete attendance entry (CRUD). |
| **Trainer** | *Register via UI* | *Your password* | Filter trainees by body type, update BMI & targets, review macro breakdown, assign diet plans, export CSV. |
| **Gym Owner** | `owner@dynamis.com` | `password123` | View revenue totals, total active members, staff roster table, and membership retention metrics. |

---

## 🎨 Design System & Accessibility Highlights

- **Universal High Contrast**: All tables, text fields, dropdown popups, and modal dialogs use high-contrast dark tones (`#12151C`, `#1A1D24`, `#202430`, `#F8F9FA`, `#CBD5E1`) to guarantee 100% human visibility and readability.
- **Custom Themed Dialogs (`ThemedDialog`)**: Replaced all native OS `JOptionPane` popups with custom-rendered cards featuring solid crimson primary buttons (`#F05454`) and slate secondary buttons (`#262B3A`), completely eliminating Windows white-on-white invisible text issues.
- **No Truncation (`...`)**: Tables utilize expanded preferred column widths and horizontal scroll awareness to guarantee that full member names, emails, fitness goals, and timestamps are displayed without abbreviation.
