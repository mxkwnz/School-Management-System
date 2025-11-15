package school;

import school.adapter.NumericGrade;
import school.decorator.User;
import school.factory.Profile;
import school.builder.Timetable;

public interface SchoolManagementService {

    Profile enrollStudent(String id, String name, String major);
    Profile hireStaff(String id, String name, String dept, String position);

    Timetable createStudentTimetable(String studentName, String year, String trimester);
    Timetable createCustomTimetable(String year, String trimester);

    void updateGrade(String studentName, NumericGrade oldGrade, int newScore);

    void registerGradeObserver(String observerType);
    void notifyGradeChange(String studentName, int oldScore, int newScore);

    double calculateAttendancePercentage(String studentName, int presentDays, int totalDays);
    boolean checkAttendancePassFail(String studentName, int presentDays, int totalDays);

    User createUserWithRole(String baseType, String... roles);
    void displayUserAccess(User user);

    void completeStudentRegistration(String id, String name, String major, String year, String trimester);
    void completeStaffOnboarding(String id, String name, String dept, String position, String year, String trimester);

    void demonstrateCompleteSystem();

    Timetable createPartTimeTimetable(String year, String trimester);
}

