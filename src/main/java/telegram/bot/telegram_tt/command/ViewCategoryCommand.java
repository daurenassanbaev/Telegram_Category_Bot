package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.service.CategoryService;
import lombok.extern.slf4j.Slf4j;

/**
 * Команда для просмотра всех категорий в формате дерева из базы данных.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCategoryCommand implements Command {

    private final CategoryService categoryService;

    /**
     * Выполняет команду для просмотра всех категорий.
     *
     * @param command полный текст команды
     * @param chatId идентификатор чата пользователя
     * @return возвращает все данные из базы данных в формате дерева
     */
    @Override
    public String execute(String command, Long chatId) {
        log.info("Executing View Category Command for chat ID: {}", chatId);

        // Получаем категории в виде дерева
        String categoryTree = categoryService.viewCategoryTree(chatId);
        log.debug("Category tree fetched for chat ID {}: {}", chatId, categoryTree);

        return categoryTree;
    }
}