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
        
        try {
            WebServer webServer = new WebServer(8080);
            webServer.start();
            System.out.println("\n=== Web API Server Started ===");
            System.out.println("Server running at: http://localhost:8080");
        } catch (Exception e) {
            System.err.println("Failed to start WebServer: " + e.getMessage());
            e.printStackTrace();
        }
        
        facade.demonstrateCompleteSystem();
    }
}