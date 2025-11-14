package school.decorator;

public abstract class UserDecorator implements User {
    protected User decoratedUser;

    public UserDecorator(User decoratedUser) {
        this.decoratedUser = decoratedUser;
    }

    @Override
    public String getDescription() {
        return decoratedUser.getDescription();
    }

    @Override
    public int getAccessLevel(){
        return decoratedUser.getAccessLevel();
    }
}
