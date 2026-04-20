import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class AuthHandler implements HttpHandler {

    /* Route to appropriate auth action based on path */
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/api/login/student") && method.equals("POST")) {
            handleStudentLogin(exchange);
        } else if (path.equals("/api/login/staff") && method.equals("POST")) {
            handleStaffLogin(exchange);
        } else if (path.equals("/api/logout") && method.equals("POST")) {
            handleLogout(exchange);
        } else if (path.equals("/api/session") && method.equals("GET")) {
            handleGetSession(exchange);
        } else if (path.equals("/api/register/student") && method.equals("POST")) { // NEW
            handleStudentRegister(exchange); // NEW
        } else if (path.equals("/api/register/staff") && method.equals("POST")) { // NEW
            handleStaffRegister(exchange); // NEW
        } else {
            Server.sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
        }
    }

    /* Handle student login: validate credentials, create session */
    private void handleStudentLogin(HttpExchange exchange) throws IOException {
        String body = Server.readBody(exchange);
        Map<String, String> params = parseForm(body);
        String enrollmentId = params.get("enrollmentId");
        String password = params.get("password");

        if (enrollmentId == null || password == null) {
            Server.sendResponse(exchange, 400, "{\"error\":\"Missing credentials\"}");
            return;
        }

        Student student = DatabaseManager.authenticateStudent(enrollmentId, password);
        if (student == null) {
            Server.sendResponse(exchange, 401, "{\"error\":\"Invalid enrollment ID or password\"}");
            return;
        }

        String token = SessionManager.createSession(student.getEnrollmentId(), "student");
        exchange.getResponseHeaders().add("Set-Cookie",
            "session=" + token + "; Path=/; HttpOnly");
        Server.sendResponse(exchange, 200,
            "{\"success\":true,\"name\":\"" + student.getName() + "\",\"role\":\"student\"}");
    }

    /* Handle staff login: validate credentials from DB, create session */
    private void handleStaffLogin(HttpExchange exchange) throws IOException {
        String body = Server.readBody(exchange);
        Map<String, String> params = parseForm(body);
        String staffId = params.get("staffId");
        String password = params.get("password");

        if (staffId == null || password == null) {
            Server.sendResponse(exchange, 400, "{\"error\":\"Missing credentials\"}");
            return;
        }

        /* First check hardcoded admin account */
        boolean validHardcoded = staffId.equals("admin") && password.equals("bennett123");

        /* Then check registered staff accounts in DB */ // NEW
        Staff staff = DatabaseManager.authenticateStaff(staffId, password); // NEW

        if (!validHardcoded && staff == null) {
            Server.sendResponse(exchange, 401, "{\"error\":\"Invalid staff ID or password\"}");
            return;
        }

        String token = SessionManager.createSession(staffId, "staff");
        exchange.getResponseHeaders().add("Set-Cookie",
            "session=" + token + "; Path=/; HttpOnly");
        String displayName = (staff != null) ? staff.getName() : "Admin";
        Server.sendResponse(exchange, 200,
            "{\"success\":true,\"name\":\"" + displayName + "\",\"role\":\"staff\"}");
    }

    /* Handle logout: remove session and clear cookie */
    private void handleLogout(HttpExchange exchange) throws IOException {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        String token = SessionManager.extractTokenFromCookie(cookieHeader);
        if (token != null) SessionManager.removeSession(token);
        exchange.getResponseHeaders().add("Set-Cookie",
            "session=; Path=/; Max-Age=0");
        Server.sendResponse(exchange, 200, "{\"success\":true}");
    }

    /* Return current session info (userId, role) */
    private void handleGetSession(HttpExchange exchange) throws IOException {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        String token = SessionManager.extractTokenFromCookie(cookieHeader);
        String role = SessionManager.getRole(token);
        String userId = SessionManager.getUserId(token);

        if (role == null) {
            Server.sendResponse(exchange, 401, "{\"error\":\"Not authenticated\"}");
            return;
        }

        String nameJson = "\"Admin\"";
        if (role.equals("student")) {
            Student s = DatabaseManager.getStudentById(userId);
            if (s != null) nameJson = "\"" + s.getName() + "\"";
        } else if (role.equals("staff")) {
            Staff s = DatabaseManager.getStaffById(userId); // NEW
            if (s != null) nameJson = "\"" + s.getName() + "\""; // NEW
        }

        Server.sendResponse(exchange, 200,
            "{\"userId\":\"" + userId + "\",\"role\":\"" + role + "\",\"name\":" + nameJson + "}");
    }

    /* NEW - Handle student registration */
    private void handleStudentRegister(HttpExchange exchange) throws IOException {
        String body = Server.readBody(exchange);
        Map<String, String> params = parseForm(body);
        String name = params.get("name");
        String enrollmentId = params.get("enrollmentId");
        String hostelRoom = params.getOrDefault("hostelRoom", "");
        String password = params.get("password");

        if (name == null || enrollmentId == null || password == null ||
            name.isEmpty() || enrollmentId.isEmpty() || password.isEmpty()) {
            Server.sendResponse(exchange, 400, "{\"error\":\"Name, Enrollment ID and Password are required\"}");
            return;
        }

        /* Check if enrollment ID already exists */
        if (DatabaseManager.getStudentById(enrollmentId) != null) {
            Server.sendResponse(exchange, 409, "{\"error\":\"Enrollment ID already registered\"}");
            return;
        }

        boolean ok = DatabaseManager.registerStudent(enrollmentId, name, password, hostelRoom);
        if (ok) {
            Server.sendResponse(exchange, 200, "{\"success\":true}");
        } else {
            Server.sendResponse(exchange, 500, "{\"error\":\"Registration failed. Try again.\"}");
        }
    }

    /* NEW - Handle staff registration with secret code */
    private void handleStaffRegister(HttpExchange exchange) throws IOException {
        String body = Server.readBody(exchange);
        Map<String, String> params = parseForm(body);
        String name = params.get("name");
        String staffId = params.get("staffId");
        String password = params.get("password");
        String secretCode = params.get("secretCode");

        if (name == null || staffId == null || password == null || secretCode == null ||
            name.isEmpty() || staffId.isEmpty() || password.isEmpty() || secretCode.isEmpty()) {
            Server.sendResponse(exchange, 400, "{\"error\":\"All fields are required\"}");
            return;
        }

        /* Validate secret registration code */
        if (!secretCode.equals("BENNETT_STAFF_2024")) {
            Server.sendResponse(exchange, 403, "{\"error\":\"Invalid secret registration code\"}");
            return;
        }

        /* Check if staff ID already exists */
        if (DatabaseManager.getStaffById(staffId) != null) {
            Server.sendResponse(exchange, 409, "{\"error\":\"Staff ID already registered\"}");
            return;
        }

        /* Also block reuse of hardcoded admin ID */
        if (staffId.equals("admin")) {
            Server.sendResponse(exchange, 409, "{\"error\":\"Staff ID already in use\"}");
            return;
        }

        boolean ok = DatabaseManager.registerStaff(staffId, name, password);
        if (ok) {
            Server.sendResponse(exchange, 200, "{\"success\":true}");
        } else {
            Server.sendResponse(exchange, 500, "{\"error\":\"Registration failed. Try again.\"}");
        }
    }

    /* Parse URL-encoded form body into key-value map */
    private Map<String, String> parseForm(String body) {
        Map<String, String> map = new HashMap<String, String>();
        if (body == null || body.isEmpty()) return map;
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    map.put(URLDecoder.decode(kv[0], "UTF-8"),
                            URLDecoder.decode(kv[1], "UTF-8"));
                } catch (Exception e) {
                    map.put(kv[0], kv[1]);
                }
            }
        }
        return map;
    }
}
