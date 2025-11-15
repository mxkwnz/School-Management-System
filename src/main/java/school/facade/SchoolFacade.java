package school.facade;

import school.SchoolManagementService;
import school.adapter.GradeAdapter;
import school.adapter.NumericGrade;
import school.decorator.*;
import school.observer.GradeNotifier;
import school.observer.ParentObserver;
import school.observer.StudentObserver;
import school.factory.*;
import school.builder.*;
import school.strategy.*;

public class SchoolFacade implements SchoolManagementService {
    private GradeNotifier gradeNotifier = new GradeNotifier();
    private TimetableDirector timetableDirector = new TimetableDirector();

    public Profile enrollStudent(String id, String name, String major) {
        ProfileFactory factory = new StudentProfileFactory(id, name, major);
        Profile student = factory.createProfile();
        System.out.println("Student enrolled.");
        student.displayProfile();
        if (student instanceof StudentProfile) {
            StudentProfile sp = (StudentProfile) student;
            System.out.println("Profile Type: " + sp.getProfileType());
            System.out.println("Student ID: " + sp.getStudentId());
            System.out.println("Student Name: " + sp.getName());
            System.out.println("Major: " + sp.getMajor());
        }
        return student;
    }

    public Profile hireStaff(String id, String name, String dept, String position) {
        ProfileFactory factory = new StaffProfileFactory(id, name, dept, position);
        Profile staff = factory.createProfile();
        System.out.println("Staff member hired.");
        staff.displayProfile();
        if (staff instanceof StaffProfile) {
            StaffProfile sp = (StaffProfile) staff;
            System.out.println("Profile Type: " + sp.getProfileType());
            System.out.println("Staff ID: " + sp.getStaffId());
            System.out.println("Staff Name: " + sp.getName());
            System.out.println("Department: " + sp.getDepartment());
            System.out.println("Position: " + sp.getPosition());
        }
        return staff;
    }

    public Timetable createStudentTimetable(String studentName, String year, String trimester) {
        System.out.println("Creating timetable for " + studentName);
        TimetableBuilder builder = new ConcreteTimetableBuilder();
        Timetable timetable = timetableDirector.constructFullTimeTimetable(builder);
        System.out.println("Timetable created with " + timetable.getEntries().size() + " entries.");
        for (int i = 0; i < timetable.getEntries().size(); i++) {
            TimetableEntry entry = timetable.getEntries().get(i);
            System.out.println("Entry " + (i + 1) + ": " + entry.getSubject() +
                    " by " + entry.getTeacher() + " in " + entry.getRoom() +
                    " on " + entry.getTimeSlot().getDayOfWeek() +
                    " from " + entry.getTimeSlot().getStartTime() +
                    " to " + entry.getTimeSlot().getEndTime());
        }
        return timetable;
    }

    public Timetable createCustomTimetable(String year, String trimester) {
        return new ConcreteTimetableBuilder()
                .setAcademicYear(year)
                .setTrimester(trimester)
                .build();
    }

    public void updateSubject(String studentName, NumericGrade oldGrade, int newScore) {
        updateGrade(studentName, oldGrade, newScore);
    }

    @Override
    public void updateGrade(String studentName, NumericGrade oldGrade, int newScore) {
        int oldScore = oldGrade.getScore();
        NumericGrade newGrade = new NumericGrade(newScore);
        System.out.println("Original numeric grade:");
        newGrade.showScore();
        new GradeAdapter(newGrade).adaptGrade();
        gradeNotifier.setGrade(studentName, oldScore, newScore);
    }

    private StudentObserver studentObserver = new StudentObserver();
    private ParentObserver parentObserver = new ParentObserver();

    @Override
    public void registerGradeObserver(String observerType) {
        if ("student".equalsIgnoreCase(observerType)) {
            gradeNotifier.addObserver(studentObserver);
        } else if ("parent".equalsIgnoreCase(observerType)) {
            gradeNotifier.addObserver(parentObserver);
        }
    }

    @Override
    public void notifyGradeChange(String studentName, int oldScore, int newScore) {
        gradeNotifier.setGrade(studentName, oldScore, newScore);
    }

