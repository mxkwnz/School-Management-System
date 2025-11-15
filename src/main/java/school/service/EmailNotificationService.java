package school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    @Autowired
    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendGradeUpdateEmail(String toEmail, String recipientName, String studentName, String subject, int oldScore, int newScore) {
        try {
            logger.info("📧 Preparing to send grade update email notification to: {} ({})", toEmail, recipientName);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Grade Update Notification - " + studentName);
            message.setText(buildGradeUpdateEmailBody(recipientName, studentName, subject, oldScore, newScore));

            logger.info("📧 Email details:");
            logger.info("   To: {}", toEmail);
            logger.info("   Subject: {}", message.getSubject());
            logger.info("   Student: {}", studentName);
            logger.info("   Subject: {}", subject);
            logger.info("   Old Score: {}", oldScore);
            logger.info("   New Score: {}", newScore);

            mailSender.send(message);
            logger.info("✅ Email successfully sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("❌ Failed to send email notification to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    public void sendGradeUpdateEmailToMultiple(String[] toEmails, String[] recipientNames, String studentName, String subject, int oldScore, int newScore) {
        for (int i = 0; i < toEmails.length; i++) {
            String recipientName = (i < recipientNames.length) ? recipientNames[i] : "Parent";
            sendGradeUpdateEmail(toEmails[i], recipientName, studentName, subject, oldScore, newScore);
        }
    }

    private String buildGradeUpdateEmailBody(String recipientName, String studentName, String subject, int oldScore, int newScore) {
        return String.format(
            "Hello %s,\n\n" +
            "This is a notification regarding a grade update for %s:\n\n" +
            "Student Name: %s\n" +
            "Subject: %s\n" +
            "Previous Grade: %d\n" +
            "New Grade: %d\n" +
            "Change: %+d points\n\n" +
            "Please review the updated grade in the system.\n\n" +
            "Best regards,\n" +
            "School Management System",
            recipientName, studentName, studentName, subject, oldScore, newScore, (newScore - oldScore)
        );
    }
}

