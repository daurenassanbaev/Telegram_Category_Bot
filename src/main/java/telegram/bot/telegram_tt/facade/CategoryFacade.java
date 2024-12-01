package telegram.bot.telegram_tt.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.service.CategoryDownloadService;
import telegram.bot.telegram_tt.service.CategoryService;
import telegram.bot.telegram_tt.service.CategoryUploadService;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

// Facade Pattern is used here
@Component
@RequiredArgsConstructor
public class CategoryFacade {
    private final CategoryService categoryService;
    private final CategoryDownloadService categoryDownloadService;
    private final CategoryUploadService categoryUploadService;

    /**
     * Adds a root category.
     *
     * @param name   the name of the category
     * @param chatId the chat identifier
     * @return a message indicating the result
     */
    public String addRootCategory(String name, Long chatId) {
        return categoryService.addRootCategory(name, chatId);
    }

    /**
     * Adds a child category under a parent category.
     *
     * @param parentName the name of the parent category
     * @param childName  the name of the child category
     * @param chatId     the chat identifier
     * @return a message indicating the result
     */
    public String addChildCategory(String parentName, String childName, Long chatId) {
        return categoryService.addChildCategory(parentName, childName, chatId);
    }

    /**
     * Removes a category.
     *
     * @param name   the name of the category
     * @param chatId the chat identifier
     * @return a message indicating the result
     */
    public String removeCategory(String name, Long chatId) {
        return categoryService.removeCategory(name, chatId);
    }

    /**
     * Views the category tree.
     *
     * @param chatId the chat identifier
     * @return a string representing the category tree
     */
    public String viewCategoryTree(Long chatId) {
        return categoryService.viewCategoryTree(chatId);
    }

    /**
     * Checks if a category exists.
     *
     * @param name   the name of the category
     * @param chatId the chat identifier
     * @return true if the category exists, otherwise false
     */
    public boolean categoryExists(String name, Long chatId) {
        return categoryService.categoryExists(name, chatId);
    }

    /**
     * Creates an Excel file with the category tree for the given chat.
     *
     * @param chatId the chat identifier
     * @return a byte array representing the Excel file
     * @throws IOException if there are issues writing the file
     */
    public byte[] createCategoryTreeExcel(Long chatId) throws IOException {
        return categoryDownloadService.createCategoryTreeExcel(chatId);
    }

    // Extract categories from the Excel file
    public LinkedHashMap<String, String> getCategoriesFromExcelFile(InputStream inputStream) {
        return categoryUploadService.getCategoriesFromExcelFile(inputStream);
    }

    // Adds all categories from the Excel file to the database
    public String addAllCategories(LinkedHashMap<String, String> map, Long chatId) {
        return categoryUploadService.addAllCategories(map, chatId);
    }
}
