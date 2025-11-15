package school;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import school.facade.SchoolFacade;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Main.class, args);
        SchoolFacade facade = context.getBean(SchoolFacade.class);
        facade.demonstrateCompleteSystem();
    }
}