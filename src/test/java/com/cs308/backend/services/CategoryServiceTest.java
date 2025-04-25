// 9. CategoryServiceTest.java
package com.cs308.backend.services;

import com.cs308.backend.models.Category;
import com.cs308.backend.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testCategory = new Category();
        testCategory.setCategoryId(UUID.randomUUID().toString());
        testCategory.setCategoryName("Electronics");
        testCategory.setProductIds(new ArrayList<>());
    }

    @Test
    public void testAddCategory_Successful() {
        // Arrange
        when(categoryRepository.existsByCategoryName(testCategory.getCategoryName())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        ResponseEntity<String> response = categoryService.addCategory(testCategory, testCategory.getCategoryName());

        // Assert
        assertEquals("Category added successfully!", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        verify(categoryRepository, times(1)).save(testCategory);
    }

    @Test
    public void testAddCategory_NameAlreadyExists() {
        // Arrange
        when(categoryRepository.existsByCategoryName(testCategory.getCategoryName())).thenReturn(true);

        // Act
        ResponseEntity<String> response = categoryService.addCategory(testCategory, testCategory.getCategoryName());

        // Assert
        assertEquals("Category with this name already exists!", response.getBody());
        assertEquals(400, response.getStatusCodeValue());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    public void testGetAllCategories_ReturnsAllCategories() {
        // Arrange
        Category category1 = new Category();
        category1.setCategoryId(UUID.randomUUID().toString());
        category1.setCategoryName("Electronics");

        Category category2 = new Category();
        category2.setCategoryId(UUID.randomUUID().toString());
        category2.setCategoryName("Clothing");

        List<Category> expectedCategories = Arrays.asList(category1, category2);

        when(categoryRepository.findAll()).thenReturn(expectedCategories);

        // Act
        List<Category> result = categoryService.getAllCategories();

        // Assert
        assertEquals(expectedCategories, result);
        verify(categoryRepository, times(1)).findAll();
    }
}