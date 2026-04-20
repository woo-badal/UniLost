public class Item {
    private int itemId;
    private String name;
    private String category;
    private String description;
    private String location;
    private String dateFound;
    private String imagePath;
    private String status; // Available, Claim Pending, Claimed
    private String claimedBy;

    /* Default constructor */
    public Item() {}

    /* Parameterized constructor */
    public Item(int itemId, String name, String category, String description,
                String location, String dateFound, String imagePath, String status, String claimedBy) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.location = location;
        this.dateFound = dateFound;
        this.imagePath = imagePath;
        this.status = status;
        this.claimedBy = claimedBy;
    }

    /* Getters and setters */
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDateFound() { return dateFound; }
    public void setDateFound(String dateFound) { this.dateFound = dateFound; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getClaimedBy() { return claimedBy; }
    public void setClaimedBy(String claimedBy) { this.claimedBy = claimedBy; }
}
