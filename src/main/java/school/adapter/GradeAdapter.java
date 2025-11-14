package school.adapter;

public class GradeAdapter implements LetterGrade{
    private NumericGrade numericGrade;

    public GradeAdapter(NumericGrade numericGrade) {
        this.numericGrade = numericGrade;
    }

    @Override
    public void adaptGrade() {
        int score = numericGrade.getScore();
        String grade;

        if (score >= 95) {
            grade = "A";
        }
        else if (score >= 90) {
            grade = "A-";
        }
        else if (score >= 85) {
            grade = "B+";
        }
        else if (score >= 80) {
            grade = "B";
        }
        else if (score >= 75) {
            grade = "B-";
        }
        else if (score >= 70) {
            grade = "C+";
        }
        else if (score >= 65) {
            grade = "C";
        }
        else if (score >= 60) {
            grade = "C-";
        }
        else if (score >= 55) {
            grade = "D+";
        }
        else if (score >= 50) {
            grade = "D";
        }
        else if (score >= 40) {
            grade = "D-";
        }
        else {
            grade = "F";
        }

        System.out.println("Your grade: " + grade);
    }
}
