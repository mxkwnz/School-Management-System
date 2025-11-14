package school.facade;

import school.adapter.GradeAdapter;
import school.adapter.NumericGrade;
import school.observer.GradeNotifier;
import school.observer.ParentObserver;
import school.observer.StudentObserver;
import school.factory.*;
import school.builder.*;
import school.strategy.*;

public class SchoolFacade {
    private GradeNotifier gradeNotifier = new GradeNotifier();
    private TimetableDirector timetableDirector = new TimetableDirector();

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

    public Timetable createCustomTimetable(String year, String semester) {
        return new ConcreteTimetableBuilder()
                .setAcademicYear(year)
                .setSemester(semester)
                .build();
    }

    public void updateSubject(String studentName, NumericGrade oldGrade, int newScore) {
        int oldScore = oldGrade.getScore();
        NumericGrade newGrade = new NumericGrade(newScore);
        new GradeAdapter(newGrade).adaptGrade();
        gradeNotifier.setGrade(studentName, oldScore, newScore);
    }

    public void completeStudentRegistration(String id, String name, String major, String year, String semester) {
        System.out.println("Student Registration Process");

        Profile student = enrollStudent(id, name, major);

        gradeNotifier.addObserver(new ParentObserver());
        System.out.println("Parent notifications enabled.");

        Timetable timetable = createStudentTimetable(name, year, semester);
        timetable.displayTimetable();

        System.out.println("Registration complete.");
    }

    public void completeStaffOnboarding(String id, String name, String dept, String position, String year, String semester) {
        System.out.println("Staff Onboarding Process");

        Profile staff = hireStaff(id, name, dept, position);

        Timetable schedule = new ConcreteTimetableBuilder()
                .setAcademicYear(year)
                .setSemester(semester)
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

    public void demonstrateCompleteSystem() {
        System.out.println("System Demo Starting");

        completeStudentRegistration(
                "S2024100",
                "Emma Wilson",
                "Computer Science",
                "2024-2025",
                "Fall"
        );

        System.out.println("\nUpdating Grade");
        updateSubject("Emma Wilson", new NumericGrade(75), 88);

        System.out.println("\nStrategy Pattern Integration");
        calculateAttendancePercentage("Emma Wilson", 18, 20);
        checkAttendancePassFail("Emma Wilson", 18, 20);

        System.out.println("\nDemo Finished.");
    }
}
