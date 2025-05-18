package com.cs308.backend.services;

import com.cs308.backend.models.Category;
import com.cs308.backend.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CategoryServiceTest2 {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testCategory = new Category();
        testCategory.setCategoryId(UUID.randomUUID().toString());
        testCategory.setCategoryName("Home");
        testCategory.setProductIds(null);
    }

    @Test
    public void testAddCategory_NullName() {
        // Act
        var response = categoryService.addCategory(testCategory, null);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Category name cannot be null or empty!", response.getBody());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    public void testFindById_CategoryFound() {
        when(categoryRepository.findById(testCategory.getCategoryId()))
                .thenReturn(Optional.of(testCategory));

        Category result = categoryService.findById(testCategory.getCategoryId());

        assertNotNull(result);
        assertEquals("Home", result.getCategoryName());
        verify(categoryRepository).findById(testCategory.getCategoryId());
    }

    @Test
    public void testDeleteCategory_CategoryNotFound() {
        String missingId = "nonexistent-id";
        when(categoryRepository.existsById(missingId)).thenReturn(false);

        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> categoryService.deleteCategory(missingId)
        );

        assertTrue(ex.getMessage().contains("Category not found"));
        verify(categoryRepository, never()).deleteById(any());
    }
}
