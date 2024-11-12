package telegram.bot.telegram_tt.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import telegram.bot.telegram_tt.service.CategoryUploadService;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.LinkedHashMap;

/**
 * Команда для загрузки категорий из Excel-файла.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UploadCommand implements FileCommand {

    private final CategoryUploadService categoryUploadService;

    /**
     * Выполняет команду загрузки категорий из файла.
     *
     * @param inputStream поток входного файла
     * @param chatId идентификатор чата пользователя
     * @return ответное сообщение, указывающее на успех или неудачу
     * @throws IOException если возникает ошибка при обработке файла
     */
    @Override
    public String executeFile(InputStream inputStream, Long chatId) throws IOException {
        log.info("Executing upload command for chat ID: {}", chatId);

        // Получаем категории из загруженного Excel файла
        LinkedHashMap<String, String> map = categoryUploadService.getCategoriesFromExcelFile(inputStream);
        log.debug("Categories loaded from file: {}", map);

        // Добавляем все категории в базу данных
        String response = categoryUploadService.addAllCategories(map, chatId);
        log.info("Categories successfully uploaded for chat ID: {}", chatId);
        return response;
    }

    /**
     * Этот метод не используется в данном контексте.
     *
     * @param command полный текст команды
     * @param chatId идентификатор чата пользователя
     * @return null
     */
    @Override
    public Object execute(String command, Long chatId) {
        log.warn("Execute command method not implemented. Command: {}", command);
        return null;
    }
}
