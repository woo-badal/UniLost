@echo off
REM Bennett University Lost & Found - Windows Build & Run
REM Usage: Double-click run.bat or run from Command Prompt

echo ========================================
echo  Bennett University Lost ^& Found
echo  Build ^& Run Script (Windows)
echo ========================================

SET JDBC_JAR=sqlite-jdbc.jar

IF NOT EXIST %JDBC_JAR% (
    echo.
    echo ERROR: sqlite-jdbc.jar not found!
    echo.
    echo Download from:
    echo   https://github.com/xerial/sqlite-jdbc/releases
    echo   Rename the downloaded jar to sqlite-jdbc.jar
    echo.
    pause
    exit /b 1
)

echo.
echo [1/3] Compiling Java sources...
javac -cp ".;%JDBC_JAR%" ^
  Student.java ^
  Staff.java ^
  Item.java ^
  LostReport.java ^
  ClaimRequest.java ^
  Notification.java ^
  DatabaseManager.java ^
  SessionManager.java ^
  AuthHandler.java ^
  ItemHandler.java ^
  ClaimHandler.java ^
  StaffHandler.java ^
  NotificationHandler.java ^
  Server.java

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed.
    pause
    exit /b 1
)

echo [2/3] Compilation successful!
echo [3/3] Starting server...
echo.
echo   Student Login : http://localhost:8080/login.html
echo   Staff Login   : http://localhost:8080/staff-login.html
echo.
echo   Demo Student  : E21CSE001 / student123
echo   Staff Admin   : admin / bennett123
echo.
echo Press Ctrl+C to stop.
echo ========================================

java -cp ".;%JDBC_JAR%" Server
pause
