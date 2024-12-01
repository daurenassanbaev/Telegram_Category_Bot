package telegram.bot.telegram_tt.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import telegram.bot.telegram_tt.entity.Category;
import telegram.bot.telegram_tt.repository.CategoryRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Сервис для выгрузки категории из базы данных.
 */
@Service
@RequiredArgsConstructor
public class CategoryUploadService {
    private final CategoryRepository categoryRepository;

    // Проверка, является ли файл валидным Excel файлом
    public boolean isValidExcelFile(MultipartFile file) {
        return Objects.equals(file.getContentType(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    // Извлекаем категории из Excel файла
    public LinkedHashMap<String, String> getCategoriesFromExcelFile(InputStream inputStream) {
        LinkedHashMap<String, String> categories = new LinkedHashMap<>();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

            XSSFSheet sheet = workbook.getSheet("Category Tree");

            // Проверка наличия листа "Category Tree"
            if (sheet == null) {
                throw new RuntimeException("Sheet 'Category Tree' not found in Excel file.");
            }

            // Проходим по строкам в листе
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (row == null) continue;

                Cell parentCell = row.getCell(0);
                Cell childCell = row.getCell(1);

                if (parentCell != null && childCell != null) {
                    String parentCategory = parentCell.getStringCellValue();
                    String childCategory = childCell.getStringCellValue();
                    categories.put(parentCategory, childCategory);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return categories;
    }

    // Добавляем все категории из Excel файла в базу данных
    // Тут использовался composite pattern в  addAllCategories
    public String addAllCategories(LinkedHashMap<String, String> categories, Long chatId) {
        for (Map.Entry<String, String> element : categories.entrySet()) {
            String category = element.getKey();
            String parentCategory = element.getValue();

            // Проверка существования категории в базе данных
            Optional<Category> categoryOptional = categoryRepository.findByNameAndChatId(category, chatId);
            if (categoryOptional.isEmpty()) {
                if (parentCategory.equals("-")) {
                    // Если родительская категория не указана, создаем корневую категорию
                    Category category1 = Category.builder()
                            .name(category)
                            .chatId(chatId)
                            .build();
                    categoryRepository.save(category1);
                    continue;
                }

                // Создаем категорию и связываем ее с родительской категорией
                Category categoryToSave = Category.builder()
                        .name(category)
                        .chatId(chatId)
                        .build();
                Category resultCategory = categoryRepository.save(categoryToSave);
                Optional<Category> parentOptional = categoryRepository.findByNameAndChatId(parentCategory, chatId);
                Category parentCategoryToEdit;

                if (!parentOptional.isEmpty()) {
                    // Если родительская категория существует, связываем с ней
                    resultCategory = categoryRepository.save(categoryToSave);
                    parentCategoryToEdit = parentOptional.get();
                    resultCategory.setParent(parentCategoryToEdit);
                    parentCategoryToEdit.addChild(resultCategory);
                    categoryRepository.save(resultCategory);
                    categoryRepository.save(parentCategoryToEdit);
                } else {
                    // Если родительской категории нет, создаем ее
                    parentCategoryToEdit = Category.builder()
                            .name(parentCategory)
                            .chatId(chatId)
                            .build();
                    Category result = categoryRepository.save(parentCategoryToEdit);
                    resultCategory.setParent(result);
                    // composite pattern
                    result.addChild(resultCategory);
                    categoryRepository.save(resultCategory);
                    categoryRepository.save(result);
                }
            } else {
                if (parentCategory.equals("-")) {
                    continue;
                }

                // Если категория уже существует, проверяем родительскую категорию
                Category childCategory = categoryOptional.get();
                Optional<Category> parentCategoryOpt = categoryRepository.findByNameAndChatId(parentCategory, chatId);
                if (parentCategoryOpt.isEmpty()) {
                    // Если родительской категории нет, создаем ее и связываем с дочерней категорией
                    Category toSaveParentCategory = Category.builder()
                            .name(parentCategory)
                            .chatId(chatId)
                            .build();
                    Category savedParentCategory = categoryRepository.save(toSaveParentCategory);
                    // composite pattern
                    savedParentCategory.addChild(childCategory);
                    childCategory.setParent(savedParentCategory);
                    categoryRepository.save(childCategory);
                    categoryRepository.save(toSaveParentCategory);
                }
            }
        }

        // Возвращаем сообщение об успешном добавлении категорий
        return "Successfully added " + categories.keySet().size() + " categories.";
    }
}
