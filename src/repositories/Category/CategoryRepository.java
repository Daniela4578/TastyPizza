package repositories.Category;

import objects.Category;
import java.util.List;

public interface CategoryRepository {
    List<Category> findAll();
}