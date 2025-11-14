package school.factory;

public class StudentProfileFactory implements ProfileFactory {
    private String studentId;
    private String name;
    private String major;

    public StudentProfileFactory(String studentId, String name, String major) {
        this.studentId = studentId;
        this.name = name;
        this.major = major;
    }

    @Override
    public Profile createProfile() {
        return new StudentProfile(studentId, name, major);
    }
}
