package repositories.interfaces;

import objects.Category;

import java.util.List;

public interface CategoryRepository {
    List<Category> findAll();
}