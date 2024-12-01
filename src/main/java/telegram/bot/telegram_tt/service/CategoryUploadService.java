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
 * Service for uploading categories from an Excel file to the database.
 */
@Service
@RequiredArgsConstructor
public class CategoryUploadService {
    private final CategoryRepository categoryRepository;

    // Checks if the uploaded file is a valid Excel file
    public boolean isValidExcelFile(MultipartFile file) {
        return Objects.equals(file.getContentType(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    // Extracts categories from the provided Excel file
    public LinkedHashMap<String, String> getCategoriesFromExcelFile(InputStream inputStream) {
        LinkedHashMap<String, String> categories = new LinkedHashMap<>();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

            XSSFSheet sheet = workbook.getSheet("Category Tree");

            // Checks if the sheet "Category Tree" exists
            if (sheet == null) {
                throw new RuntimeException("Sheet 'Category Tree' not found in Excel file.");
            }

            // Iterates over rows in the sheet
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

    // Adds all categories from the Excel file to the database
    // Uses composite pattern in the addAllCategories method
    public String addAllCategories(LinkedHashMap<String, String> categories, Long chatId) {
        for (Map.Entry<String, String> element : categories.entrySet()) {
            String category = element.getKey();
            String parentCategory = element.getValue();

            // Checks if the category already exists in the database
            Optional<Category> categoryOptional = categoryRepository.findByNameAndChatId(category, chatId);
            if (categoryOptional.isEmpty()) {
                if (parentCategory.equals("-")) {
                    // Creates a root category if no parent category is specified
                    Category category1 = Category.builder()
                            .name(category)
                            .chatId(chatId)
                            .build();
                    categoryRepository.save(category1);
                    continue;
                }

                // Creates a new category and links it to its parent
                Category categoryToSave = Category.builder()
                        .name(category)
                        .chatId(chatId)
                        .build();
                Category resultCategory = categoryRepository.save(categoryToSave);
                Optional<Category> parentOptional = categoryRepository.findByNameAndChatId(parentCategory, chatId);
                Category parentCategoryToEdit;

                if (!parentOptional.isEmpty()) {
                    // If the parent category exists, links the new category to it
                    resultCategory = categoryRepository.save(categoryToSave);
                    parentCategoryToEdit = parentOptional.get();
                    resultCategory.setParent(parentCategoryToEdit);
                    parentCategoryToEdit.getChildren().add(resultCategory);
                    categoryRepository.save(resultCategory);
                    categoryRepository.save(parentCategoryToEdit);
                } else {
                    // If the parent category does not exist, creates it and links the new category to it
                    parentCategoryToEdit = Category.builder()
                            .name(parentCategory)
                            .chatId(chatId)
                            .build();
                    Category result = categoryRepository.save(parentCategoryToEdit);
                    resultCategory.setParent(result);
                    // Composite pattern: adding child elements to the tree structure
                    result.getChildren().add(resultCategory);
                    categoryRepository.save(resultCategory);
                    categoryRepository.save(result);
                }
            } else {
                if (parentCategory.equals("-")) {
                    continue;
                }

                // If the category already exists, checks its parent category
                Category childCategory = categoryOptional.get();
                Optional<Category> parentCategoryOpt = categoryRepository.findByNameAndChatId(parentCategory, chatId);
                if (parentCategoryOpt.isEmpty()) {
                    // If the parent category does not exist, creates it and links it to the child category
                    Category toSaveParentCategory = Category.builder()
                            .name(parentCategory)
                            .chatId(chatId)
                            .build();
                    Category savedParentCategory = categoryRepository.save(toSaveParentCategory);
                    // Composite pattern: updating parent-child relationship
                    savedParentCategory.getChildren().add(childCategory);
                    childCategory.setParent(savedParentCategory);
                    categoryRepository.save(childCategory);
                    categoryRepository.save(toSaveParentCategory);
                }
            }
        }

        // Returns a success message after adding the categories
        return "Successfully added " + categories.keySet().size() + " categories.";
    }
}
