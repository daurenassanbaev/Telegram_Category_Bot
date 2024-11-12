package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.service.CategoryService;


/**
 * Command to view all categories as tree format from database.
 */
@Component
@RequiredArgsConstructor
public class ViewCategoryCommand implements Command {

    private final CategoryService categoryService;

    /**
     * Executes the view all categories command.
     *
     * @param command the full command text
     * @param chatId the chat ID of the user
     * @return returns all data from the database as tree format
     */
    @Override
    public String execute(String command, Long chatId) {
        return categoryService.viewCategoryTree(chatId);
    }
}
