package school.observer;

public class StudentObserver implements GradeObserver {

    @Override
    public void update(String studentName, int oldScore, int newScore) {
        System.out.println(studentName + " is notified that his grade changed from " + oldScore + " to " + newScore);
    }
}
