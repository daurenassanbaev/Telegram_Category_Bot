package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.facade.CategoryFacade;
import telegram.bot.telegram_tt.service.CategoryUploadService;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.LinkedHashMap;

/**
 * Command to upload categories from an Excel file.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UploadCommand implements FileCommand {

    private final CategoryFacade categoryFacade;

    /**
     * Executes the command to upload categories from a file.
     *
     * @param inputStream the input file stream
     * @param chatId the user's chat ID
     * @return a response message indicating success or failure
     * @throws IOException if there is an error processing the file
     */
    @Override
    public String executeFile(InputStream inputStream, Long chatId) throws IOException {
        log.info("Executing upload command for chat ID: {}", chatId);

        // Get categories from the uploaded Excel file
        LinkedHashMap<String, String> map = categoryFacade.getCategoriesFromExcelFile(inputStream);
        log.debug("Categories loaded from file: {}", map);

        // Add all categories to the database
        String response = categoryFacade.addAllCategories(map, chatId);
        log.info("Categories successfully uploaded for chat ID: {}", chatId);
        return response;
    }

    /**
     * This method is not used in this context.
     *
     * @param command the full text of the command
     * @param chatId the user's chat ID
     * @return null
     */
    @Override
    public Object execute(String command, Long chatId) {
        log.warn("Execute command method not implemented. Command: {}", command);
        return null;
    }
}
