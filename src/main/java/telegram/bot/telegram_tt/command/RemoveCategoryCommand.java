package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.facade.CategoryFacade;
import telegram.bot.telegram_tt.service.CategoryService;
import lombok.extern.slf4j.Slf4j;

/**
 * Команда для удаления категории из базы данных.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RemoveCategoryCommand implements Command {

    private final CategoryFacade categoryFacade;

    /**
     * Выполняет команду удаления категории.
     *
     * @param command полный текст команды
     * @param chatId идентификатор чата пользователя
     * @return ответное сообщение, указывающее на успех или неудачу
     */
    @Override
    public String execute(String command, Long chatId) {
        log.info("Executing remove category command for chat ID: {}", chatId);

        String[] args = command.split(" ");
        if (args.length >= 2) {
            // Формируем строку для удаления категории, начиная с первого элемента после команды
            StringBuilder forRemove = new StringBuilder(args[1].trim());
            forRemove.append(" ");

            // Добавляем оставшиеся элементы к строке для удаления
            for (int i = 2; i < args.length; i++) {
                String element = args[i].trim();
                forRemove.append(element).append(" ");
            }

            // Выполняем удаление категории
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
