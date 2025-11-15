package school;

import school.facade.SchoolFacade;

public class Main {
    public static void main(String[] args) {
        SchoolFacade facade = new SchoolFacade();
        facade.demonstrateCompleteSystem();
    }
}