package school.observer;

public interface GradeObserver {
    void update(String studentName, int oldScore, int newScore);
}
