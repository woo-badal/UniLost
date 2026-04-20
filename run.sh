#!/bin/bash
# Bennett University Lost & Found - Build & Run Script
# Usage: chmod +x run.sh && ./run.sh

echo "========================================"
echo " Bennett University Lost & Found"
echo " Build & Run Script"
echo "========================================"

# Check if sqlite-jdbc jar exists
JDBC_JAR="sqlite-jdbc.jar"
if [ ! -f "$JDBC_JAR" ]; then
  echo ""
  echo "ERROR: sqlite-jdbc.jar not found!"
  echo ""
  echo "Download it from:"
  echo "  https://github.com/xerial/sqlite-jdbc/releases"
  echo "  (e.g. sqlite-jdbc-3.45.1.0.jar, rename to sqlite-jdbc.jar)"
  echo ""
  exit 1
fi

echo ""
echo "[1/3] Compiling Java sources..."
javac -cp ".:$JDBC_JAR" \
  Student.java \
  Staff.java \
  Item.java \
  LostReport.java \
  ClaimRequest.java \
  Notification.java \
  DatabaseManager.java \
  SessionManager.java \
  AuthHandler.java \
  ItemHandler.java \
  ClaimHandler.java \
  StaffHandler.java \
  NotificationHandler.java \
  Server.java

if [ $? -ne 0 ]; then
  echo ""
  echo "ERROR: Compilation failed. Fix errors above and try again."
  exit 1
fi

echo "[2/3] Compilation successful!"
echo "[3/3] Starting server on http://localhost:8080 ..."
echo ""
echo "  Student Login : http://localhost:8080/login.html"
echo "  Staff Login   : http://localhost:8080/staff-login.html"
echo ""
echo "  Demo Student  : E21CSE001 / student123"
echo "  Staff Admin   : admin / bennett123"
echo ""
echo "Press Ctrl+C to stop."
echo "========================================"

java -cp ".:$JDBC_JAR" Server
