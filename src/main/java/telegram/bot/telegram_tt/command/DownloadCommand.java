package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import telegram.bot.telegram_tt.facade.CategoryFacade;
import telegram.bot.telegram_tt.service.CategoryDownloadService;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Команда для загрузки всех категорий из базы данных.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DownloadCommand implements Command {

    private final CategoryFacade categoryFacade;

    /**
     * Выполняет команду загрузки категорий.
     *
     * @param command полный текст команды
     * @param chatId идентификатор чата пользователя
     * @return возвращает все данные из базы данных в виде файла .xlsx
     */
    @Override
    public SendDocument execute(String command, Long chatId) {
        log.info("Executing download command for chat ID: {}", chatId);
        return getSendDocument(chatId);
    }

    private SendDocument getSendDocument(Long chatId) {
        try {
            log.debug("Attempting to create the Excel file for chat ID: {}", chatId);

            // Создаем файл Excel с категориями с помощью сервиса
            byte[] excelFile = categoryFacade.createCategoryTreeExcel(chatId);

            // Преобразуем массив байтов в InputStream для документа
            ByteArrayInputStream inputStream = new ByteArrayInputStream(excelFile);

            // Подготавливаем объект SendDocument для отправки файла Excel
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(new InputFile(inputStream, "AllCategoriesTree.xlsx"));
            sendDocument.setCaption("Categories Tree");
            log.info("Excel file successfully created for chat ID: {}", chatId);
            return sendDocument;
        } catch (IOException e) {
            // Запишите ошибку с подробностями
            log.error("Failed to generate Excel document for chat ID: {}", chatId, e);

            // Возвращает null или ответ об ошибке
            return null;
        }
    }

}
