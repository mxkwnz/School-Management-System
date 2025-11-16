package school.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import school.model.Role;
import school.model.Subject;
import school.repository.RoleRepository;
import school.repository.SubjectRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeSubjects();
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

    private void initializeSubjects() {
        String[][] subjects = {
                {"MATH101", "Mathematics I"},
                {"CS101", "Introduction to Programming"},
                {"CS201", "Data Structures"},
                {"SE301", "Software Design Patterns"}
        };

        for (String[] s : subjects) {
            String code = s[0];
            String name = s[1];
            if (!subjectRepository.findByCode(code).isPresent()) {
                Subject subject = new Subject(code, name);
                subjectRepository.save(subject);
                System.out.println("Created subject: " + code + " - " + name);
            }
        }

        System.out.println("Subject initialization completed.");
    }
}

