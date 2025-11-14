package school.builder;

public class TimetableEntry {
    private String subject;
    private String teacher;
    private String room;
    private TimeSlot timeSlot;

    public TimetableEntry(String subject, String teacher, String room, TimeSlot timeSlot) {
        this.subject = subject;
        this.teacher = teacher;
        this.room = room;
        this.timeSlot = timeSlot;
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getRoom() {
        return room;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    @Override
    public String toString() {
        return String.format("Subject: %s | Teacher: %s | Room: %s | Time: %s",
                subject, teacher, room, timeSlot);
    }
}
