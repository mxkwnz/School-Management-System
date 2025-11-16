package school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import school.model.Role;
import school.model.User;
import school.repository.RoleRepository;
import school.repository.UserRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public User register(String userId, String firstName, String lastName, String email, String password, String userType, String major, String department, String position) {
        if (userRepository.findByEmail(email).isPresent() || userRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User(userId, firstName, lastName, email, password);
        
        if ("student".equalsIgnoreCase(userType)) {
            user.setMajor(major);
            Role studentRole = roleRepository.findByName("STUDENT")
                    .orElseGet(() -> roleRepository.save(new Role("STUDENT")));
            user.addRole(studentRole);
        } else if ("staff".equalsIgnoreCase(userType) || "teacher".equalsIgnoreCase(userType)) {
            user.setDepartment(department);
            user.setPosition(position);
            Role teacherRole = roleRepository.findByName("TEACHER")
                    .orElseGet(() -> roleRepository.save(new Role("TEACHER")));
            user.addRole(teacherRole);
        } else if ("admin".equalsIgnoreCase(userType)) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ADMIN")));
            user.addRole(adminRole);
        } else if ("advisor".equalsIgnoreCase(userType)) {
            Role advisorRole = roleRepository.findByName("ADVISOR")
                    .orElseGet(() -> roleRepository.save(new Role("ADVISOR")));
            user.addRole(advisorRole);
        }

        return userRepository.save(user);
    }

    public Optional<User> login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Set<String> getUserRoles(User user) {
        Set<String> roleNames = new HashSet<>();
        for (Role role : user.getRoles()) {
            roleNames.add(role.getName());
        }
        return roleNames;
    }
}

