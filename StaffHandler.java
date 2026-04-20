import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;

public class StaffHandler implements HttpHandler {

    /* Route to appropriate staff action */
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        String token = SessionManager.extractTokenFromCookie(cookieHeader);
        String role = SessionManager.getRole(token);

        if (path.equals("/api/lost-reports") && method.equals("GET")) {
            /* Staff sees all, student sees own */
            if ("staff".equals(role)) {
                handleGetAllLostReports(exchange);
            } else if ("student".equals(role)) {
                String userId = SessionManager.getUserId(token);
                handleGetStudentLostReports(exchange, userId);
            } else {
                Server.sendResponse(exchange, 401, "{\"error\":\"Not authenticated\"}");
            }
        } else if (path.equals("/api/lost-reports") && method.equals("POST")) {
            /* Students only */
            if (!"student".equals(role)) {
                Server.sendResponse(exchange, 403, "{\"error\":\"Students only\"}");
                return;
            }
            String userId = SessionManager.getUserId(token);
            handleSubmitLostReport(exchange, userId);
        } else if (path.equals("/api/dashboard") && method.equals("GET")) {
            /* Staff only */
            if (!"staff".equals(role)) {
                Server.sendResponse(exchange, 403, "{\"error\":\"Staff only\"}");
                return;
            }
            handleGetDashboard(exchange);
        } else {
            Server.sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
        }
    }

    /* Get all lost reports for staff */
    private void handleGetAllLostReports(HttpExchange exchange) throws IOException {
        List<LostReport> reports = DatabaseManager.getAllLostReports();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < reports.size(); i++) {
            sb.append(reportToJson(reports.get(i)));
            if (i < reports.size() - 1) sb.append(",");
        }
        sb.append("]");
        Server.sendResponse(exchange, 200, sb.toString());
    }

    /* Get lost reports for a specific student */
    private void handleGetStudentLostReports(HttpExchange exchange, String enrollmentId) throws IOException {
        List<LostReport> reports = DatabaseManager.getLostReportsByStudent(enrollmentId);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < reports.size(); i++) {
            sb.append(reportToJson(reports.get(i)));
            if (i < reports.size() - 1) sb.append(",");
        }
        sb.append("]");
        Server.sendResponse(exchange, 200, sb.toString());
    }

    /* Submit a new lost item report from a student */
    private void handleSubmitLostReport(HttpExchange exchange, String enrollmentId) throws IOException {
        String body = Server.readBody(exchange);
        Map<String, String> params = parseForm(body);
        String itemName = params.get("itemName");
        String category = params.getOrDefault("category", "Other");
        String description = params.getOrDefault("description", "");
        String location = params.getOrDefault("location", "");
        String dateLost = params.getOrDefault("dateLost", "");

        if (itemName == null || itemName.isEmpty()) {
            Server.sendResponse(exchange, 400, "{\"error\":\"Item name required\"}");
            return;
        }

        DatabaseManager.insertLostReport(enrollmentId, itemName, category, description, location, dateLost);
        Server.sendResponse(exchange, 200, "{\"success\":true}");
    }

    /* Return staff dashboard statistics */
    private void handleGetDashboard(HttpExchange exchange) throws IOException {
        int totalItems = DatabaseManager.countItems();
        int pendingClaims = DatabaseManager.countPendingClaims();
        int lostReports = DatabaseManager.countLostReports();
        int claimedItems = DatabaseManager.countItemsByStatus("Claimed");
        int availableItems = DatabaseManager.countItemsByStatus("Available");

        List<Item> recent = DatabaseManager.getRecentItems(5);
        StringBuilder recentJson = new StringBuilder("[");
        for (int i = 0; i < recent.size(); i++) {
            Item item = recent.get(i);
            recentJson.append("{\"itemId\":").append(item.getItemId())
                .append(",\"name\":\"").append(esc(item.getName())).append("\"")
                .append(",\"category\":\"").append(esc(item.getCategory())).append("\"")
                .append(",\"status\":\"").append(esc(item.getStatus())).append("\"")
                .append(",\"dateFound\":\"").append(esc(item.getDateFound())).append("\"")
                .append("}");
            if (i < recent.size() - 1) recentJson.append(",");
        }
        recentJson.append("]");

        String json = "{" +
            "\"totalItems\":" + totalItems + "," +
            "\"pendingClaims\":" + pendingClaims + "," +
            "\"lostReports\":" + lostReports + "," +
            "\"claimedItems\":" + claimedItems + "," +
            "\"availableItems\":" + availableItems + "," +
            "\"recentItems\":" + recentJson.toString() +
        "}";

        Server.sendResponse(exchange, 200, json);
    }

    /* Convert LostReport to JSON string */
    private String reportToJson(LostReport r) {
        return "{" +
            "\"reportId\":" + r.getReportId() + "," +
            "\"enrollmentId\":\"" + esc(r.getEnrollmentId()) + "\"," +
            "\"itemName\":\"" + esc(r.getItemName()) + "\"," +
            "\"category\":\"" + esc(r.getCategory()) + "\"," +
            "\"description\":\"" + esc(r.getDescription()) + "\"," +
            "\"location\":\"" + esc(r.getLocation()) + "\"," +
            "\"dateLost\":\"" + esc(r.getDateLost()) + "\"" +
        "}";
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
                } catch (Exception e) { map.put(kv[0], kv[1]); }
            }
        }
        return map;
    }

    /* Escape special characters for JSON */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
