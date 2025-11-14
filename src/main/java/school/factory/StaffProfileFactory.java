package school.factory;

public class StaffProfileFactory implements ProfileFactory {
    private String staffId;
    private String name;
    private String department;
    private String position;

    public StaffProfileFactory(String staffId, String name, String department, String position) {
        this.staffId = staffId;
        this.name = name;
        this.department = department;
        this.position = position;
    }

    @Override
    public Profile createProfile() {
        return new StaffProfile(staffId, name, department, position);
    }
}
