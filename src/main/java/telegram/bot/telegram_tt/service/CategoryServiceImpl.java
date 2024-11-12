package telegram.bot.telegram_tt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import telegram.bot.telegram_tt.entity.Category;
import telegram.bot.telegram_tt.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public String addRootCategory(String name, Long chatId) {
        boolean check = categoryRepository.findByNameAndChatId(name, chatId).isPresent();
        if (check) {
            return "Category with name " + name + " is already exists. Please type another name for root category.";
        }
        Category category = createCategory(name, chatId);
        categoryRepository.save(category);
        return "Successfully added root category with name: " + name;
    }

    public Category createCategory(String name, Long chatId) {
        Category category = new Category();
        category.setName(name);
        category.setChatId(chatId);
        return category;
    }

    @Override
    @Transactional
    public String addChildCategory(String name, String child, Long chatId) {
        if (name.equals(child)) {
            return "Please enter a correct category name.";
        }
        Optional<Category> parentOpt = categoryRepository.findByNameAndChatId(name, chatId);
        if (parentOpt.isEmpty()) {
            return "Category with name " + name + " does not exist. Please specify an existing parent category.";
        }
        Category parentCategory = parentOpt.get();
        Category childCategory = findOrCreateChildCategory(child, chatId);

        if (childCategory.getParent() != null && childCategory.getParent().getId().equals(parentCategory.getId())) {
            return "Please enter a valid category.";
        }

        childCategory.setParent(parentCategory);
        parentCategory.getChildren().add(childCategory);
        categoryRepository.save(childCategory);
        return "Successfully added child: %s to parent: %s".formatted(child, name);
    }

    private Category findOrCreateChildCategory(String child, Long chatId) {
        return categoryRepository.findByNameAndChatId(child, chatId)
                .orElseGet(() -> createCategory(child, chatId));
    }

    @Override
    @Transactional
    public String removeCategory(String name, Long chatId) {
        Optional<Category> categoryOpt = categoryRepository.findByNameAndChatId(name, chatId);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();

            // Удалите категорию из списка детей родителя, если у нее есть родитель
            if (category.getParent() != null) {
                category.getParent().getChildren().remove(category);
                categoryRepository.save(category.getParent()); // Сохраните обновленного родителя
            }

            categoryRepository.delete(category);
            return "Successfully removed category with name: " + name;
        }
        return "Category with name " + name + " does not exist. Please type exists category.";
    }

    @Override
    public boolean categoryExists(String name, Long chatId) {
        return categoryRepository.findByNameAndChatId(name, chatId).isPresent();
    }

    @Override
    public List<Category> findByParentIsNullAndChatId(Long chatId) {
        return categoryRepository.findByParentIsNullAndChatId(chatId);
    }

    @Override
    public String viewCategoryTree(Long chatId) {
        List<Category> list = findByParentIsNullAndChatId(chatId);
        if (list.isEmpty()) {
            return "There are no categories";
        }
        StringBuilder tree = new StringBuilder();
        for (Category category : list) {
            buildTree(category, tree, 0);
        }
        return tree.toString();
    }

    private void buildTree(Category category, StringBuilder builder, int level) {
        builder.append("    ".repeat(level)).append("-   ").append(category.getName()).append("\n");
        for (Category child : category.getChildren()) {
            buildTree(child, builder, level + 1);
        }
    }

}
