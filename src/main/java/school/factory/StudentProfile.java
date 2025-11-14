package school.factory;

public class StudentProfile implements Profile {
    private String studentId;
    private String name;
    private String major;

    public StudentProfile(String studentId, String name, String major) {
        this.studentId = studentId;
        this.name = name;
        this.major = major;
    }

    @Override
    public void displayProfile() {
        System.out.println("=== Student Profile ===");
        System.out.println("ID: " + studentId);
        System.out.println("Name: " + name);
        System.out.println("Major: " + major);
        System.out.println("Access Level: " + getAccessLevel());
    }

    @Override
    public String getProfileType() {
        return "Student";
    }

    @Override
    public int getAccessLevel() {
        return 1;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getMajor() {
        return major;
    }
}
