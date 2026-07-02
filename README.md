# Lab Booking — Engineering Lab Equipment Reservation System

A web application for booking engineering lab equipment at a university. Built as a coursework project for the EPAM Java Development program.

---

TODO
Pagination in DAO,


## Technology Stack

| Layer | Technology |
|---|---|
| Web | Jakarta Servlets 6.0, JSP |
| Framework | Spring Core 7.0, Spring MVC 7.0, Spring Security 7.1 |
| Database | H2 (in-memory), plain JDBC |
| Connection Pool | Custom BlockingQueue-based ConnectionPool |
| Build | Maven |
| Testing | JUnit 5, Mockito 5 |
| Server | Apache Tomcat 10.1.x |
| i18n | Spring MessageSource (EN / RU / KZ) |

---

## Architecture

The application follows a **Layered Architecture** with strict separation of concerns:
```
Servlet (web layer)
└── Service (business logic)
└── DAO (data access)
└── H2 in-memory database
```

### Package Structure
```
kz.epam.campus
├── config/       Spring configuration, connection pool, i18n helper
├── dao/          DAO interfaces and implementations
├── model/        Domain model classes (User, Booking, Slot, Equipment, …)
├── security/     Spring Security integration (UserDetailsService)
├── services/     Business logic services and exceptions
└── web/          HTTP Servlets (controllers)
```

---

## Prerequisites

- JDK 17 or later (tested on JDK 25)
- Apache Tomcat 10.1.x
- Maven 3.6+

---

## Build

```bash
mvn package
```

This compiles the project, runs all 87 unit and integration tests, and produces:
```
target/lab-booking-1.0-SNAPSHOT.war
```

---

## Run

1. Copy the WAR to Tomcat's webapps folder:
```bash
   cp target/lab-booking-1.0-SNAPSHOT.war $CATALINA_HOME/webapps/ROOT.war
```
2. Start Tomcat:
```bash
   $CATALINA_HOME/bin/catalina.sh run
```
3. Open your browser at **http://localhost:8090/**

> **Note:** The application uses an H2 in-memory database. All data is reset on every restart.

---

## Default Credentials

A seeded admin account is created automatically on first startup:

| Field | Value |
|---|---|
| Email | admin@lab.local |
| Password | admin123 |

Regular user accounts can be created via the **Register** page.

---

## Features

### User
- Register and log in
- Browse active lab equipment
- View available time slots by date
- Book a slot (max 2 bookings per week, up to 2 weeks in advance)
- Cancel confirmed bookings
- Edit profile (name, email, password)

### Admin
- Manage equipment (add, deactivate)
- Manage schedule (set working days / holidays — holiday declaration automatically cancels affected bookings and sends notifications)
- Manage users (view, deactivate)

### General
- Interface available in **English**, **Russian**, and **Kazakh**
- Language switcher on every page (session-based, no login required)
- CSS-styled UI

---

## Running Tests

```bash
mvn test
```

87 tests across 11 test classes:
- **Mockito unit tests** — isolated service-layer tests with mocked dependencies (`TestingBookingService`, `TestingUserService`, `TestingEquipmentService`, `TestingSlotService`, `TestingScheduleService`, `TestingNotificationService`)
- **Integration tests** — real Spring context + H2 database (`BookingRulesTest`, `BookingServiceTest`, `NotificationTest`, `ScheduleServiceTest`, `UserServiceTest`)

---

## Design Patterns

| Pattern | Location |
|---|---|
| Layered Architecture | Full application structure |
| MVC | Servlet → JSP view layer |
| Custom Connection Pool | `config/ConnectionPool.java` + `PooledConnection.java` |
| Builder | *(planned)* |
| Façade | *(planned)* |
| Strategy | *(planned)* |

---

## Project Structure
```
lab-booking/
├── src/
│   ├── main/
│   │   ├── java/kz/epam/campus/   # Application source
│   │   ├── resources/             # Spring XML config, i18n properties, schema.sql
│   │   └── webapp/                # JSPs, CSS
│   └── test/
│       └── java/                  # JUnit 5 + Mockito test classes
├── pom.xml
└── README.md
```
