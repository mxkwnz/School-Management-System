package school.observer;

import java.util.HashSet;
import java.util.Set;

public class GradeNotifier implements GradeSubject {
    private Set<GradeObserver> observers = new HashSet<>();

    @Override
    public void addObserver(GradeObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(GradeObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String studentName, int oldScore, int newScore) {
        for (GradeObserver observer : observers) {
            observer.update(studentName, oldScore, newScore);
        }
    }

    public void setGrade(String studentName, int oldScore, int newScore) {
        notifyObservers(studentName, oldScore, newScore);
    }
}
