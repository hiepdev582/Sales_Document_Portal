package com.salesportal.config;

import com.salesportal.entity.Department;
import com.salesportal.entity.Role;
import com.salesportal.entity.User;
import com.salesportal.repository.UserRepository;
import com.salesportal.service.PasswordService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public DataInitializer(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(new User(
                    "admin",
                    passwordService.hashPassword("admin123"),
                    "System Admin",
                    Role.ROLE_ADMIN,
                    Department.EXECUTIVE
            ));

            userRepository.save(new User(
                    "sales_staff_a",
                    passwordService.hashPassword("password123"),
                    "Sales Representative A",
                    Role.ROLE_STAFF,
                    Department.SALES
            ));

            userRepository.save(new User(
                    "sales_staff_b",
                    passwordService.hashPassword("password123"),
                    "Sales Representative B",
                    Role.ROLE_STAFF,
                    Department.SALES
            ));

            userRepository.save(new User(
                    "mkt_staff",
                    passwordService.hashPassword("password123"),
                    "Marketing Specialist",
                    Role.ROLE_STAFF,
                    Department.MARKETING
            ));
        }
    }
}
