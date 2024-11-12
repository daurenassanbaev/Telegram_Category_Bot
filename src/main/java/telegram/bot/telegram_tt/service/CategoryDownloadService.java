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

@Service
@RequiredArgsConstructor
public class CategoryDownloadService {
    private final CategoryRepository categoryRepository;

    public byte[] createCategoryTreeExcel(Long chatId) throws IOException {
        List<Category> rootCategories = categoryRepository.findByParentIsNullAndChatId(chatId);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Category Tree");
            int[] rowIndex = {0};
            createHeaderRow(sheet, rowIndex);

            for (Category rootCategory : rootCategories) {
                writeCategoryToSheet(rootCategory, sheet, rowIndex);
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    private void createHeaderRow(Sheet sheet, int[] rowIndex) {
        Row headerRow = sheet.createRow(rowIndex[0]++);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        String[] headers = {"Category", "Parent Category"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeCategoryToSheet(Category category, Sheet sheet, int[] rowIndex) {
        Row row = sheet.createRow(rowIndex[0]++);
        row.createCell(0).setCellValue(category.getName());
        row.createCell(1).setCellValue(category.getParent() != null ? category.getParent().getName() : "-");

        for (Category child : category.getChildren()) {
            writeCategoryToSheet(child, sheet, rowIndex);
        }
    }
}
