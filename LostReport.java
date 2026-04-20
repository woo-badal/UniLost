public class LostReport {
    private int reportId;
    private String enrollmentId;
    private String itemName;
    private String category;
    private String description;
    private String location;
    private String dateLost;

    /* Default constructor */
    public LostReport() {}

    /* Parameterized constructor */
    public LostReport(int reportId, String enrollmentId, String itemName, String category,
                      String description, String location, String dateLost) {
        this.reportId = reportId;
        this.enrollmentId = enrollmentId;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.location = location;
        this.dateLost = dateLost;
    }

    /* Getters and setters */
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDateLost() { return dateLost; }
    public void setDateLost(String dateLost) { this.dateLost = dateLost; }
}
