package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.facade.CategoryFacade;
import telegram.bot.telegram_tt.service.CategoryService;
import lombok.extern.slf4j.Slf4j;

/**
 * Command to remove a category from the database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RemoveCategoryCommand implements Command {

    private final CategoryFacade categoryFacade;

    /**
     * Executes the command to remove a category.
     *
     * @param command the full text of the command
     * @param chatId the user's chat ID
     * @return a response message indicating success or failure
     */
    @Override
    public String execute(String command, Long chatId) {
        log.info("Executing remove category command for chat ID: {}", chatId);

        String[] args = command.split(" ");
        if (args.length >= 2) {
            // Build the category name string to be removed, starting with the first element after the command
            StringBuilder forRemove = new StringBuilder(args[1].trim());
            forRemove.append(" ");

            // Add the remaining elements to the category string for removal
            for (int i = 2; i < args.length; i++) {
                String element = args[i].trim();
                forRemove.append(element).append(" ");
            }

            // Execute the category removal
            log.debug("Category to be removed: {}", forRemove.toString().trim());
            String response = categoryFacade.removeCategory(forRemove.toString().trim(), chatId);
            log.info("Remove category response: {}", response);
            return response;
        } else {
            log.error("Invalid command format for chat ID: {}. Command: {}", chatId, command);
            return "Invalid command format. Use /remove <element>.";
        }
    }
}
