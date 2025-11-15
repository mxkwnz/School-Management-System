package school.builder;

import java.util.ArrayList;
import java.util.List;

public class Timetable {
    private String trimester;
    private String academicYear;
    private List<TimetableEntry> entries;

    public Timetable() {
        this.entries = new ArrayList<>();
    }

    public void setTrimester(String trimester) {
        this.trimester = trimester;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public void addEntry(TimetableEntry entry) {
        this.entries.add(entry);
    }

    public String getTrimester() {
        return trimester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public List<TimetableEntry> getEntries() {
        return entries;
    }

    public void displayTimetable() {
        System.out.println("Academic Year: " + academicYear);
        System.out.println("Trimester: " + trimester);

        if (entries.isEmpty()) {
            System.out.println("No classes scheduled");
        } else {
            for (int i = 0; i < entries.size(); i++) {
                System.out.println((i + 1) + ". " + entries.get(i));
            }
        }
    }
}
