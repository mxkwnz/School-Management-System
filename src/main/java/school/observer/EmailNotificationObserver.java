package school.observer;

import school.service.EmailNotificationService;
import school.repository.UserRepository;
import school.repository.ParentStudentRepository;
import school.model.User;
import school.model.ParentStudent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmailNotificationObserver implements GradeObserver {

    private final EmailNotificationService emailService;
    private final UserRepository userRepository;
    private final ParentStudentRepository parentStudentRepository;

    @Autowired
    public EmailNotificationObserver(EmailNotificationService emailService, 
                                     UserRepository userRepository,
                                     ParentStudentRepository parentStudentRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.parentStudentRepository = parentStudentRepository;
    }

    @Override
    public void update(String studentName, int oldScore, int newScore) {
        try {
            Optional<User> studentOpt = userRepository.findByUserId(studentName);
            if (studentOpt.isPresent()) {
                User student = studentOpt.get();
                String studentEmail = student.getEmail();
                String studentFullName = student.getFirstName() + " " + student.getLastName();
                
                emailService.sendGradeUpdateEmail(studentEmail, studentFullName, studentName, "General", oldScore, newScore);
            }
            
            java.util.List<ParentStudent> parentRelations = parentStudentRepository.findByStudentUserId(studentName);
            for (ParentStudent relation : parentRelations) {
                Optional<User> parentOpt = userRepository.findByUserId(relation.getParentUserId());
                if (parentOpt.isPresent()) {
                    User parent = parentOpt.get();
                    String parentEmail = parent.getEmail();
                    String parentFullName = parent.getFirstName() + " " + parent.getLastName();
                    
                    emailService.sendGradeUpdateEmail(parentEmail, parentFullName, studentName, "General", oldScore, newScore);
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending email notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