    @Override
    public User createUserWithRole(String baseType, String... roles) {
        User user = new BasicUser();

        for (String role : roles) {
            switch (role.toLowerCase()) {
                case "teacher":
                    user = new TeacherDecorator(user);
                    break;
                case "admin":
                    user = new AdminDecorator(user);
                    break;
                case "advisor":
                case "classadvisor":
                    user = new ClassAdvisorDecorator(user);
                    break;
            }
        }

        return user;
    }

    @Override
    public void displayUserAccess(User user) {
        System.out.println("=== User Access Information ===");
        System.out.println("Description: " + user.getDescription());
        System.out.println("Access Level: " + user.getAccessLevel());
        System.out.println("==============================");
    }

    public void completeStudentRegistration(String id, String name, String major, String year, String trimester) {
        System.out.println("Student Registration Process");

        Profile student = enrollStudent(id, name, major);

        gradeNotifier.addObserver(studentObserver);
        gradeNotifier.addObserver(parentObserver);
        System.out.println("Student and Parent notifications enabled.");

        Timetable timetable = createStudentTimetable(name, year, trimester);
        timetable.displayTimetable();

        System.out.println("Registration complete.");
    }

    public void completeStaffOnboarding(String id, String name, String dept, String position, String year, String trimester) {
        System.out.println("Staff Onboarding Process");

        Profile staff = hireStaff(id, name, dept, position);

        Timetable schedule = new ConcreteTimetableBuilder()
                .setAcademicYear(year)
                .setTrimester(trimester)
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

        System.out.println("Switching to Pass/Fail strategy using setStrategy()");
        calculator.setStrategy(new PassFailAttendanceStrategy());
        double passFailResult = calculator.calculateAttendance(presentDays, totalDays);

        System.out.println("Present Days: " + presentDays);
        System.out.println("Total Days: " + totalDays);
        System.out.println("Attendance: " + result + "%");
        System.out.println("Pass/Fail Result: " + passFailResult);

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

    public void demonstrateCompleteSystem() {
        System.out.println("System Demo Starting");

        System.out.println("\n=== Using enrollStudent directly ===");
        enrollStudent("S2024101", "Qalen Mukhammedali", "Software Engineer");

        System.out.println("\n=== Using hireStaff directly ===");
        hireStaff("T2024101", "Aliev Aibek", "Software Engineering", "Senior Lecturer");

        completeStudentRegistration(
                "S2024100",
                "Menbay Alisher",
                "Cybersecurity",
                "2024-2025",
                "first"
        );

        System.out.println("\n=== Using createCustomTimetable ===");
        Timetable customTimetable = createCustomTimetable("2024-2025", "second");
        customTimetable.displayTimetable();

        System.out.println("\n=== Using constructPartTimeTimetable ===");
        TimetableBuilder builder2 = new ConcreteTimetableBuilder();
        Timetable partTimeTimetable = timetableDirector.constructPartTimeTimetable(builder2);
        partTimeTimetable.displayTimetable();

        System.out.println("\n=== Using constructCustomTimetable from Director ===");
        TimetableBuilder builder3 = new ConcreteTimetableBuilder();
        Timetable directorCustomTimetable = timetableDirector.constructCustomTimetable(builder3, "2024-2025", "third");
        directorCustomTimetable.displayTimetable();

        System.out.println("\nUpdating Grade");
        updateSubject("Menbay Alisher", new NumericGrade(75), 88);

        System.out.println("\n=== Using notifyGradeChange directly ===");
        notifyGradeChange("Menbay Alisher", 88, 92);

        System.out.println("\nStrategy Pattern Integration");
        calculateAttendancePercentage("Menbay Alisher", 18, 20);
        checkAttendancePassFail("Menbay Alisher", 18, 20);

        System.out.println("\nDemo Finished.");
    }

    public Timetable createPartTimeTimetable(String year, String trimester) {
        System.out.println("Creating part-time timetable");
        TimetableBuilder builder = new ConcreteTimetableBuilder();
        Timetable timetable = timetableDirector.constructPartTimeTimetable(builder);
        System.out.println("Part-time timetable created with " + timetable.getEntries().size() + " entries.");
        return timetable;
    }
}
