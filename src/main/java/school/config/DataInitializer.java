package school.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import school.model.Role;
import school.repository.RoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        String[] roleNames = {"STUDENT", "TEACHER", "ADMIN", "ADVISOR", "PARENT"};
        
        for (String roleName : roleNames) {
            if (!roleRepository.findByName(roleName).isPresent()) {
                Role role = new Role(roleName);
                roleRepository.save(role);
                System.out.println("Created role: " + roleName);
            } else {
                System.out.println("Role already exists: " + roleName);
            }
        }
        
        System.out.println("Role initialization completed.");
    }
}

