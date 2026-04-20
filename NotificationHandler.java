import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.util.*;

public class NotificationHandler implements HttpHandler {

    /* Route to appropriate notification action */
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        String token = SessionManager.extractTokenFromCookie(cookieHeader);
        String role = SessionManager.getRole(token);
        String userId = SessionManager.getUserId(token);

        if (role == null || !"student".equals(role)) {
            Server.sendResponse(exchange, 401, "{\"error\":\"Not authenticated\"}");
            return;
        }

        if (path.equals("/api/notifications") && method.equals("GET")) {
            handleGetNotifications(exchange, userId);
        } else if (path.equals("/api/notifications/unread-count") && method.equals("GET")) {
            handleGetUnreadCount(exchange, userId);
        } else if (path.equals("/api/notifications/mark-read") && method.equals("POST")) {
            handleMarkAllRead(exchange, userId);
        } else {
            Server.sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
        }
    }

    /* Get all notifications for the logged-in student */
    private void handleGetNotifications(HttpExchange exchange, String enrollmentId) throws IOException {
        List<Notification> notifs = DatabaseManager.getNotificationsByStudent(enrollmentId);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < notifs.size(); i++) {
            Notification n = notifs.get(i);
            sb.append("{")
              .append("\"notifId\":").append(n.getNotifId()).append(",")
              .append("\"enrollmentId\":\"").append(esc(n.getEnrollmentId())).append("\",")
              .append("\"message\":\"").append(esc(n.getMessage())).append("\",")
              .append("\"isRead\":").append(n.isRead())
              .append("}");
            if (i < notifs.size() - 1) sb.append(",");
        }
        sb.append("]");
        Server.sendResponse(exchange, 200, sb.toString());
    }

    /* Return count of unread notifications */
    private void handleGetUnreadCount(HttpExchange exchange, String enrollmentId) throws IOException {
        int count = DatabaseManager.countUnreadNotifications(enrollmentId);
        Server.sendResponse(exchange, 200, "{\"count\":" + count + "}");
    }

    /* Mark all notifications as read */
    private void handleMarkAllRead(HttpExchange exchange, String enrollmentId) throws IOException {
        DatabaseManager.markAllNotificationsRead(enrollmentId);
        Server.sendResponse(exchange, 200, "{\"success\":true}");
    }

    /* Escape special characters for JSON */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
