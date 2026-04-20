public class ClaimRequest {
    private int claimId;
    private int itemId;
    private String enrollmentId;
    private String hostelRoom;
    private String idCardDetails;
    private String status; // Pending, Approved, Rejected

    /* Default constructor */
    public ClaimRequest() {}

    /* Parameterized constructor */
    public ClaimRequest(int claimId, int itemId, String enrollmentId,
                        String hostelRoom, String idCardDetails, String status) {
        this.claimId = claimId;
        this.itemId = itemId;
        this.enrollmentId = enrollmentId;
        this.hostelRoom = hostelRoom;
        this.idCardDetails = idCardDetails;
        this.status = status;
    }

    /* Getters and setters */
    public int getClaimId() { return claimId; }
    public void setClaimId(int claimId) { this.claimId = claimId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getHostelRoom() { return hostelRoom; }
    public void setHostelRoom(String hostelRoom) { this.hostelRoom = hostelRoom; }

    public String getIdCardDetails() { return idCardDetails; }
    public void setIdCardDetails(String idCardDetails) { this.idCardDetails = idCardDetails; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
