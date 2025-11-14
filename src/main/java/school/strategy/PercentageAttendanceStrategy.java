package school.strategy;

public class PercentageAttendanceStrategy implements AttendanceStrategy {
    @Override
    public double calculate(int presentDays, int totalDays) {
        if (totalDays == 0) {
            return 0.0;
        }
        return (presentDays * 100.0) / totalDays;
    }
}

