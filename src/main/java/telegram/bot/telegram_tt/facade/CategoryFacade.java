package telegram.bot.telegram_tt.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.service.CategoryDownloadService;
import telegram.bot.telegram_tt.service.CategoryService;
import telegram.bot.telegram_tt.service.CategoryUploadService;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

// Здесь используется Facade Pattern
@Component
@RequiredArgsConstructor
public class CategoryFacade {
    private final CategoryService categoryService;
    private final CategoryDownloadService categoryDownloadService;
    private final CategoryUploadService categoryUploadService;

    /**
     * Добавление корневой категории.
     *
     * @param name имя категории
     * @param chatId идентификатор чата
     * @return сообщение о результате
     */
    public String addRootCategory(String name, Long chatId) {
        return categoryService.addRootCategory(name, chatId);
    }

    /**
     * Добавление дочерней категории.
     *
     * @param parentName имя родительской категории
     * @param childName имя дочерней категории
     * @param chatId идентификатор чата
     * @return сообщение о результате
     */
    public String addChildCategory(String parentName, String childName, Long chatId) {
        return categoryService.addChildCategory(parentName, childName, chatId);
    }

    /**
     * Удаление категории.
     *
     * @param name имя категории
     * @param chatId идентификатор чата
     * @return сообщение о результате
     */
    public String removeCategory(String name, Long chatId) {
        return categoryService.removeCategory(name, chatId);
    }

    /**
     * Просмотр дерева категорий.
     *
     * @param chatId идентификатор чата
     * @return дерево категорий в виде строки
     */
    public String viewCategoryTree(Long chatId) {
        return categoryService.viewCategoryTree(chatId);
    }

    /**
     * Проверка существования категории.
     *
     * @param name  имя категории
     * @param chatId идентификатор чата
     * @return true, если категория существует, иначе false
     */
    public boolean categoryExists(String name, Long chatId) {
        return categoryService.categoryExists(name, chatId);
    }

    /**
     * Создает Excel-файл с деревом категорий для заданного чата.
     *
     * @param chatId идентификатор чата
     * @return байтовый массив, представляющий Excel-файл
     * @throws IOException если возникнут проблемы при записи в файл
     */
    public byte[] createCategoryTreeExcel(Long chatId) throws IOException {
        return categoryDownloadService.createCategoryTreeExcel(chatId);
    }

    // Извлекаем категории из Excel файла
    public LinkedHashMap<String, String> getCategoriesFromExcelFile(InputStream inputStream) {
        return categoryUploadService.getCategoriesFromExcelFile(inputStream);
    }

    // Добавляем все категории из Excel файла в базу данных
    public String addAllCategories(LinkedHashMap<String, String> map, Long chatId) {
        return categoryUploadService.addAllCategories(map, chatId);
    }
}

