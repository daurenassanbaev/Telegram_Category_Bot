package telegram.bot.telegram_tt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import telegram.bot.telegram_tt.entity.Category;
import telegram.bot.telegram_tt.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;


/**
 * Сервис для добавления, удаления и получения категории из базы данных.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    /**
     * Добавление корневой категории.
     *
     * @param name  имя категории
     * @param chatId идентификатор чата
     * @return сообщение о результате
     */
    @Override
    @Transactional
    public String addRootCategory(String name, Long chatId) {
        log.info("Attempting to add root category with name: {} for chatId: {}", name, chatId);

        boolean check = categoryRepository.findByNameAndChatId(name, chatId).isPresent();
        if (check) {
            log.warn("Category with name {} already exists for chatId: {}", name, chatId);
            return "Category with name " + name + " is already exists. Please type another name for root category.";
        }
        Category category = createCategory(name, chatId);
        categoryRepository.save(category);
        log.info("Root category with name: {} added successfully for chatId: {}", name, chatId);
        return "Successfully added root category with name: " + name;
    }

    /**
     * Создает новую категорию.
     *
     * @param name  имя категории
     * @param chatId идентификатор чата
     * @return новая категория
     */
    public Category createCategory(String name, Long chatId) {
        Category category = new Category();
        category.setName(name);
        category.setChatId(chatId);
        return category;
    }

    /**
     * Добавление дочерней категории.
     *
     * @param name  имя родительской категории
     * @param child имя дочерней категории
     * @param chatId идентификатор чата
     * @return сообщение о результате
     */
    @Override
    @Transactional
    public String addChildCategory(String name, String child, Long chatId) {
        log.info("Attempting to add child category with name: {} under parent category: {} for chatId: {}", child, name, chatId);

        if (name.equals(child)) {
            log.warn("User tried to add a category as its own child. Invalid input: {}", name);
            return "Please enter a correct category name.";
        }

        Optional<Category> parentOpt = categoryRepository.findByNameAndChatId(name, chatId);
        if (parentOpt.isEmpty()) {
            log.warn("Parent category with name {} does not exist for chatId: {}", name, chatId);
            return "Category with name " + name + " does not exist. Please specify an existing parent category.";
        }
        Category parentCategory = parentOpt.get();
        Category childCategory = findOrCreateChildCategory(child, chatId);

        if (childCategory.getParent() != null && childCategory.getParent().getId().equals(parentCategory.getId())) {
            log.warn("The category {} is already a child of the parent category {} for chatId: {}", child, name, chatId);
            return "Please enter a valid category.";
        }

        childCategory.setParent(parentCategory);
        parentCategory.getChildren().add(childCategory);
        categoryRepository.save(childCategory);
        log.info("Successfully added child category: {} to parent category: {} for chatId: {}", child, name, chatId);
        return "Successfully added child: %s to parent: %s".formatted(child, name);
    }

    /**
     * Находит или создает дочернюю категорию.
     *
     * @param child имя дочерней категории
     * @param chatId идентификатор чата
     * @return найденная или созданная дочерняя категория
     */
    private Category findOrCreateChildCategory(String child, Long chatId) {
        return categoryRepository.findByNameAndChatId(child, chatId)
                .orElseGet(() -> createCategory(child, chatId));
    }

    /**
     * Удаление категории.
     *
     * @param name  имя категории
     * @param chatId идентификатор чата
     * @return сообщение о результате
     */
    @Override
    @Transactional
    public String removeCategory(String name, Long chatId) {
        log.info("Attempting to remove category with name: {} for chatId: {}", name, chatId);

        Optional<Category> categoryOpt = categoryRepository.findByNameAndChatId(name, chatId);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();

            // Удаление категории из списка детей родителя, если категория имеет родителя
            if (category.getParent() != null) {
                category.getParent().getChildren().remove(category);
                categoryRepository.save(category.getParent());
            }

            categoryRepository.delete(category);
            log.info("Successfully removed category with name: {} for chatId: {}", name, chatId);
            return "Successfully removed category with name: " + name;
        }
        log.warn("Category with name {} does not exist for chatId: {}", name, chatId);
        return "Category with name " + name + " does not exist. Please type exists category.";
    }

    /**
     * Проверка существования категории.
     *
     * @param name  имя категории
     * @param chatId идентификатор чата
     * @return true, если категория существует, иначе false
     */
    @Override
    public boolean categoryExists(String name, Long chatId) {
        return categoryRepository.findByNameAndChatId(name, chatId).isPresent();
    }

    /**
     * Получение списка корневых категорий для данного чата.
     *
     * @param chatId идентификатор чата
     * @return список корневых категорий
     */
    @Override
    public List<Category> findByParentIsNullAndChatId(Long chatId) {
        return categoryRepository.findByParentIsNullAndChatId(chatId);
    }

    /**
     * Просмотр дерева категорий для данного чата.
     *
     * @param chatId идентификатор чата
     * @return дерево категорий в виде строки
     */
    @Override
    public String viewCategoryTree(Long chatId) {
        log.info("Attempting to view category tree for chatId: {}", chatId);

        List<Category> list = findByParentIsNullAndChatId(chatId);
        if (list.isEmpty()) {
            log.info("No categories found for chatId: {}", chatId);
            return "There are no categories";
        }
        StringBuilder tree = new StringBuilder();
        for (Category category : list) {
            buildTree(category, tree, 0);
        }
        log.info("Category tree generated successfully for chatId: {}", chatId);
        return tree.toString();
    }

    /**
     * Рекурсивно строит дерево категорий.
     *
     * @param category категория для записи
     * @param builder строковый билдер для дерева
     * @param level уровень вложенности
     */
    private void buildTree(Category category, StringBuilder builder, int level) {
        builder.append("    ".repeat(level)).append("-   ").append(category.getName()).append("\n");
        for (Category child : category.getChildren()) {
            buildTree(child, builder, level + 1);
        }
    }
}
