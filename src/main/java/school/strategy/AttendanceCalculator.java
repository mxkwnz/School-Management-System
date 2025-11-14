package school.strategy;

public class AttendanceCalculator {
    private AttendanceStrategy strategy;

    public AttendanceCalculator(AttendanceStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(AttendanceStrategy strategy) {
        this.strategy = strategy;
    }

    public double calculateAttendance(int presentDays, int totalDays) {
        return strategy.calculate(presentDays, totalDays);
    }
}

