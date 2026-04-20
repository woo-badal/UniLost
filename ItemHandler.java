import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ItemHandler implements HttpHandler {

    /* Route to appropriate item action */
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        String token = SessionManager.extractTokenFromCookie(cookieHeader);

        /* Add CORS headers */
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (path.equals("/api/items") && method.equals("GET")) {
            handleGetItems(exchange);
        } else if (path.startsWith("/api/items/") && method.equals("GET")) {
            int itemId = parseId(path.substring("/api/items/".length()));
            handleGetItem(exchange, itemId);
        } else if (path.equals("/api/items") && method.equals("POST")) {
            /* Staff only */
            if (!"staff".equals(SessionManager.getRole(token))) {
                Server.sendResponse(exchange, 403, "{\"error\":\"Staff only\"}");
                return;
            }
            handleUploadItem(exchange);
        } else if (path.startsWith("/api/items/") && method.equals("DELETE")) {
            /* Staff only */
            if (!"staff".equals(SessionManager.getRole(token))) {
                Server.sendResponse(exchange, 403, "{\"error\":\"Staff only\"}");
                return;
            }
            int itemId = parseId(path.substring("/api/items/".length()));
            handleDeleteItem(exchange, itemId);
        } else if (path.startsWith("/api/items/") && path.endsWith("/status") && method.equals("POST")) {
            /* Staff only */
            if (!"staff".equals(SessionManager.getRole(token))) {
                Server.sendResponse(exchange, 403, "{\"error\":\"Staff only\"}");
                return;
            }
            String idPart = path.substring("/api/items/".length(), path.length() - "/status".length());
            int itemId = parseId(idPart);
            handleUpdateStatus(exchange, itemId);
        } else {
            Server.sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
        }
    }

    /* Get all items with optional category/location filters */
    private void handleGetItems(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        String category = params.get("category");
        String location = params.get("location");

        List<Item> items = DatabaseManager.getAllItems(category, location);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append(itemToJson(items.get(i)));
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");
        Server.sendResponse(exchange, 200, sb.toString());
    }

    /* Get a single item by ID */
    private void handleGetItem(HttpExchange exchange, int itemId) throws IOException {
        Item item = DatabaseManager.getItemById(itemId);
        if (item == null) {
            Server.sendResponse(exchange, 404, "{\"error\":\"Item not found\"}");
            return;
        }
        Server.sendResponse(exchange, 200, itemToJson(item));
    }

    /* Upload a new found item with multipart form data */
    private void handleUploadItem(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("multipart/form-data")) {
            Server.sendResponse(exchange, 400, "{\"error\":\"Expected multipart form\"}");
            return;
        }

        /* Extract boundary */
        String boundary = "";
        for (String part : contentType.split(";")) {
            if (part.trim().startsWith("boundary=")) {
                boundary = part.trim().substring("boundary=".length());
                break;
            }
        }

        /* Read raw bytes */
        InputStream is = exchange.getRequestBody();
        byte[] bodyBytes = is.readAllBytes();
        is.close();

        /* Parse multipart fields */
        Map<String, String> fields = new HashMap<String, String>();
        String imagePath = "";

        String bodyStr = new String(bodyBytes, "UTF-8");
        String[] parts = bodyStr.split("--" + boundary);

        for (String part : parts) {
            if (part.contains("Content-Disposition")) {
                if (part.contains("filename=")) {
                    /* This is the file part */
                    String filename = extractFilename(part);
                    if (filename != null && !filename.isEmpty()) {
                        /* Extract file bytes after double newline */
                        int headerEnd = part.indexOf("\r\n\r\n");
                        if (headerEnd != -1) {
                            /* Save file */
                            String ext = filename.contains(".") ?
                                filename.substring(filename.lastIndexOf(".")) : ".jpg";
                            String savedName = "img_" + System.currentTimeMillis() + ext;
                            File uploadsDir = new File("uploads");
                            if (!uploadsDir.exists()) uploadsDir.mkdirs();
                            String fileContent = part.substring(headerEnd + 4);
                            /* Remove trailing boundary marker */
                            if (fileContent.endsWith("\r\n")) {
                                fileContent = fileContent.substring(0, fileContent.length() - 2);
                            }
                            /* Write using original bytes to preserve binary */
                            byte[] fileBytes = extractFileBytes(bodyBytes, boundary, filename);
                            if (fileBytes != null && fileBytes.length > 0) {
                                Files.write(Paths.get("uploads/" + savedName), fileBytes);
                                imagePath = "uploads/" + savedName;
                            }
                        }
                    }
                } else {
                    /* Text field */
                    String fieldName = extractFieldName(part);
                    int valStart = part.indexOf("\r\n\r\n");
                    if (fieldName != null && valStart != -1) {
                        String value = part.substring(valStart + 4).trim();
                        if (value.endsWith("--")) value = value.substring(0, value.length() - 2).trim();
                        fields.put(fieldName, value);
                    }
                }
            }
        }

        String name = fields.getOrDefault("name", "");
        String category = fields.getOrDefault("category", "Other");
        String description = fields.getOrDefault("description", "");
        String location = fields.getOrDefault("location", "");
        String dateFound = fields.getOrDefault("dateFound", "");

        if (name.isEmpty()) {
            Server.sendResponse(exchange, 400, "{\"error\":\"Item name required\"}");
            return;
        }

        int itemId = DatabaseManager.insertItem(name, category, description, location, dateFound, imagePath);

        /* Check for matching lost reports and send notifications */
        List<LostReport> matchingReports = DatabaseManager.getLostReportsByCategory(category);
        for (LostReport report : matchingReports) {
            String msg = "A " + category + " item '" + name + "' was found at " + location +
                         ". Check the Lost & Found portal!";
            DatabaseManager.insertNotification(report.getEnrollmentId(), msg);
        }

        Server.sendResponse(exchange, 200,
            "{\"success\":true,\"itemId\":" + itemId + ",\"notified\":" + matchingReports.size() + "}");
    }

    /* Delete an item */
    private void handleDeleteItem(HttpExchange exchange, int itemId) throws IOException {
        DatabaseManager.deleteItem(itemId);
        Server.sendResponse(exchange, 200, "{\"success\":true}");
    }

    /* Update item status */
    private void handleUpdateStatus(HttpExchange exchange, int itemId) throws IOException {
        String body = Server.readBody(exchange);
        Map<String, String> params = parseQuery(body);
        String status = params.get("status");
        String claimedBy = params.getOrDefault("claimedBy", "");
        if (status == null) {
            Server.sendResponse(exchange, 400, "{\"error\":\"Status required\"}");
            return;
        }
        DatabaseManager.updateItemStatus(itemId, status, claimedBy);
        Server.sendResponse(exchange, 200, "{\"success\":true}");
    }

    /* Convert Item to JSON string */
    private String itemToJson(Item item) {
        return "{" +
            "\"itemId\":" + item.getItemId() + "," +
            "\"name\":\"" + esc(item.getName()) + "\"," +
            "\"category\":\"" + esc(item.getCategory()) + "\"," +
            "\"description\":\"" + esc(item.getDescription()) + "\"," +
            "\"location\":\"" + esc(item.getLocation()) + "\"," +
            "\"dateFound\":\"" + esc(item.getDateFound()) + "\"," +
            "\"imagePath\":\"" + esc(item.getImagePath()) + "\"," +
            "\"status\":\"" + esc(item.getStatus()) + "\"," +
            "\"claimedBy\":\"" + esc(item.getClaimedBy()) + "\"" +
        "}";
    }

    /* Extract filename from content-disposition header */
    private String extractFilename(String part) {
        String[] lines = part.split("\r\n");
        for (String line : lines) {
            if (line.contains("Content-Disposition") && line.contains("filename=")) {
                int start = line.indexOf("filename=\"") + 10;
                int end = line.indexOf("\"", start);
                if (start > 9 && end > start) return line.substring(start, end);
            }
        }
        return null;
    }

    /* Extract field name from content-disposition header */
    private String extractFieldName(String part) {
        String[] lines = part.split("\r\n");
        for (String line : lines) {
            if (line.contains("Content-Disposition") && line.contains("name=")) {
                int start = line.indexOf("name=\"") + 6;
                int end = line.indexOf("\"", start);
                if (start > 5 && end > start) return line.substring(start, end);
            }
        }
        return null;
    }

    /* Extract raw file bytes from multipart body */
    private byte[] extractFileBytes(byte[] body, String boundary, String filename) {
        try {
            String marker = "filename=\"" + filename + "\"";
            String bodyStr = new String(body, "ISO-8859-1");
            int filenamePos = bodyStr.indexOf(marker);
            if (filenamePos == -1) return null;
            int headerEnd = bodyStr.indexOf("\r\n\r\n", filenamePos);
            if (headerEnd == -1) return null;
            int start = headerEnd + 4;
            String endMarker = "\r\n--" + boundary;
            int end = bodyStr.indexOf(endMarker, start);
            if (end == -1) end = body.length;
            byte[] result = new byte[end - start];
            System.arraycopy(body, start, result, 0, end - start);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /* Parse URL query string into map */
    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<String, String>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    map.put(java.net.URLDecoder.decode(kv[0], "UTF-8"),
                            java.net.URLDecoder.decode(kv[1], "UTF-8"));
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

    /* Parse integer from string safely */
    private int parseId(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return -1; }
    }
}
