package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.bot.telegram_tt.service.CategoryDownloadService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import telegram.bot.telegram_tt.command.*;
import telegram.bot.telegram_tt.service.CategoryDownloadService;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Command to download all categories from database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DownloadCommand implements Command {

    private final CategoryDownloadService categoryDownloadService;

    /**
     * Executes the download categories command.
     *
     * @param command the full command text
     * @param chatId  the chat ID of the user
     * @return returns all data from the database as an .xlsx file
     */
    @Override
    public SendDocument execute(String command, Long chatId) {
        return getSendDocument(chatId);
    }

    private SendDocument getSendDocument(Long chatId) {
        try {
            byte[] excelFile = categoryDownloadService.createCategoryTreeExcel(chatId);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(excelFile);
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(new InputFile(inputStream, "AllCategoriesTree.xlsx"));
            sendDocument.setCaption("Categories Tree");
            return sendDocument;
        } catch (IOException e) {
            log.error("Failed to generate Excel document for chat ID: {}", chatId, e);
            throw new IllegalArgumentException();
        }
    }

}
