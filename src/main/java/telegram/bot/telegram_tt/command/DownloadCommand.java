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
 * Command to download all categories from the database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DownloadCommand implements Command {

    private final CategoryFacade categoryFacade;

    /**
     * Executes the download categories command.
     *
     * @param command the full command text
     * @param chatId the user's chat ID
     * @return returns all data from the database as an .xlsx file
     */
    @Override
    public SendDocument execute(String command, Long chatId) {
        log.info("Executing download command for chat ID: {}", chatId);
        return getSendDocument(chatId);
    }

    private SendDocument getSendDocument(Long chatId) {
        try {
            log.debug("Attempting to create the Excel file for chat ID: {}", chatId);

            // Create an Excel file with categories using the service
            byte[] excelFile = categoryFacade.createCategoryTreeExcel(chatId);

            // Convert the byte array into an InputStream for the document
            ByteArrayInputStream inputStream = new ByteArrayInputStream(excelFile);

            // Prepare the SendDocument object to send the Excel file
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(new InputFile(inputStream, "AllCategoriesTree.xlsx"));
            sendDocument.setCaption("Categories Tree");
            log.info("Excel file successfully created for chat ID: {}", chatId);
            return sendDocument;
        } catch (IOException e) {
            // Log the error with details
            log.error("Failed to generate Excel document for chat ID: {}", chatId, e);

            // Return null or an error response
            return null;
        }
    }

}
