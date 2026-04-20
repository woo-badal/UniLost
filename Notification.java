public class Notification {
    private int notifId;
    private String enrollmentId;
    private String message;
    private boolean isRead;

    /* Default constructor */
    public Notification() {}

    /* Parameterized constructor */
    public Notification(int notifId, String enrollmentId, String message, boolean isRead) {
        this.notifId = notifId;
        this.enrollmentId = enrollmentId;
        this.message = message;
        this.isRead = isRead;
    }

    /* Getters and setters */
    public int getNotifId() { return notifId; }
    public void setNotifId(int notifId) { this.notifId = notifId; }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
