package school.strategy;

public class PassFailAttendanceStrategy implements AttendanceStrategy {

    @Override
    public double calculate(int presentDays, int totalDays) {
        if (totalDays == 0) {
            return 0.0;
        }
        double percentage = (presentDays * 100.0) / totalDays;
        if (percentage >= 75.0) {
            return 100.0;
        }
        return 0.0;
    }
}

