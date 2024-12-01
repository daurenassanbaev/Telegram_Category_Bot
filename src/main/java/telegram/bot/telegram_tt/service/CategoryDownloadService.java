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
 * Service for downloading categories from the database in Excel format.
 */
@Service
@RequiredArgsConstructor
public class CategoryDownloadService {

    private final CategoryRepository categoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(CategoryDownloadService.class);

    /**
     * Creates an Excel file with the category tree for a given chat.
     *
     * Uses the Template Pattern: defines a general algorithm for file creation
     * (initialization, header addition, data writing, finalization).
     *
     * @param chatId chat identifier
     * @return byte array representing the Excel file
     * @throws IOException if any issues occur during file writing
     */
    public byte[] createCategoryTreeExcel(Long chatId) throws IOException {
        logger.info("Starting to create category tree Excel for chatId: {}", chatId);

        // Get all root categories for the chat
        List<Category> rootCategories = categoryRepository.findByParentIsNullAndChatId(chatId);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Category Tree");
            int[] rowIndex = {0};

            // Step 1: Add headers — encapsulated in createHeaderRow
            createHeaderRow(sheet, rowIndex);

            // Step 2: Write data — encapsulated in writeCategoryToSheet
            for (Category rootCategory : rootCategories) {
                writeCategoryToSheet(rootCategory, sheet, rowIndex);
            }

            // Step 3: Final processing and writing to stream
            sheet.autoSizeColumn(0);  // Auto-adjust column width
            sheet.autoSizeColumn(1);

            // Write data to a byte array and return
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                logger.info("Category tree Excel file created successfully for chatId: {}", chatId);
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            logger.error("Error occurred while creating Excel file for chatId: {}", chatId, e);
            throw e; // Re-throw the exception
        }
    }

    /**
     * Creates a header row for the Excel sheet.
     *
     * Uses the Template Pattern: adds headers as a fixed part of the template,
     * which can be modified if the header format needs changes.
     *
     * @param sheet the sheet to add headers to
     * @param rowIndex the current row index
     */
    private void createHeaderRow(Sheet sheet, int[] rowIndex) {
        Row headerRow = sheet.createRow(rowIndex[0]++);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        // Column headers
        String[] headers = {"Category", "Parent Category"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Recursively writes a category and its subcategories to the Excel sheet.
     *
     * Uses the Template Pattern: adds categories according to the hierarchy,
     * but the recursive logic can be replaced if the category structure changes.
     *
     * @param category the category to write
     * @param sheet the sheet to write the category to
     * @param rowIndex the current row index
     */
    private void writeCategoryToSheet(Category category, Sheet sheet, int[] rowIndex) {
        Row row = sheet.createRow(rowIndex[0]++);
        row.createCell(0).setCellValue(category.getName());
        row.createCell(1).setCellValue(category.getParent() != null ? category.getParent().getName() : "-");

        // Composite Pattern: recursive traversal of subcategories (tree)
        for (Category child : category.getChildren()) {
            writeCategoryToSheet(child, sheet, rowIndex);
        }
    }
}
