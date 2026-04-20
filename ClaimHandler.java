import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;

public class ClaimHandler implements HttpHandler {

    /* Route to appropriate claim action */
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        String token = SessionManager.extractTokenFromCookie(cookieHeader);
        String role = SessionManager.getRole(token);
        String userId = SessionManager.getUserId(token);

        if (role == null) {
            Server.sendResponse(exchange, 401, "{\"error\":\"Not authenticated\"}");
            return;
        }

        if (path.equals("/api/claims") && method.equals("POST")) {
            /* Students only */
            if (!"student".equals(role)) {
                Server.sendResponse(exchange, 403, "{\"error\":\"Students only\"}");
                return;
            }
            handleSubmitClaim(exchange, userId);
        } else if (path.equals("/api/claims") && method.equals("GET")) {
            if ("staff".equals(role)) {
                handleGetAllClaims(exchange);
            } else {
                handleGetStudentClaims(exchange, userId);
            }
        } else if (path.startsWith("/api/claims/") && path.endsWith("/approve") && method.equals("POST")) {
            /* Staff only */
            if (!"staff".equals(role)) {
                Server.sendResponse(exchange, 403, "{\"error\":\"Staff only\"}");
                return;
            }
            int claimId = parseId(path.substring("/api/claims/".length(), path.length() - "/approve".length()));
            handleApproveClaim(exchange, claimId);
        } else if (path.startsWith("/api/claims/") && path.endsWith("/reject") && method.equals("POST")) {
            /* Staff only */
            if (!"staff".equals(role)) {
                Server.sendResponse(exchange, 403, "{\"error\":\"Staff only\"}");
                return;
            }
            int claimId = parseId(path.substring("/api/claims/".length(), path.length() - "/reject".length()));
            handleRejectClaim(exchange, claimId);
        } else {
            Server.sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
        }
    }

    /* Submit a new claim request from a student */
    private void handleSubmitClaim(HttpExchange exchange, String enrollmentId) throws IOException {
        String body = Server.readBody(exchange);
        Map<String, String> params = parseForm(body);
        String itemIdStr = params.get("itemId");
        String hostelRoom = params.get("hostelRoom");
        String idCardDetails = params.get("idCardDetails");

        if (itemIdStr == null || hostelRoom == null || idCardDetails == null) {
            Server.sendResponse(exchange, 400, "{\"error\":\"Missing fields\"}");
            return;
        }

        int itemId = parseId(itemIdStr);
        Item item = DatabaseManager.getItemById(itemId);
        if (item == null) {
            Server.sendResponse(exchange, 404, "{\"error\":\"Item not found\"}");
            return;
        }
        if (!"Available".equals(item.getStatus())) {
            Server.sendResponse(exchange, 400, "{\"error\":\"Item is not available for claim\"}");
            return;
        }

        int claimId = DatabaseManager.insertClaim(itemId, enrollmentId, hostelRoom, idCardDetails);
        /* Update item status to Claim Pending */
        DatabaseManager.updateItemStatus(itemId, "Claim Pending", "");

        /* Notify the student their claim was received */
        DatabaseManager.insertNotification(enrollmentId,
            "Your claim for '" + item.getName() + "' has been submitted. Status: Pending.");

        Server.sendResponse(exchange, 200,
            "{\"success\":true,\"claimId\":" + claimId + "}");
    }

    /* Get all claim requests (staff) */
    private void handleGetAllClaims(HttpExchange exchange) throws IOException {
        List<ClaimRequest> claims = DatabaseManager.getAllClaims();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < claims.size(); i++) {
            sb.append(claimToJson(claims.get(i)));
            if (i < claims.size() - 1) sb.append(",");
        }
        sb.append("]");
        Server.sendResponse(exchange, 200, sb.toString());
    }

    /* Get claim requests for a specific student */
    private void handleGetStudentClaims(HttpExchange exchange, String enrollmentId) throws IOException {
        List<ClaimRequest> claims = DatabaseManager.getClaimsByStudent(enrollmentId);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < claims.size(); i++) {
            sb.append(claimToJson(claims.get(i)));
            if (i < claims.size() - 1) sb.append(",");
        }
        sb.append("]");
        Server.sendResponse(exchange, 200, sb.toString());
    }

    /* Approve a claim: mark item as Claimed */
    private void handleApproveClaim(HttpExchange exchange, int claimId) throws IOException {
        ClaimRequest claim = DatabaseManager.getClaimById(claimId);
        if (claim == null) {
            Server.sendResponse(exchange, 404, "{\"error\":\"Claim not found\"}");
            return;
        }
        DatabaseManager.updateClaimStatus(claimId, "Approved");
        DatabaseManager.updateItemStatus(claim.getItemId(), "Claimed", claim.getEnrollmentId());

        /* Notify student */
        Item item = DatabaseManager.getItemById(claim.getItemId());
        String itemName = item != null ? item.getName() : "item";
        DatabaseManager.insertNotification(claim.getEnrollmentId(),
            "Your claim for '" + itemName + "' has been APPROVED. Please collect from the Lost & Found office.");

        Server.sendResponse(exchange, 200, "{\"success\":true}");
    }

    /* Reject a claim: revert item to Available */
    private void handleRejectClaim(HttpExchange exchange, int claimId) throws IOException {
        ClaimRequest claim = DatabaseManager.getClaimById(claimId);
        if (claim == null) {
            Server.sendResponse(exchange, 404, "{\"error\":\"Claim not found\"}");
            return;
        }
        DatabaseManager.updateClaimStatus(claimId, "Rejected");
        DatabaseManager.updateItemStatus(claim.getItemId(), "Available", "");

        /* Notify student */
        Item item = DatabaseManager.getItemById(claim.getItemId());
        String itemName = item != null ? item.getName() : "item";
        DatabaseManager.insertNotification(claim.getEnrollmentId(),
            "Your claim for '" + itemName + "' was not approved. Please visit the Lost & Found office for more info.");

        Server.sendResponse(exchange, 200, "{\"success\":true}");
    }

    /* Convert ClaimRequest to JSON */
    private String claimToJson(ClaimRequest c) {
        /* Fetch item name for convenience */
        Item item = DatabaseManager.getItemById(c.getItemId());
        String itemName = item != null ? item.getName() : "Unknown";
        String itemCategory = item != null ? item.getCategory() : "";
        return "{" +
            "\"claimId\":" + c.getClaimId() + "," +
            "\"itemId\":" + c.getItemId() + "," +
            "\"itemName\":\"" + esc(itemName) + "\"," +
            "\"itemCategory\":\"" + esc(itemCategory) + "\"," +
            "\"enrollmentId\":\"" + esc(c.getEnrollmentId()) + "\"," +
            "\"hostelRoom\":\"" + esc(c.getHostelRoom()) + "\"," +
            "\"idCardDetails\":\"" + esc(c.getIdCardDetails()) + "\"," +
            "\"status\":\"" + esc(c.getStatus()) + "\"" +
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

    /* Parse integer safely */
    private int parseId(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return -1; }
    }
}
