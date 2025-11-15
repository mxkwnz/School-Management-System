package school.decorator;

public class AdminDecorator extends UserDecorator {
    public AdminDecorator(User decoratedUser) {
        super(decoratedUser);
    }

    @Override
    public String getDescription() {
        return decoratedUser.getDescription() + ", Admin";
    }

    @Override
    public int getAccessLevel(){
        return decoratedUser.getAccessLevel() + 3;
    }
}
