package school.adapter;

public class NumericGrade {
    private int score;

    public NumericGrade(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void showScore() {
        System.out.println("Score: " + score);
    }
}
