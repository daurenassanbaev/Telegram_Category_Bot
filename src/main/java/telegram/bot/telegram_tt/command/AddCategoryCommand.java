package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.service.CategoryService;

/**
 * Command to add a category, either as a root or as a child of an existing parent.
 */
@Component
@RequiredArgsConstructor
public class AddCategoryCommand implements Command {

    private final CategoryService categoryService;

    /**
     * Executes the add category command.
     *
     * @param command the full command text
     * @param chatId the chat ID of the user
     * @return a response message indicating success or failure
     */
    @Override
    public String execute(String command, Long chatId) {
        String[] args = command.split(" ");
        if (args.length == 2) {
            String root = args[1].trim();
            return categoryService.addRootCategory(root, chatId);
        } else if (args.length >= 3) {
            String parent = extractParent(args, chatId);
            if (parent == null) {
                return "Parent category does not exist.";
            }
            String child = extractChild(args);
            return categoryService.addChildCategory(parent, child, chatId);
        } else {
            return "Invalid format. Use /addElement <parent> <child> or /addElement <root>.";
        }
    }

    private String extractParent(String[] args, Long chatId) {
        StringBuilder parentBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            parentBuilder.append(args[i]).append(" ");
            if (categoryService.categoryExists(parentBuilder.toString().trim(), chatId)) {
                return parentBuilder.toString().trim();
            }
        }
        return null;
    }

    private String extractChild(String[] args) {
        StringBuilder childBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            childBuilder.append(args[i]).append(" ");
        }
        return childBuilder.toString().trim();
    }

}
