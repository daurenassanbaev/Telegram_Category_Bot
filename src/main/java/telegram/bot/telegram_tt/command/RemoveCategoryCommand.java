package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.service.CategoryService;

/**
 * Command to remove category from database.
 */
@Component
@RequiredArgsConstructor
public class RemoveCategoryCommand implements Command {

    private final CategoryService categoryService;

    /**
     * Executes the remove category command.
     *
     * @param command the full command text
     * @param chatId the chat ID of the user
     * @return a response message indicating success or failure
     */
    @Override
    public String execute(String command, Long chatId) {
        String[] args = command.split(" ");
        if (args.length >= 2) {
            StringBuilder forRemove = new StringBuilder(args[1].trim());
            forRemove.append(" ");
            for (int i = 2; i < args.length; i++) {
                String element = args[i].trim();
                forRemove.append(element).append(" ");
            }
            return categoryService.removeCategory(forRemove.toString().trim(), chatId);
        } else {
            return "Invalid command format. Use /remove <element>.";
        }
    }
}
