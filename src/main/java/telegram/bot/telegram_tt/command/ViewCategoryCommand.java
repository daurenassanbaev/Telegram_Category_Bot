package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.facade.CategoryFacade;
import telegram.bot.telegram_tt.service.CategoryService;
import lombok.extern.slf4j.Slf4j;

/**
 * Command to view all categories in a tree format from the database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCategoryCommand implements Command {

    private final CategoryFacade categoryFacade;

    /**
     * Executes the command to view all categories.
     *
     * @param command the full text of the command
     * @param chatId the user's chat ID
     * @return returns all data from the database in a tree format
     */
    @Override
    public String execute(String command, Long chatId) {
        log.info("Executing View Category Command for chat ID: {}", chatId);

        // Get the categories in a tree format
        String categoryTree = categoryFacade.viewCategoryTree(chatId);
        log.debug("Category tree fetched for chat ID {}: {}", chatId, categoryTree);

        return categoryTree;
    }
}
