package telegram.bot.telegram_tt.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import telegram.bot.telegram_tt.entity.Category;
import telegram.bot.telegram_tt.repository.CategoryRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Сервис для загрузки категории из базы данных в формате Excel.
 */
@Service
@RequiredArgsConstructor
public class CategoryDownloadService {

    private final CategoryRepository categoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(CategoryDownloadService.class);

    /**
     * Создает Excel-файл с деревом категорий для заданного чата.
     *
     * @param chatId идентификатор чата
     * @return байтовый массив, представляющий Excel-файл
     * @throws IOException если возникнут проблемы при записи в файл
     */
    public byte[] createCategoryTreeExcel(Long chatId) throws IOException {
        logger.info("Starting to create category tree Excel for chatId: {}", chatId);

        // Получаем все корневые категории для чата
        List<Category> rootCategories = categoryRepository.findByParentIsNullAndChatId(chatId);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Category Tree");
            int[] rowIndex = {0};
            createHeaderRow(sheet, rowIndex); // Добавляем заголовок в таблицу

            // Записываем категории в файл
            for (Category rootCategory : rootCategories) {
                writeCategoryToSheet(rootCategory, sheet, rowIndex);
            }

            sheet.autoSizeColumn(0);  // Автоматически изменяем ширину столбцов
            sheet.autoSizeColumn(1);

            // Записываем данные в байтовый массив и возвращаем
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                logger.info("Category tree Excel file created successfully for chatId: {}", chatId);
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            logger.error("Error occurred while creating Excel file for chatId: {}", chatId, e);
            throw e; // Пробрасываем исключение дальше
        }
    }

    /**
     * Создает строку с заголовками для Excel-листа.
     *
     * @param sheet лист, в который добавляются заголовки
     * @param rowIndex индекс текущей строки
     */
    private void createHeaderRow(Sheet sheet, int[] rowIndex) {
        Row headerRow = sheet.createRow(rowIndex[0]++);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        // Заголовки колонок
        String[] headers = {"Category", "Parent Category"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Рекурсивно записывает категорию и ее подкатегории в Excel.
     *
     * @param category категория для записи
     * @param sheet лист, в который записывается категория
     * @param rowIndex индекс текущей строки
     */
    private void writeCategoryToSheet(Category category, Sheet sheet, int[] rowIndex) {
        Row row = sheet.createRow(rowIndex[0]++);
        row.createCell(0).setCellValue(category.getName());
        row.createCell(1).setCellValue(category.getParent() != null ? category.getParent().getName() : "-");

        // Рекурсивный вызов для записи дочерних категорий
        for (Category child : category.getChildren()) {
            writeCategoryToSheet(child, sheet, rowIndex);
        }
    }
}
