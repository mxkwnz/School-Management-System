package school.facade;

import school.adapter.GradeAdapter;
import school.adapter.NumericGrade;
import school.observer.GradeNotifier;

public class SchoolFacade {
    private GradeNotifier gradeNotifier = new GradeNotifier();

    public void updateSubject(String studentName, NumericGrade oldGrade, int newScore) {
        int oldScore = oldGrade.getScore();
        NumericGrade newGrade = new NumericGrade(newScore);
        GradeAdapter adapter = new GradeAdapter(newGrade);
        adapter.adaptGrade();
        gradeNotifier.setGrade(studentName, oldScore, newScore);
    }

}
