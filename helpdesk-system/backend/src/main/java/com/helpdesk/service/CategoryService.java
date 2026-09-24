package com.helpdesk.service;

import com.helpdesk.entity.Category;
import com.helpdesk.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Cacheable("categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
