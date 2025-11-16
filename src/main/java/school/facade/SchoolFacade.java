package school.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import school.SchoolManagementService;
import school.adapter.GradeAdapter;
import school.adapter.NumericGrade;
import school.decorator.*;
import school.model.Attendance;
import school.model.Grade;
import school.observer.GradeNotifier;
import school.observer.ParentObserver;
import school.observer.StudentObserver;
import school.observer.EmailNotificationObserver;
import school.factory.*;
import school.builder.*;
import school.repository.AttendanceRepository;
import school.repository.GradeRepository;
import school.repository.NotificationRepository;
import school.repository.ParentStudentRepository;
import school.repository.UserRepository;
import school.repository.SubjectRepository;
import school.repository.MessageRepository;
import school.model.Notification;
import school.model.ParentStudent;
import school.model.Subject;
import school.model.Message;
import school.service.AuthenticationService;
import school.strategy.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SchoolFacade implements SchoolManagementService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private ParentStudentRepository parentStudentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private EmailNotificationObserver emailNotificationObserver;
    
    private GradeNotifier gradeNotifier = new GradeNotifier();
    private TimetableDirector timetableDirector = new TimetableDirector();
    private StudentObserver studentObserver = new StudentObserver();
    private ParentObserver parentObserver = new ParentObserver();

    public Profile enrollStudent(String id, String name, String major) {
        ProfileFactory factory = new StudentProfileFactory(id, name, major);
        Profile student = factory.createProfile();
        student.displayProfile();
        return student;
    }

    public Profile hireStaff(String id, String name, String dept, String position) {
        ProfileFactory factory = new StaffProfileFactory(id, name, dept, position);
        Profile staff = factory.createProfile();
        staff.displayProfile();
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
        
        NumericGrade numericGrade = new NumericGrade(newScore);
        numericGrade.showScore();
        new GradeAdapter(numericGrade).adaptGrade();
        
        Optional<school.model.User> studentOptional = userRepository.findByUserId(studentName);
        if (studentOptional.isPresent()) {
            school.model.User student = studentOptional.get();
            String subject = "General";
            
            Optional<Grade> gradeOptional = gradeRepository.findByStudentIdAndSubject(student.getUserId(), subject);
            Grade grade = gradeOptional.orElse(new Grade(student.getUserId(), studentName, subject, newScore));
            grade.setScore(newScore);
            gradeRepository.save(grade);
        }
        
        gradeNotifier.setGrade(studentName, oldScore, newScore);
        createGradeNotification(studentName, oldScore, newScore);
    }

    public void updateGradeForSubject(String studentUserId, String subject, NumericGrade oldGrade, int newScore) {
        int oldScore = oldGrade.getScore();

        NumericGrade numericGrade = new NumericGrade(newScore);
        numericGrade.showScore();
        new GradeAdapter(numericGrade).adaptGrade();

        Optional<school.model.User> studentOptional = userRepository.findByUserId(studentUserId);
        if (studentOptional.isPresent()) {
            school.model.User student = studentOptional.get();

            String effectiveSubject = (subject == null || subject.isEmpty()) ? "General" : subject;

            Long subjectId = null;
            if (subject != null && !subject.isEmpty()) {
                Subject subjectEntity = subjectRepository.findByCode(subject)
                        .orElseGet(() -> subjectRepository.save(new Subject(subject, subject)));
                subjectId = subjectEntity.getId();
            }

            Optional<Grade> gradeOptional = gradeRepository.findByStudentIdAndSubject(student.getUserId(), effectiveSubject);
            Grade grade = gradeOptional.orElse(
                    subjectId != null
                            ? new Grade(student.getUserId(), student.getName(), effectiveSubject, subjectId, newScore)
                            : new Grade(student.getUserId(), student.getName(), effectiveSubject, newScore)
            );
            grade.setScore(newScore);
            if (subjectId != null) {
                grade.setSubjectId(subjectId);
            }
            gradeRepository.save(grade);
        }

        gradeNotifier.setGrade(studentUserId, oldScore, newScore);
        createGradeNotification(studentUserId, oldScore, newScore);
    }
    
    private void createGradeNotification(String studentName, int oldScore, int newScore) {
        String studentMessage = String.format("Your grade has been updated from %d to %d", oldScore, newScore);
        Notification studentNotification = new Notification(studentName, studentMessage, "GRADE_UPDATE");
        notificationRepository.save(studentNotification);
        
        List<ParentStudent> parentRelations = parentStudentRepository.findByStudentUserId(studentName);
        for (ParentStudent relation : parentRelations) {
            String parentMessage = String.format("Your child %s's grade has been updated from %d to %d", studentName, oldScore, newScore);
            Notification parentNotification = new Notification(relation.getParentUserId(), parentMessage, "GRADE_UPDATE");
            notificationRepository.save(parentNotification);
        }
    }

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
        enrollStudent(id, name, major);

        String email = id + "@school.edu";
        String password = "Test123!";
        String[] nameParts = name.split(" ", 2);
        String firstName = nameParts.length > 0 ? nameParts[0] : name;
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        try {
            authenticationService.register(id, firstName, lastName, email, password, "student", major, null, null);
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
        }

        gradeNotifier.addObserver(studentObserver);
        gradeNotifier.addObserver(parentObserver);
        if (emailNotificationObserver != null) {
            gradeNotifier.addObserver(emailNotificationObserver);
        }

        createStudentTimetable(name, year, trimester);
    }

    public void completeStaffOnboarding(String id, String name, String dept, String position, String year, String trimester) {
        hireStaff(id, name, dept, position);

        String email = id + "@school.edu";
        String password = "Test123!";
        String[] nameParts = name.split(" ", 2);
        String firstName = nameParts.length > 0 ? nameParts[0] : name;
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        try {
            authenticationService.register(id, firstName, lastName, email, password, "teacher", null, dept, position);
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
        }

        Timetable timetable = new ConcreteTimetableBuilder()
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

        timetable.displayTimetable();
    }

    public double calculateAttendancePercentage(String studentName, int presentDays, int totalDays) {
        Optional<school.model.User> studentOptional = userRepository.findByUserId(studentName);
        if (studentOptional.isPresent()) {
            school.model.User student = studentOptional.get();
            long actualPresent = attendanceRepository.countByStudentIdAndPresentTrue(student.getUserId());
            long actualTotal = attendanceRepository.countByStudentId(student.getUserId());
            if (actualTotal > 0) {
                presentDays = (int) actualPresent;
                totalDays = (int) actualTotal;
            }
        }

        AttendanceStrategy strategy = new PercentageAttendanceStrategy();
        AttendanceCalculator calculator = new AttendanceCalculator(strategy);
        return calculator.calculateAttendance(presentDays, totalDays);
    }

    public double calculateAttendancePercentageBySubject(String studentUserId, Long subjectId) {
        long actualPresent = attendanceRepository.countByStudentIdAndSubjectIdAndPresentTrue(studentUserId, subjectId);
        long actualTotal = attendanceRepository.countByStudentIdAndSubjectId(studentUserId, subjectId);
        if (actualTotal == 0) {
            return 0.0;
        }

        AttendanceStrategy strategy = new PercentageAttendanceStrategy();
        AttendanceCalculator calculator = new AttendanceCalculator(strategy);
        return calculator.calculateAttendance((int) actualPresent, (int) actualTotal);
    }

    public boolean checkAttendancePassFail(String studentName, int presentDays, int totalDays) {
        AttendanceStrategy strategy = new PassFailAttendanceStrategy();
        AttendanceCalculator calculator = new AttendanceCalculator(strategy);
        double result = calculator.calculateAttendance(presentDays, totalDays);
        return result >= 100.0;
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

    public List<Grade> getUserGrades(String userId) {
        return gradeRepository.findByStudentId(userId);
    }

    public List<Attendance> getUserAttendance(String userId) {
        return attendanceRepository.findByStudentId(userId);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public void recordAttendance(String studentId, String studentName, LocalDate date, boolean present, String subject) {
        Long subjectId = null;
        if (subject != null && !subject.isEmpty()) {
            Subject subjectEntity = subjectRepository.findByCode(subject)
                    .orElseGet(() -> subjectRepository.save(new Subject(subject, subject)));
            subjectId = subjectEntity.getId();
        }

        Attendance attendance = new Attendance(studentId, studentName, date, present, subject, subjectId);
        attendanceRepository.save(attendance);
        
        createAttendanceNotification(studentId, studentName, date, present, subject);
    }
    
    private void createAttendanceNotification(String studentId, String studentName, LocalDate date, boolean present, String subject) {
        String studentMessage = present 
            ? String.format("You were marked present for %s on %s", subject, date.toString())
            : String.format("You were marked ABSENT for %s on %s", subject, date.toString());
        
        Notification studentNotification = new Notification(studentId, studentMessage, "ATTENDANCE");
        notificationRepository.save(studentNotification);
        
        List<ParentStudent> parentRelations = parentStudentRepository.findByStudentUserId(studentId);
        for (ParentStudent relation : parentRelations) {
            String parentMessage = present
                ? String.format("Your child %s was marked present for %s on %s", studentName, subject, date.toString())
                : String.format("Your child %s was marked ABSENT for %s on %s", studentName, subject, date.toString());
            
            Notification parentNotification = new Notification(relation.getParentUserId(), parentMessage, "ATTENDANCE");
            notificationRepository.save(parentNotification);
        }
    }

    public List<school.model.User> getAllStudents() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("STUDENT")))
                .collect(Collectors.toList());
    }

    public List<school.model.User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<school.model.User> getAllStaff() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("TEACHER") || role.getName().equals("ADMIN") || role.getName().equals("ADVISOR")))
                .collect(Collectors.toList());
    }

    public void deleteUser(String userId) {
        Optional<school.model.User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isPresent()) {
            userRepository.delete(userOptional.get());
        }
    }

    public school.model.User updateUser(String userId, String firstName, String lastName, String email, String major, String department, String position) {
        Optional<school.model.User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isPresent()) {
            school.model.User user = userOptional.get();
            if (firstName != null) user.setFirstName(firstName);
            if (lastName != null) user.setLastName(lastName);
            if (email != null) user.setEmail(email);
            if (major != null) user.setMajor(major);
            if (department != null) user.setDepartment(department);
            if (position != null) user.setPosition(position);
            return userRepository.save(user);
        }
        return null;
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    public void markNotificationAsRead(Long notificationId) {
        Optional<Notification> notificationOptional = notificationRepository.findById(notificationId);
        if (notificationOptional.isPresent()) {
            Notification notification = notificationOptional.get();
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    public void linkParentToStudent(String parentUserId, String studentUserId) {
        Optional<ParentStudent> existing = parentStudentRepository.findByParentUserIdAndStudentUserId(parentUserId, studentUserId);
        if (!existing.isPresent()) {
            ParentStudent relation = new ParentStudent(parentUserId, studentUserId);
            parentStudentRepository.save(relation);
        }
    }

    public List<String> getStudentIdsForParent(String parentUserId) {
        return parentStudentRepository.findByParentUserId(parentUserId).stream()
                .map(ParentStudent::getStudentUserId)
                .collect(Collectors.toList());
    }

    public Message sendMessage(Long senderUserPk, Long receiverUserPk, String content) {
        Message message = new Message(senderUserPk, receiverUserPk, content);
        return messageRepository.save(message);
    }

    public java.util.List<Message> getConversation(Long userPkA, Long userPkB) {
        java.util.List<Message> aToB = messageRepository.findBySenderUserPkAndReceiverUserPkOrderByCreatedAtAsc(userPkA, userPkB);
        java.util.List<Message> bToA = messageRepository.findBySenderUserPkAndReceiverUserPkOrderByCreatedAtAsc(userPkB, userPkA);

        return java.util.stream.Stream.concat(aToB.stream(), bToA.stream())
                .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
                .collect(java.util.stream.Collectors.toList());
    }
}
