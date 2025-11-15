package school.builder;

public class TimetableDirector {
    public Timetable constructFullTimeTimetable(TimetableBuilder builder) {
        return builder
                .setAcademicYear("2025-2026")
                .setTrimester("first")
                .addSubject("Object-Oriented Programming")
                .addTeacher("Dr. Aibek")
                .addRoom("C1.1.366")
                .addTimeSlot("Monday", "09:00", "10:30")
                .addSubject("Data Structures")
                .addTeacher("Prof. Rakhman")
                .addRoom("C1.2.254")
                .addTimeSlot("Tuesday", "11:00", "12:30")
                .addSubject("Database Systems")
                .addTeacher("Dr. Alisher")
                .addRoom("C1.1.233")
                .addTimeSlot("Wednesday", "14:00", "15:30")
                .build();
    }

    public Timetable constructPartTimeTimetable(TimetableBuilder builder) {
        return builder
                .setAcademicYear("2024-2025")
                .setTrimester("second")
                .addSubject("Web Development")
                .addTeacher("Prof. Ali")
                .addRoom("C1.1.235")
                .addTimeSlot("Saturday", "10:00", "13:00")
                .build();
    }

    public Timetable constructCustomTimetable(
            TimetableBuilder builder,
            String year,
            String trimester
    ) {
        return builder
                .setAcademicYear(year)
                .setTrimester(trimester)
                .build();
    }
}
