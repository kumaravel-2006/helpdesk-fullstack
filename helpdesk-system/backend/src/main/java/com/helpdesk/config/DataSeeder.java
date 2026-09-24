package com.helpdesk.config;

import com.helpdesk.entity.Category;
import com.helpdesk.entity.SubCategory;
import com.helpdesk.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds help topics from the campus helpdesk spec on boot.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

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
}
