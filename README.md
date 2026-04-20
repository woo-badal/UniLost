# Bennett University — Lost & Found Management System

A complete Lost & Found web application for Bennett University built with:
- **Backend**: Pure Java (`com.sun.net.httpserver.HttpServer`) — no Maven, no Spring
- **Database**: SQLite via JDBC (single JAR)
- **Frontend**: Plain HTML + CSS + JavaScript — no frameworks

---

## Project Structure

```
LostAndFound/
│
├── ── Java Source Files ──
│   ├── Student.java           Model: student (enrollmentId, name, password, hostelRoom)
│   ├── Staff.java             Model: staff (staffId, name, password)
│   ├── Item.java              Model: found item
│   ├── LostReport.java        Model: lost item report
│   ├── ClaimRequest.java      Model: claim request
│   ├── Notification.java      Model: notification
│   ├── DatabaseManager.java   All SQLite CRUD operations
│   ├── SessionManager.java    In-memory HashMap session management
│   ├── Server.java            HTTP server on port 8080, route registration
│   ├── AuthHandler.java       Login/logout endpoints
│   ├── ItemHandler.java       Found item CRUD + image upload
│   ├── ClaimHandler.java      Claim submission & approval
│   ├── StaffHandler.java      Lost reports & dashboard stats
│   └── NotificationHandler.java  Notifications API
│
├── ── Frontend ──
│   frontend/
│   ├── style.css              Shared stylesheet (Bennett red + dark theme)
│   ├── common.js              Shared JS (auth check, navbar, utilities)
│   ├── login.html             Role selection + student login
│   ├── staff-login.html       Staff login
│   ├── index.html             Browse found items (student home)
│   ├── item-detail.html       Full item detail + claim form
│   ├── report-lost.html       Report a lost item
│   ├── my-claims.html         Student's claim history
│   ├── notifications.html     Student notifications
│   ├── staff-dashboard.html   Staff overview & stats
│   ├── staff-upload.html      Upload a found item
│   ├── staff-claims.html      Review & process claims
│   ├── staff-lost-reports.html View all lost reports
│   └── staff-items.html       Manage items (edit status / delete)
│
├── run.sh                     Linux/Mac build & run script
├── run.bat                    Windows build & run script
└── README.md                  This file
```

---

## Setup Instructions

### Step 1 — Download SQLite JDBC JAR

Go to: https://github.com/xerial/sqlite-jdbc/releases

Download the latest `sqlite-jdbc-x.x.x.x.jar` and **rename it to `sqlite-jdbc.jar`**.

Place it in the **same folder** as your `.java` files.

### Step 2 — Compile

**Linux / Mac:**
```bash
chmod +x run.sh
./run.sh
```

**Windows:**
```
Double-click run.bat
```

**Or manually:**
```bash
# Linux/Mac
javac -cp ".:sqlite-jdbc.jar" *.java

# Windows
javac -cp ".;sqlite-jdbc.jar" *.java
```

### Step 3 — Run

```bash
# Linux/Mac
java -cp ".:sqlite-jdbc.jar" Server

# Windows
java -cp ".;sqlite-jdbc.jar" Server
```

### Step 4 — Open in Browser

- Student Portal: http://localhost:8080/login.html
- Staff Portal: http://localhost:8080/staff-login.html

---

## Demo Credentials

| Role    | Username    | Password     |
|---------|-------------|--------------|
| Student | E21CSE001   | student123   |
| Student | E21CSE002   | student123   |
| Student | E21CSE003   | student123   |
| Staff   | admin       | bennett123   |

---

## API Endpoints

| Method | Path                          | Access  | Description                  |
|--------|-------------------------------|---------|------------------------------|
| POST   | /api/login/student            | Public  | Student login                |
| POST   | /api/login/staff              | Public  | Staff login                  |
| POST   | /api/logout                   | Any     | Logout                       |
| GET    | /api/session                  | Any     | Get current session info     |
| GET    | /api/items                    | Any     | Get all found items          |
| GET    | /api/items/:id                | Any     | Get single item              |
| POST   | /api/items                    | Staff   | Upload new found item        |
| DELETE | /api/items/:id                | Staff   | Delete an item               |
| POST   | /api/items/:id/status         | Staff   | Update item status           |
| GET    | /api/claims                   | Auth    | Get claims (own or all)      |
| POST   | /api/claims                   | Student | Submit a claim               |
| POST   | /api/claims/:id/approve       | Staff   | Approve a claim              |
| POST   | /api/claims/:id/reject        | Staff   | Reject a claim               |
| GET    | /api/lost-reports             | Auth    | Get lost reports             |
| POST   | /api/lost-reports             | Student | Submit a lost report         |
| GET    | /api/dashboard                | Staff   | Dashboard statistics         |
| GET    | /api/notifications            | Student | Get notifications            |
| GET    | /api/notifications/unread-count | Student | Unread count               |
| POST   | /api/notifications/mark-read  | Student | Mark all as read             |

---

## Features

- **Role-based auth** with cookie sessions (student / staff)
- **Image upload** — saved to `uploads/` folder, path stored in DB
- **Lost report matching** — when staff uploads an item, students with matching category reports get notified automatically
- **Claim workflow**: Available → Claim Pending → Claimed
- **Notification bell** with unread count badge in navbar
- **Staff dashboard** with live stat cards and recent activity
- **Filter & search** on found items grid

---

## Notes

- Passwords stored as plain text (acceptable for this project scope)
- No external dependencies — just Java + one SQLite JAR
- Database file `lostandfound.db` is created automatically on first run
- Uploaded images are stored in the `uploads/` directory
