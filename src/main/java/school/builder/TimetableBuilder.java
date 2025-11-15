package school.builder;

public interface TimetableBuilder {
    TimetableBuilder setTrimester(String trimester);
    TimetableBuilder setAcademicYear(String academicYear);
    TimetableBuilder addSubject(String subject);
    TimetableBuilder addTeacher(String teacher);
    TimetableBuilder addRoom(String room);
    TimetableBuilder addTimeSlot(String day, String startTime, String endTime);
    Timetable build();
}

