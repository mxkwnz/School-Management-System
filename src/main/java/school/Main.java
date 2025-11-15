package school;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import school.facade.SchoolFacade;
import school.web.WebServer;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        System.setProperty("spring.main.web-application-type", "none");

        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        SchoolFacade facade = context.getBean(SchoolFacade.class);

        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        try {
            WebServer server = new WebServer(port);
            server.start();

            System.out.println("School Management System");
            System.out.println("Web server running at: http://localhost:" + port);

        } catch (Exception e) {
            System.err.println("Error starting web server: " + e.getMessage());
            e.printStackTrace();
        }

        facade.demonstrateCompleteSystem();
    }
}
