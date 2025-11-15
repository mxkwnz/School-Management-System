package school.facade;

import school.SchoolManagementService;
import school.adapter.GradeAdapter;
import school.adapter.NumericGrade;
import school.observer.GradeNotifier;
import school.observer.ParentObserver;
import school.observer.StudentObserver;
import school.observer.EmailNotificationObserver;
import school.factory.*;
import school.builder.*;
import school.strategy.*;
import school.decorator.*;
import school.model.Grade;
import school.repository.GradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchoolFacade implements SchoolManagementService {
    private GradeNotifier gradeNotifier = new GradeNotifier();
    private TimetableDirector timetableDirector = new TimetableDirector();
    
    @Autowired(required = false)
    private EmailNotificationObserver emailNotificationObserver;
    
    @Autowired(required = false)
    private GradeRepository gradeRepository;

    public Profile enrollStudent(String id, String name, String major) {
        ProfileFactory factory = new StudentProfileFactory(id, name, major);
        Profile student = factory.createProfile();
        gradeNotifier.addObserver(new StudentObserver());
        System.out.println("Student enrolled.");
        student.displayProfile();
        return student;
    }

    public Profile hireStaff(String id, String name, String dept, String position) {
        ProfileFactory factory = new StaffProfileFactory(id, name, dept, position);
        Profile staff = factory.createProfile();
        System.out.println("Staff member hired.");
        staff.displayProfile();
        return staff;
    }

    public Timetable createStudentTimetable(String studentName, String year, String semester) {
        System.out.println("Creating timetable for " + studentName);
        TimetableBuilder builder = new ConcreteTimetableBuilder();
        Timetable timetable = timetableDirector.constructFullTimeTimetable(builder);
        System.out.println("Timetable created.");
        return timetable;
    }

    public Timetable createCustomTimetable(String year, String trimester) {
        return new ConcreteTimetableBuilder()
                .setAcademicYear(year)
                .setTrimester(trimester)
                .build();
    }

    @Override
    public Timetable createPartTimeTimetable(String year, String trimester) {
        System.out.println("Creating part-time timetable for " + year + " - " + trimester);
        TimetableBuilder builder = new ConcreteTimetableBuilder();
        Timetable timetable = timetableDirector.constructPartTimeTimetable(builder);
        timetable.setAcademicYear(year);
        timetable.setTrimester(trimester);
        System.out.println("Part-time timetable created.");
        timetable.displayTimetable();
        return timetable;
    }

    public void updateSubject(String studentName, NumericGrade oldGrade, int newScore) {
        int oldScore = oldGrade.getScore();
        NumericGrade newGrade = new NumericGrade(newScore);
        new GradeAdapter(newGrade).adaptGrade();
        
        if (gradeRepository != null) {
            Grade grade = new Grade("", studentName, "General", newScore);
            gradeRepository.save(grade);
        }
        
        gradeNotifier.setGrade(studentName, oldScore, newScore);
    }

    @Override
    public void updateGrade(String studentName, NumericGrade oldGrade, int newScore) {
        updateSubject(studentName, oldGrade, newScore);
    }

    @Override
    public void registerGradeObserver(String observerType) {
        if ("student".equalsIgnoreCase(observerType)) {
            gradeNotifier.addObserver(new StudentObserver());
        } else if ("parent".equalsIgnoreCase(observerType)) {
            gradeNotifier.addObserver(new ParentObserver());
        }
    }

    @Override
    public void notifyGradeChange(String studentName, int oldScore, int newScore) {
        gradeNotifier.setGrade(studentName, oldScore, newScore);
    }

    public void completeStudentRegistration(String id, String name, String major, String year, String semester) {
        System.out.println("Student Registration Process");

        enrollStudent(id, name, major);

        gradeNotifier.addObserver(new ParentObserver());
        
        if (emailNotificationObserver != null) {
            gradeNotifier.addObserver(emailNotificationObserver);
            System.out.println("Email notifications enabled for:");
            for (String email : emailNotificationObserver.getEmailAddresses()) {
                System.out.println("  - " + email);
            }
        }
        
        System.out.println("Parent notifications enabled.");

        Timetable timetable = createStudentTimetable(name, year, semester);
        timetable.displayTimetable();

        System.out.println("Registration complete.");
    }

    public void completeStaffOnboarding(String id, String name, String dept, String position, String year, String semester) {
        System.out.println("Staff Onboarding Process");

        hireStaff(id, name, dept, position);

        Timetable schedule = new ConcreteTimetableBuilder()
                .setAcademicYear(year)
                .setTrimester(semester)
                .addSubject("Software Design Patterns")
                .addTeacher(name)
                .addRoom("Room 501")
                .addTimeSlot("Monday", "14:00", "16:00")
                .addSubject("Advanced Java")
                .addTeacher(name)
                .addRoom("Lab 203")
                .addTimeSlot("Thursday", "10:00", "12:00")
                .build();

        schedule.displayTimetable();

        System.out.println("Onboarding complete.");
    }

    public double calculateAttendancePercentage(String studentName, int presentDays, int totalDays) {
        System.out.println("\nCalculating Attendance Percentage for " + studentName);
        System.out.println("Using Percentage Attendance Strategy");
        
        AttendanceStrategy strategy = new PercentageAttendanceStrategy();
        AttendanceCalculator calculator = new AttendanceCalculator(strategy);
        double result = calculator.calculateAttendance(presentDays, totalDays);
        
        System.out.println("Present Days: " + presentDays);
        System.out.println("Total Days: " + totalDays);
        System.out.println("Attendance: " + result + "%");
        
        return result;
    }

    public boolean checkAttendancePassFail(String studentName, int presentDays, int totalDays) {
        System.out.println("\nChecking Attendance Pass/Fail for " + studentName);
        System.out.println("Using Pass/Fail Attendance Strategy");
        
        AttendanceStrategy strategy = new PassFailAttendanceStrategy();
        AttendanceCalculator calculator = new AttendanceCalculator(strategy);
        double result = calculator.calculateAttendance(presentDays, totalDays);
        
        boolean passed;
        if (result >= 100.0) {
            passed = true;
        } else {
            passed = false;
        }
        
        System.out.println("Present Days: " + presentDays);
        System.out.println("Total Days: " + totalDays);
        
        String status;
        if (passed) {
            status = "PASS";
        } else {
            status = "FAIL";
        }
        System.out.println("Status: " + status);
        
        return passed;
    }

    public school.decorator.User createUserWithRole(String role) {
        school.decorator.User basicUser = new BasicUser();
        System.out.println("\nDecorator Pattern - Creating User with Role: " + role);
        System.out.println("Initial: " + basicUser.getDescription() + " (Access Level: " + basicUser.getAccessLevel() + ")");
        
        school.decorator.User decoratedUser;
        switch (role.toLowerCase()) {
            case "admin":
                decoratedUser = new AdminDecorator(basicUser);
                break;
            case "teacher":
                decoratedUser = new TeacherDecorator(basicUser);
                break;
            case "classadvisor":
            case "class advisor":
                decoratedUser = new ClassAdvisorDecorator(basicUser);
                break;
            default:
                decoratedUser = basicUser;
        }
        
        System.out.println("After decoration: " + decoratedUser.getDescription() + " (Access Level: " + decoratedUser.getAccessLevel() + ")");
        return decoratedUser;
    }

    @Override
    public User createUserWithRole(String baseType, String... roles) {
        User user = new BasicUser();
        System.out.println("\nDecorator Pattern - Creating User with Multiple Roles");
        System.out.println("Initial: " + user.getDescription() + " (Access Level: " + user.getAccessLevel() + ")");
        
        for (String role : roles) {
            switch (role.toLowerCase()) {
                case "admin":
                    user = new AdminDecorator(user);
                    break;
                case "teacher":
                    user = new TeacherDecorator(user);
                    break;
                case "classadvisor":
                case "class advisor":
                    user = new ClassAdvisorDecorator(user);
                    break;
            }
            System.out.println("After adding " + role + ": " + user.getDescription() + " (Access Level: " + user.getAccessLevel() + ")");
        }
        
        return user;
    }

    @Override
    public void displayUserAccess(User user) {
        System.out.println("User Description: " + user.getDescription());
        System.out.println("Access Level: " + user.getAccessLevel());
    }

    public school.decorator.User createMultiRoleUser(String... roles) {
        school.decorator.User user = new BasicUser();
        System.out.println("\nDecorator Pattern - Creating Multi-Role User");
        System.out.println("Initial: " + user.getDescription() + " (Access Level: " + user.getAccessLevel() + ")");
        
        for (String role : roles) {
            switch (role.toLowerCase()) {
                case "admin":
                    user = new AdminDecorator(user);
                    break;
                case "teacher":
                    user = new TeacherDecorator(user);
                    break;
                case "classadvisor":
                case "class advisor":
                    user = new ClassAdvisorDecorator(user);
                    break;
            }
            System.out.println("After adding " + role + ": " + user.getDescription() + " (Access Level: " + user.getAccessLevel() + ")");
        }
        
        return user;
    }

    public void demonstrateCompleteSystem() {
        System.out.println("System Demo Starting");

        completeStudentRegistration(
                "S2024100",
                "Muhammedali Kalen",
                "Computer Science",
                "2024-2025",
                "Fall"
        );

        System.out.println("\nUpdating Grade");
        updateSubject("Muhammedali Kalen", new NumericGrade(75), 88);

        System.out.println("\nStrategy Pattern Integration");
        calculateAttendancePercentage("Muhammedali Kalen", 18, 20);
        checkAttendancePassFail("Muhammedali Kalen", 18, 20);

        System.out.println("\nDecorator Pattern Integration");
        createUserWithRole("teacher");
        createUserWithRole("admin");
        createMultiRoleUser("teacher", "class advisor");

        System.out.println("\nDemo Finished.");
    }
}
