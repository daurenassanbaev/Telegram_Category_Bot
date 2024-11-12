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

@Service
@RequiredArgsConstructor
public class CategoryUploadService {
    private final CategoryRepository categoryRepository;
    public boolean isValidExcelFile(MultipartFile file) {
        return Objects.equals(file.getContentType(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    public LinkedHashMap<String, String> getCategoriesFromExcelFile(InputStream inputStream) {
        LinkedHashMap<String, String> categories = new LinkedHashMap<>();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

            XSSFSheet sheet = workbook.getSheet("Category Tree");

            if (sheet == null) {
                throw new RuntimeException("Sheet 'Category Tree' not found in Excel file.");
            }

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

    public String addAllCategories(LinkedHashMap<String, String> categories, Long chatId) {
        for (Map.Entry<String, String> element : categories.entrySet()) {
            String category = element.getKey();
            String parentCategory = element.getValue();
            Optional<Category> categoryOptional = categoryRepository.findByNameAndChatId(category, chatId);
            if (categoryOptional.isEmpty()) {
                if (parentCategory.equals("-")) {
                    Category category1 = new Category();
                    category1.setName(category);
                    category1.setChatId(chatId);
                    categoryRepository.save(category1);
                    continue;
                }
                Category categoryToSave = new Category();
                categoryToSave.setName(category);
                categoryToSave.setChatId(chatId);
                Category resultCategory = categoryRepository.save(categoryToSave);
                Optional<Category> parentOptional = categoryRepository.findByNameAndChatId(parentCategory, chatId);
                Category parentCategoryToEdit;
                if (!parentOptional.isEmpty()) {
                    resultCategory = categoryRepository.save(categoryToSave);
                    parentCategoryToEdit = parentOptional.get();
                    resultCategory.setParent(parentCategoryToEdit);
                    parentCategoryToEdit.getChildren().add(resultCategory);
                    categoryRepository.save(resultCategory);
                    categoryRepository.save(parentCategoryToEdit);
                } else {
                    parentCategoryToEdit = new Category();
                    parentCategoryToEdit.setName(parentCategory);
                    parentCategoryToEdit.setChatId(chatId);
                    Category result = categoryRepository.save(parentCategoryToEdit);
                    resultCategory.setParent(result);
                    result.getChildren().add(resultCategory);
                    categoryRepository.save(resultCategory);
                    categoryRepository.save(result);
                }
            } else {
                if (parentCategory.equals("-")) {
                    continue;
                }
                Category childCategory = categoryOptional.get(); // earth
                Optional<Category> parentCategoryOpt = categoryRepository.findByNameAndChatId(parentCategory, chatId);
                if (parentCategoryOpt.isEmpty()) {
                    Category toSaveParentCategory = new Category();
                    toSaveParentCategory.setName(parentCategory);
                    toSaveParentCategory.setChatId(chatId);
                    Category savedParentCategory = categoryRepository.save(toSaveParentCategory);
                    savedParentCategory.getChildren().add(childCategory);
                    childCategory.setParent(savedParentCategory);
                    categoryRepository.save(childCategory);
                    categoryRepository.save(toSaveParentCategory);
                }
            }
        }

        return "Successfully added " + categories.keySet().size() + " categories.";
    }
}
