package school.decorator;

public class TeacherDecorator extends UserDecorator {
    public TeacherDecorator(User decoratedUser) {
        super(decoratedUser);
    }

    @Override
    public String getDescription() {
        return decoratedUser.getDescription() + ", Teacher";
    }

    @Override
    public int getAccessLevel() {
        return decoratedUser.getAccessLevel() + 2;
    }
}
