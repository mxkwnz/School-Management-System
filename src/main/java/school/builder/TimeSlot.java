package school.builder;

public class TimeSlot {
    private String dayOfWeek;
    private String startTime;
    private String endTime;

    public TimeSlot(String dayOfWeek, String startTime, String endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + startTime + "-" + endTime;
    }
}
