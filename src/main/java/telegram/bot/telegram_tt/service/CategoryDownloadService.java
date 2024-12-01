package telegram.bot.telegram_tt.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import telegram.bot.telegram_tt.composite.CategoryComponent;
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
     * Использует Template Pattern: определяет общий алгоритм создания файла
     * (инициализация файла, добавление заголовков, запись данных, завершение).
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

            // Шаг 1: Добавление заголовков — реализация инкапсулирована в createHeaderRow
            createHeaderRow(sheet, rowIndex);

            // Шаг 2: Запись данных — реализация инкапсулирована в writeCategoryToSheet
            for (Category rootCategory : rootCategories) {
                writeCategoryToSheet(rootCategory, sheet, rowIndex);
            }

            // Шаг 3: Финальная обработка и запись в поток
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
     * Использует Template Pattern: добавляет заголовки как фиксированную часть шаблона,
     * которую можно модифицировать, если потребуется изменить формат заголовков.
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
     * Использует Template Pattern: добавляет категории согласно иерархии,
     * но логику рекурсивного вызова можно заменить, если структура категорий изменится.
     *
     * @param category категория для записи
     * @param sheet лист, в который записывается категория
     * @param rowIndex индекс текущей строки
     */
    private void writeCategoryToSheet(CategoryComponent category, Sheet sheet, int[] rowIndex) {
        Row row = sheet.createRow(rowIndex[0]++);
        row.createCell(0).setCellValue(category.getName());
        row.createCell(1).setCellValue(category.getParent() != null ? category.getParent().getName() : "-");

        // Рекурсивный вызов для записи дочерних категорий
        for (CategoryComponent child : category.getChildren()) {
            writeCategoryToSheet(child, sheet, rowIndex);
        }
    }
}
