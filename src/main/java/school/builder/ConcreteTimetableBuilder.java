package school.builder;

public class ConcreteTimetableBuilder implements TimetableBuilder {
    private Timetable timetable;
    private String currentSubject;
    private String currentTeacher;
    private String currentRoom;

    public ConcreteTimetableBuilder() {
        this.timetable = new Timetable();
    }

    @Override
    public TimetableBuilder setSemester(String semester) {
        timetable.setSemester(semester);
        return this;
    }

    @Override
    public TimetableBuilder setAcademicYear(String academicYear) {
        timetable.setAcademicYear(academicYear);
        return this;
    }

    @Override
    public TimetableBuilder addSubject(String subject) {
        this.currentSubject = subject;
        return this;
    }

    @Override
    public TimetableBuilder addTeacher(String teacher) {
        validateSubjectSet();
        this.currentTeacher = teacher;
        return this;
    }

    @Override
    public TimetableBuilder addRoom(String room) {
        validateTeacherSet();
        this.currentRoom = room;
        return this;
    }

    @Override
    public TimetableBuilder addTimeSlot(String day, String startTime, String endTime) {
        validateRoomSet();

        TimeSlot timeSlot = new TimeSlot(day, startTime, endTime);
        TimetableEntry entry = new TimetableEntry(
                currentSubject,
                currentTeacher,
                currentRoom,
                timeSlot
        );

        timetable.addEntry(entry);
        resetCurrentEntry();
        return this;
    }

    @Override
    public Timetable build() {
        validateTimetableComplete();
        return timetable;
    }

    private void resetCurrentEntry() {
        currentSubject = null;
        currentTeacher = null;
        currentRoom = null;
    }

    private void validateSubjectSet() {
        if (currentSubject == null) {
            throw new IllegalStateException(
                    "Subject must be set before adding teacher. Use addSubject() first."
            );
        }
    }

    private void validateTeacherSet() {
        if (currentTeacher == null) {
            throw new IllegalStateException(
                    "Teacher must be set before adding room. Use addTeacher() first."
            );
        }
    }

    private void validateRoomSet() {
        if (currentRoom == null) {
            throw new IllegalStateException(
                    "Room must be set before adding time slot. Use addRoom() first."
            );
        }
    }

    private void validateTimetableComplete() {
        if (timetable.getSemester() == null || timetable.getAcademicYear() == null) {
            throw new IllegalStateException(
                    "Timetable must have semester and academic year set"
            );
        }
    }
}

