package com.helpdesk.config;

import com.helpdesk.entity.Category;
import com.helpdesk.entity.Role;
import com.helpdesk.entity.SubCategory;
import com.helpdesk.entity.User;
import com.helpdesk.repository.CategoryRepository;
import com.helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds help topics and default system users from the campus helpdesk spec on boot.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public static final List<String> HELP_TOPICS = List.of(
            "AC-Problem",
            "Acadamic ERP & LMS Related",
            "Attendance System",
            "AV & Projector Problem",
            "Canteen / Cafeteria",
            "Carpentery",
            "CCTV Problem",
            "Civil- Mason",
            "Desktop",
            "Electrical",
            "Email - adding Email Group",
            "Email ID - Password",
            "Fire & Safety",
            "Hostel Gatepass",
            "Hot water",
            "Housekeeping – Hostels",
            "Housekeeping – Other Areas (Except Hostels)",
            "Landscape related works",
            "Laptop",
            "Lift Complaints",
            "Mazdoor - Shifting",
            "Network",
            "Onedrive/Microsoft",
            "Painting",
            "Pest Control",
            "Plumbing",
            "Printer or Scanner",
            "RO -Problem",
            "Scrolling LED Video Wall",
            "Security",
            "Server",
            "Stores, Table, Chair requisition",
            "STP-PROBLEM",
            "Telephone-Intercom problem",
            "Transport",
            "UPS-Problem",
            "Welding",
            "Wifi -LAN Internet"
    );

    @Override
    public void run(String... args) {
        seedCategories();
        seedUsers();
    }

    private void seedCategories() {
        if (categoryRepository.count() >= HELP_TOPICS.size()) {
            return;
        }

        categoryRepository.deleteAll();

        for (String topic : HELP_TOPICS) {
            Category category = Category.builder()
                    .name(topic)
                    .appliesTo("BOTH")
                    .build();

            category.getSubCategories().add(
                    SubCategory.builder()
                            .name(topic)
                            .category(category)
                            .build()
            );

            categoryRepository.save(category);
        }
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("manager@ssn.edu.in")) {
            userRepository.save(User.builder()
                    .fullName("Campus Manager")
                    .email("manager@ssn.edu.in")
                    .phoneNumber("9876543210")
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .role(Role.MANAGER)
                    .build());
        }

        if (!userRepository.existsByEmail("admin@ssn.edu.in")) {
            userRepository.save(User.builder()
                    .fullName("System Admin")
                    .email("admin@ssn.edu.in")
                    .phoneNumber("9876543211")
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .role(Role.ADMIN)
                    .build());
        }

        if (!userRepository.existsByEmail("worker@ssn.edu.in")) {
            userRepository.save(User.builder()
                    .fullName("Ramesh Worker")
                    .email("worker@ssn.edu.in")
                    .phoneNumber("9876543212")
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .role(Role.WORKER)
                    .specialization("General")
                    .build());
        }

        if (!userRepository.existsByEmail("student@ssn.edu.in")) {
            userRepository.save(User.builder()
                    .fullName("Arun Student")
                    .email("student@ssn.edu.in")
                    .phoneNumber("9876543213")
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .role(Role.STUDENT)
                    .build());
        }
    }
}
