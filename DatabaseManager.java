import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:lostandfound.db";

    /* Initialize database and create all tables */
    public static void initDatabase() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();

            /* Create students table */
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                "enrollmentId TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "hostelRoom TEXT" +
            ")");

            /* Create staff table */ // NEW
            stmt.execute("CREATE TABLE IF NOT EXISTS staff (" + // NEW
                "staffId TEXT PRIMARY KEY," + // NEW
                "name TEXT NOT NULL," + // NEW
                "password TEXT NOT NULL" + // NEW
            ")"); // NEW

            /* Create items table */
            stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                "itemId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "description TEXT," +
                "location TEXT," +
                "dateFound TEXT," +
                "imagePath TEXT," +
                "status TEXT DEFAULT 'Available'," +
                "claimedBy TEXT" +
            ")");

            /* Create lost_reports table */
            stmt.execute("CREATE TABLE IF NOT EXISTS lost_reports (" +
                "reportId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "enrollmentId TEXT NOT NULL," +
                "itemName TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "description TEXT," +
                "location TEXT," +
                "dateLost TEXT" +
            ")");

            /* Create claim_requests table */
            stmt.execute("CREATE TABLE IF NOT EXISTS claim_requests (" +
                "claimId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "itemId INTEGER NOT NULL," +
                "enrollmentId TEXT NOT NULL," +
                "hostelRoom TEXT," +
                "idCardDetails TEXT," +
                "status TEXT DEFAULT 'Pending'" +
            ")");

            /* Create notifications table */
            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                "notifId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "enrollmentId TEXT NOT NULL," +
                "message TEXT NOT NULL," +
                "isRead INTEGER DEFAULT 0" +
            ")");

            /* Insert demo students if not exist */
            stmt.execute("INSERT OR IGNORE INTO students (enrollmentId, name, password, hostelRoom) VALUES " +
                "('E21CSE001','Aarav Sharma','student123','H1-201')," +
                "('E21CSE002','Priya Patel','student123','H2-305')," +
                "('E21CSE003','Rahul Verma','student123','H1-108')");

            stmt.close();
            conn.close();
            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            System.out.println("DB init error: " + e.getMessage());
        }
    }

    /* Get a database connection */
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /* Authenticate student by enrollmentId and password */
    public static Student authenticateStudent(String enrollmentId, String password) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM students WHERE enrollmentId=? AND password=?");
            ps.setString(1, enrollmentId);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Student s = new Student(
                    rs.getString("enrollmentId"),
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getString("hostelRoom")
                );
                rs.close(); ps.close(); conn.close();
                return s;
            }
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Auth error: " + e.getMessage());
        }
        return null;
    }

    /* Get student by enrollmentId */
    public static Student getStudentById(String enrollmentId) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM students WHERE enrollmentId=?");
            ps.setString(1, enrollmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Student s = new Student(
                    rs.getString("enrollmentId"),
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getString("hostelRoom")
                );
                rs.close(); ps.close(); conn.close();
                return s;
            }
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Get student error: " + e.getMessage());
        }
        return null;
    }

    /* NEW - Register a new student */
    public static boolean registerStudent(String enrollmentId, String name, String password, String hostelRoom) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO students (enrollmentId, name, password, hostelRoom) VALUES (?,?,?,?)");
            ps.setString(1, enrollmentId);
            ps.setString(2, name);
            ps.setString(3, password);
            ps.setString(4, hostelRoom);
            ps.executeUpdate();
            ps.close(); conn.close();
            return true;
        } catch (Exception e) {
            System.out.println("Register student error: " + e.getMessage());
            return false;
        }
    }

    /* NEW - Authenticate staff by staffId and password from DB */
    public static Staff authenticateStaff(String staffId, String password) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM staff WHERE staffId=? AND password=?");
            ps.setString(1, staffId);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Staff s = new Staff(
                    rs.getString("staffId"),
                    rs.getString("name"),
                    rs.getString("password")
                );
                rs.close(); ps.close(); conn.close();
                return s;
            }
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Auth staff error: " + e.getMessage());
        }
        return null;
    }

    /* NEW - Get staff by staffId */
    public static Staff getStaffById(String staffId) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM staff WHERE staffId=?");
            ps.setString(1, staffId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Staff s = new Staff(
                    rs.getString("staffId"),
                    rs.getString("name"),
                    rs.getString("password")
                );
                rs.close(); ps.close(); conn.close();
                return s;
            }
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Get staff error: " + e.getMessage());
        }
        return null;
    }

    /* NEW - Register a new staff member */
    public static boolean registerStaff(String staffId, String name, String password) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO staff (staffId, name, password) VALUES (?,?,?)");
            ps.setString(1, staffId);
            ps.setString(2, name);
            ps.setString(3, password);
            ps.executeUpdate();
            ps.close(); conn.close();
            return true;
        } catch (Exception e) {
            System.out.println("Register staff error: " + e.getMessage());
            return false;
        }
    }

    /* Insert a new found item */
    public static int insertItem(String name, String category, String description,
                                  String location, String dateFound, String imagePath) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO items (name,category,description,location,dateFound,imagePath,status) VALUES (?,?,?,?,?,?,'Available')",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setString(3, description);
            ps.setString(4, location);
            ps.setString(5, dateFound);
            ps.setString(6, imagePath);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = -1;
            if (keys.next()) id = keys.getInt(1);
            keys.close(); ps.close(); conn.close();
            return id;
        } catch (Exception e) {
            System.out.println("Insert item error: " + e.getMessage());
        }
        return -1;
    }

    /* Get all items with optional category and location filter */
    public static List<Item> getAllItems(String category, String location) {
        List<Item> items = new ArrayList<Item>();
        try {
            Connection conn = getConnection();
            String sql = "SELECT * FROM items WHERE 1=1";
            if (category != null && !category.isEmpty()) sql += " AND category=?";
            if (location != null && !location.isEmpty()) sql += " AND location LIKE ?";
            sql += " ORDER BY itemId DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            int idx = 1;
            if (category != null && !category.isEmpty()) { ps.setString(idx++, category); }
            if (location != null && !location.isEmpty()) { ps.setString(idx++, "%" + location + "%"); }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new Item(
                    rs.getInt("itemId"), rs.getString("name"), rs.getString("category"),
                    rs.getString("description"), rs.getString("location"),
                    rs.getString("dateFound"), rs.getString("imagePath"),
                    rs.getString("status"), rs.getString("claimedBy")
                ));
            }
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Get items error: " + e.getMessage());
        }
        return items;
    }

    /* Get a single item by itemId */
    public static Item getItemById(int itemId) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM items WHERE itemId=?");
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Item item = new Item(
                    rs.getInt("itemId"), rs.getString("name"), rs.getString("category"),
                    rs.getString("description"), rs.getString("location"),
                    rs.getString("dateFound"), rs.getString("imagePath"),
                    rs.getString("status"), rs.getString("claimedBy")
                );
                rs.close(); ps.close(); conn.close();
                return item;
            }
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Get item error: " + e.getMessage());
        }
        return null;
    }

    /* Update item status */
    public static void updateItemStatus(int itemId, String status, String claimedBy) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE items SET status=?, claimedBy=? WHERE itemId=?");
            ps.setString(1, status);
            ps.setString(2, claimedBy);
            ps.setInt(3, itemId);
            ps.executeUpdate();
            ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Update item error: " + e.getMessage());
        }
    }

    /* Delete an item by itemId */
    public static void deleteItem(int itemId) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM items WHERE itemId=?");
            ps.setInt(1, itemId);
            ps.executeUpdate();
            ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Delete item error: " + e.getMessage());
        }
    }

    /* Insert a lost report */
    public static void insertLostReport(String enrollmentId, String itemName, String category,
                                         String description, String location, String dateLost) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO lost_reports (enrollmentId,itemName,category,description,location,dateLost) VALUES (?,?,?,?,?,?)");
            ps.setString(1, enrollmentId);
            ps.setString(2, itemName);
            ps.setString(3, category);
            ps.setString(4, description);
            ps.setString(5, location);
            ps.setString(6, dateLost);
            ps.executeUpdate();
            ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Insert lost report error: " + e.getMessage());
        }
    }

    /* Get all lost reports */
    public static List<LostReport> getAllLostReports() {
        List<LostReport> reports = new ArrayList<LostReport>();
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM lost_reports ORDER BY reportId DESC");
            while (rs.next()) {
                reports.add(new LostReport(
                    rs.getInt("reportId"), rs.getString("enrollmentId"),
                    rs.getString("itemName"), rs.getString("category"),
                    rs.getString("description"), rs.getString("location"),
                    rs.getString("dateLost")
                ));
            }
            rs.close(); stmt.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Get lost reports error: " + e.getMessage());
        }
        return reports;
    }

    /* Get lost reports by enrollmentId */
    public static List<LostReport> getLostReportsByStudent(String enrollmentId) {
        List<LostReport> reports = new ArrayList<LostReport>();
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM lost_reports WHERE enrollmentId=? ORDER BY reportId DESC");
            ps.setString(1, enrollmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reports.add(new LostReport(
                    rs.getInt("reportId"), rs.getString("enrollmentId"),
                    rs.getString("itemName"), rs.getString("category"),
                    rs.getString("description"), rs.getString("location"),
                    rs.getString("dateLost")
                ));
            }
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Get lost reports by student error: " + e.getMessage());
        }
        return reports;
    }

    /* Find lost reports matching a category for notification matching */
    public static List<LostReport> getLostReportsByCategory(String category) {
        List<LostReport> reports = new ArrayList<LostReport>();
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM lost_reports WHERE category=?");
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reports.add(new LostReport(
                    rs.getInt("reportId"), rs.getString("enrollmentId"),
                    rs.getString("itemName"), rs.getString("category"),
                    rs.getString("description"), rs.getString("location"),
                    rs.getString("dateLost")
                ));
            }
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Get lost reports by category error: " + e.getMessage());
        }
        return reports;
    }

    /* Insert a claim request */
    public static int insertClaim(int itemId, String enrollmentId, String hostelRoom, String idCardDetails) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO claim_requests (itemId,enrollmentId,hostelRoom,idCardDetails,status) VALUES (?,?,?,?,'Pending')",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, itemId);
            ps.setString(2, enrollmentId);
            ps.setString(3, hostelRoom);
            ps.setString(4, idCardDetails);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = -1;
            if (keys.next()) id = keys.getInt(1);
            keys.close(); ps.close(); conn.close();
            return id;
        } catch (Exception e) {
            System.out.println("Insert claim error: " + e.getMessage());
        }
        return -1;
    }

    /* Get all claim requests (for staff) */
    public static List<ClaimRequest> getAllClaims() {
        List<ClaimRequest> claims = new ArrayList<ClaimRequest>();
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM claim_requests ORDER BY claimId DESC");
            while (rs.next()) {
                claims.add(new ClaimRequest(
                    rs.getInt("claimId"), rs.getInt("itemId"),
                    rs.getString("enrollmentId"), rs.getString("hostelRoom"),
                    rs.getString("idCardDetails"), rs.getString("status")
                ));
            }
            rs.close(); stmt.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Get claims error: " + e.getMessage());
        }
        return claims;
    }

    /* Get claim requests by student enrollmentId */
    public static List<ClaimRequest> getClaimsByStudent(String enrollmentId) {
        List<ClaimRequest> claims = new ArrayList<ClaimRequest>();
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM claim_requests WHERE enrollmentId=? ORDER BY claimId DESC");
            ps.setString(1, enrollmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                claims.add(new ClaimRequest(
                    rs.getInt("claimId"), rs.getInt("itemId"),
                    rs.getString("enrollmentId"), rs.getString("hostelRoom"),
                    rs.getString("idCardDetails"), rs.getString("status")
                ));
            }
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Get claims by student error: " + e.getMessage());
        }
        return claims;
    }

    /* Update claim status by claimId */
    public static void updateClaimStatus(int claimId, String status) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE claim_requests SET status=? WHERE claimId=?");
            ps.setString(1, status);
            ps.setInt(2, claimId);
            ps.executeUpdate();
            ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Update claim error: " + e.getMessage());
        }
    }

    /* Get claim request by claimId */
    public static ClaimRequest getClaimById(int claimId) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM claim_requests WHERE claimId=?");
            ps.setInt(1, claimId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ClaimRequest c = new ClaimRequest(
                    rs.getInt("claimId"), rs.getInt("itemId"),
                    rs.getString("enrollmentId"), rs.getString("hostelRoom"),
                    rs.getString("idCardDetails"), rs.getString("status")
                );
                rs.close(); ps.close(); conn.close();
                return c;
            }
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Get claim by id error: " + e.getMessage());
        }
        return null;
    }

    /* Insert a notification for a student */
    public static void insertNotification(String enrollmentId, String message) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO notifications (enrollmentId,message,isRead) VALUES (?,?,0)");
            ps.setString(1, enrollmentId);
            ps.setString(2, message);
            ps.executeUpdate();
            ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Insert notification error: " + e.getMessage());
        }
    }

    /* Get notifications for a student */
    public static List<Notification> getNotificationsByStudent(String enrollmentId) {
        List<Notification> notifs = new ArrayList<Notification>();
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM notifications WHERE enrollmentId=? ORDER BY notifId DESC");
            ps.setString(1, enrollmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                notifs.add(new Notification(
                    rs.getInt("notifId"), rs.getString("enrollmentId"),
                    rs.getString("message"), rs.getInt("isRead") == 1
                ));
            }
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Get notifications error: " + e.getMessage());
        }
        return notifs;
    }

    /* Count unread notifications for a student */
    public static int countUnreadNotifications(String enrollmentId) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM notifications WHERE enrollmentId=? AND isRead=0");
            ps.setString(1, enrollmentId);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            if (rs.next()) count = rs.getInt(1);
            rs.close(); ps.close(); conn.close();
            return count;
        } catch (Exception e) {
            System.out.println("Count notifications error: " + e.getMessage());
        }
        return 0;
    }

    /* Mark all notifications as read for a student */
    public static void markAllNotificationsRead(String enrollmentId) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE notifications SET isRead=1 WHERE enrollmentId=?");
            ps.setString(1, enrollmentId);
            ps.executeUpdate();
            ps.close(); conn.close();
        } catch (Exception e) {
            System.out.println("Mark read error: " + e.getMessage());
        }
    }

    /* Get total count of items */
    public static int countItems() {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM items");
            int count = 0;
            if (rs.next()) count = rs.getInt(1);
            rs.close(); stmt.close(); conn.close();
            return count;
        } catch (Exception e) { return 0; }
    }

    /* Get count of items with a given status */
    public static int countItemsByStatus(String status) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM items WHERE status=?");
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            if (rs.next()) count = rs.getInt(1);
            rs.close(); ps.close(); conn.close();
            return count;
        } catch (Exception e) { return 0; }
    }

    /* Get count of pending claims */
    public static int countPendingClaims() {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM claim_requests WHERE status='Pending'");
            ResultSet rs = ps.executeQuery();
            int count = 0;
            if (rs.next()) count = rs.getInt(1);
            rs.close(); ps.close(); conn.close();
            return count;
        } catch (Exception e) { return 0; }
    }

    /* Get count of all lost reports */
    public static int countLostReports() {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM lost_reports");
            int count = 0;
            if (rs.next()) count = rs.getInt(1);
            rs.close(); stmt.close(); conn.close();
            return count;
        } catch (Exception e) { return 0; }
    }

    /* Get recent items (latest 5) */
    public static List<Item> getRecentItems(int limit) {
        List<Item> items = new ArrayList<Item>();
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM items ORDER BY itemId DESC LIMIT ?");
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new Item(
                    rs.getInt("itemId"), rs.getString("name"), rs.getString("category"),
                    rs.getString("description"), rs.getString("location"),
                    rs.getString("dateFound"), rs.getString("imagePath"),
                    rs.getString("status"), rs.getString("claimedBy")
                ));
            }
            rs.close(); ps.close(); conn.close();
        } catch (Exception e) { System.out.println("Get recent items error: " + e.getMessage()); }
        return items;
    }
}
