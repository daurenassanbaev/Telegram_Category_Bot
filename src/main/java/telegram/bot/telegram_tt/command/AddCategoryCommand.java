package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.facade.CategoryFacade;
import telegram.bot.telegram_tt.service.CategoryService;

/**
 * Command to add a category, either as root or as a child of an existing parent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AddCategoryCommand implements Command {

    private final CategoryFacade categoryFacade;

    /**
     * Executes the add category command.
     *
     * @param command full command text
     * @param chatId user chat ID
     * @return response message indicating success or failure
     */
    @Override
    public String execute(String command, Long chatId) {
        log.info("Executing add category command for chat ID: {}", chatId);

        String[] args = command.split(" ");
        if (args.length == 2) {
            String root = args[1].trim();
            log.info("Root category '{}' added successfully for chat ID: {}", root, chatId);
            return categoryFacade.addRootCategory(root, chatId);

        } else if (args.length >= 3) {
            String parent = extractParent(args, chatId);
            if (parent == null) {
                log.warn("Parent category does not exist for chat ID: {}", chatId);
                return "Parent category does not exist.";
            }
            String child = extractChild(args);
            log.info("Child category '{}' added successfully under parent '{}' for chat ID: {}", child, parent, chatId);
            return categoryFacade.addChildCategory(parent, child, chatId);
        } else {
            log.error("Invalid command format for chat ID: {}. Command: {}", chatId, command);
            return "Invalid format. Use /addElement <parent> <child> or /addElement <root>.";
        }
    }

    /**
     * Extracts the parent category from the command arguments.
     *
     * @param args command arguments
     * @param chatId the chat identifier
     * @return the parent category if it exists, otherwise null
     */
    private String extractParent(String[] args, Long chatId) {
        StringBuilder parentBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            parentBuilder.append(args[i]).append(" ");
            if (categoryFacade.categoryExists(parentBuilder.toString().trim(), chatId)) {
                return parentBuilder.toString().trim();
            }
        }
        log.warn("Parent category not found in the arguments for chat ID: {}", chatId);
        return null;
    }

    /**
     * Extracts the child category from the command arguments.
     *
     * @param args command arguments
     * @return the child category as a string
     */
    private String extractChild(String[] args) {
        StringBuilder childBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            childBuilder.append(args[i]).append(" ");
        }
        String child = childBuilder.toString().trim();
        log.debug("Extracted child category: {}", child);
        return child;
    }

}
