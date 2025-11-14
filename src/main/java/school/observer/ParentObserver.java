package school.observer;

public class ParentObserver implements GradeObserver {

    @Override
    public void update(String studentName, int oldScore, int newScore) {
        System.out.println("Parent of " + studentName + "is notified that grade changed from oldScore " + oldScore + " to newScore: " + newScore);
    }
}
