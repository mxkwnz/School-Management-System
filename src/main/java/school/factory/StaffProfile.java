package school.factory;

public class StaffProfile implements Profile {
    private String staffId;
    private String name;
    private String department;
    private String position;

    public StaffProfile(String staffId, String name, String department, String position) {
        this.staffId = staffId;
        this.name = name;
        this.department = department;
        this.position = position;
    }

    @Override
    public void displayProfile() {
        System.out.println("=== Staff Profile ===");
        System.out.println("ID: " + staffId);
        System.out.println("Name: " + name);
        System.out.println("Department: " + department);
        System.out.println("Position: " + position);
        System.out.println("Access Level: " + getAccessLevel());
    }

    @Override
    public String getProfileType() {
        return "Staff";
    }

    @Override
    public int getAccessLevel() {
        return 3;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }
}
