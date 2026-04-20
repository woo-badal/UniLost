public class Student {
    private String enrollmentId;
    private String name;
    private String password;
    private String hostelRoom;

    /* Default constructor */
    public Student() {}

    /* Parameterized constructor */
    public Student(String enrollmentId, String name, String password, String hostelRoom) {
        this.enrollmentId = enrollmentId;
        this.name = name;
        this.password = password;
        this.hostelRoom = hostelRoom;
    }

    /* Getters and setters */
    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getHostelRoom() { return hostelRoom; }
    public void setHostelRoom(String hostelRoom) { this.hostelRoom = hostelRoom; }
}
