package school.observer;

public interface GradeSubject {
    void notifyObservers(String studentName, int oldScore, int newScore);
    void addObserver(GradeObserver observer);
    void removeObserver(GradeObserver observer);
}
