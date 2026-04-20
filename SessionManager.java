import java.util.HashMap;
import java.util.UUID;

public class SessionManager {
    /* Map of sessionToken -> SessionData (userId + role) */
    private static HashMap<String, String[]> sessions = new HashMap<String, String[]>();

    /* Create a new session and return the token */
    public static String createSession(String userId, String role) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new String[]{userId, role});
        return token;
    }

    /* Get the session data array [userId, role] by token, returns null if not found */
    public static String[] getSession(String token) {
        if (token == null) return null;
        return sessions.get(token);
    }

    /* Get userId from session token */
    public static String getUserId(String token) {
        String[] data = getSession(token);
        if (data == null) return null;
        return data[0];
    }

    /* Get role from session token */
    public static String getRole(String token) {
        String[] data = getSession(token);
        if (data == null) return null;
        return data[1];
    }

    /* Remove session (logout) */
    public static void removeSession(String token) {
        sessions.remove(token);
    }

    /* Extract session token from Cookie header string */
    public static String extractTokenFromCookie(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isEmpty()) return null;
        String[] parts = cookieHeader.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("session=")) {
                return trimmed.substring("session=".length()).trim();
            }
        }
        return null;
    }
}
