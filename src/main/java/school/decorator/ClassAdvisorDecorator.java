package school.decorator;

public class ClassAdvisorDecorator extends UserDecorator{
    public ClassAdvisorDecorator(User decoratedUser) {
        super(decoratedUser);
    }

    @Override
    public String getDescription() {
        return decoratedUser.getDescription() + ", Class Advisor";
    }

    @Override
    public int getAccessLevel(){
        return decoratedUser.getAccessLevel() + 1;
    }
}
