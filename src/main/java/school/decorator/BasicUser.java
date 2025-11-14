package school.decorator;

public class BasicUser implements User {
    @Override
    public String getDescription(){
        return "Basic User";
    }
    @Override
    public int getAccessLevel() {
        return 1;
    }
}
