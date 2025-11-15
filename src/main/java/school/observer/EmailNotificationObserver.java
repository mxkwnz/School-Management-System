package school.observer;

import school.service.EmailNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationObserver implements GradeObserver {

    private final EmailNotificationService emailService;
    private String[] emailAddresses;
    private String[] recipientNames;

    @Autowired
    public EmailNotificationObserver(EmailNotificationService emailService) {
        this.emailService = emailService;
        this.emailAddresses = new String[]{
            "kalen.ali2007@gmail.com",
            "abdullayevshynggys@gmail.com"
        };
        this.recipientNames = new String[]{
            "Muhammedali Kalen",
            "Shynggys Abdullayev"
        };
    }

    public EmailNotificationObserver(EmailNotificationService emailService, String[] emailAddresses, String[] recipientNames) {
        this.emailService = emailService;
        this.emailAddresses = emailAddresses;
        this.recipientNames = recipientNames;
    }

    @Override
    public void update(String studentName, int oldScore, int newScore) {
        emailService.sendGradeUpdateEmailToMultiple(emailAddresses, recipientNames, studentName, "General", oldScore, newScore);
    }

    public void setEmailAddresses(String[] emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public String[] getEmailAddresses() {
        return emailAddresses;
    }
}
