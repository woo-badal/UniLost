import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;

public class Server {
    private static final int PORT = 8080;
    private static final String STATIC_DIR = "frontend";

    /* Main entry point - starts the HTTP server */
    public static void main(String[] args) throws Exception {
        /* Load SQLite JDBC driver */
        Class.forName("org.sqlite.JDBC");

        /* Initialize database */
        DatabaseManager.initDatabase();

        /* Create uploads directory */
        new File("uploads").mkdirs();

        /* Create HTTP server */
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        /* Register API handlers */
        AuthHandler authHandler = new AuthHandler();
        server.createContext("/api/login", authHandler);
        server.createContext("/api/logout", authHandler);
        server.createContext("/api/session", authHandler);
        server.createContext("/api/register", authHandler); // NEW

        ItemHandler itemHandler = new ItemHandler();
        server.createContext("/api/items", itemHandler);

        ClaimHandler claimHandler = new ClaimHandler();
        server.createContext("/api/claims", claimHandler);

        StaffHandler staffHandler = new StaffHandler();
        server.createContext("/api/lost-reports", staffHandler);
        server.createContext("/api/dashboard", staffHandler);

        NotificationHandler notifHandler = new NotificationHandler();
        server.createContext("/api/notifications", notifHandler);

        /* Serve uploaded images */
        server.createContext("/uploads", new StaticFileHandler(""));

        /* Serve frontend HTML/CSS/JS files */
        server.createContext("/", new StaticFileHandler(STATIC_DIR));

        server.setExecutor(null);
        server.start();
        System.out.println("Bennett University Lost & Found Server started on http://localhost:" + PORT);
        System.out.println("Open http://localhost:" + PORT + "/login.html to begin");
    }

    /* Read request body as string */
    public static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int read;
        while ((read = is.read(buf)) != -1) {
            bos.write(buf, 0, read);
        }
        return bos.toString("UTF-8");
    }

    /* Send a JSON response with given status code */
    public static void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = body.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    /* Static file handler - serves files from the given base directory */
    static class StaticFileHandler implements HttpHandler {
        private String baseDir;

        /* Constructor with base directory */
        public StaticFileHandler(String baseDir) {
            this.baseDir = baseDir;
        }

        /* Handle static file requests */
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            /* Handle uploads directory separately */
            if (path.startsWith("/uploads/")) {
                String filePath = path.substring(1);
                File file = new File(filePath);
                if (file.exists() && file.isFile()) {
                    serveFile(exchange, file, getMimeType(filePath));
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.getResponseBody().close();
                }
                return;
            }

            /* Default to login.html for root */
            if (path.equals("/")) path = "/login.html";

            /* Remove leading slash for file lookup */
            String filePath = baseDir + "/" + path.substring(1);
            File file = new File(filePath);

            if (!file.exists() || !file.isFile()) {
                file = new File(path.substring(1));
            }

            if (file.exists() && file.isFile()) {
                serveFile(exchange, file, getMimeType(path));
            } else {
                byte[] msg = "404 Not Found".getBytes("UTF-8");
                exchange.sendResponseHeaders(404, msg.length);
                exchange.getResponseBody().write(msg);
                exchange.getResponseBody().close();
            }
        }

        /* Write file bytes to response */
        private void serveFile(HttpExchange exchange, File file, String mimeType) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            byte[] data = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, data.length);
            OutputStream os = exchange.getResponseBody();
            os.write(data);
            os.close();
        }

        /* Return MIME type based on file extension */
        private String getMimeType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".gif")) return "image/gif";
            if (path.endsWith(".webp")) return "image/webp";
            if (path.endsWith(".svg")) return "image/svg+xml";
            if (path.endsWith(".ico")) return "image/x-icon";
            return "application/octet-stream";
        }
    }
}
