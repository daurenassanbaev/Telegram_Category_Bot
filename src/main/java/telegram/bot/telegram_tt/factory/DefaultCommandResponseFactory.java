package telegram.bot.telegram_tt.factory;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// Здесь используется Factory Method Pattern
@Component
public class DefaultCommandResponseFactory implements CommandResponseFactory {
    private static final String UPLOAD_MESSAGE = """
            ⚠️ При отправке убедитесь, что объекты уникальны. В колонке "Категория" не должно быть дубликатов. Формат листа должен быть, как на фотках.(2 фото означает, что имя таблицы должна быть "Category Tree") :
                        
            "-" - означает отсутствие родительской категории.
                        
            Пожалуйста, отправьте ваш файл, я с удовольствием обработаю его 😊
            """;
    private static final String RULES = """
            Команды:
                        
            1) /viewTree - 📜 Показать текущее дерево категорий в структурированном формате.
                        
            2) /addElement <название элемента> - ➕ Добавить элемент как корневой, если у него нет родителя.
                        
            3) /addElement <родительский элемент> <дочерний элемент> - ➕ Добавить дочерний элемент к существующему родителю. Если родителя нет, будет показано сообщение об ошибке.
                        
            4) /removeElement <название элемента> - 🗑️ Удалить указанный элемент и все его дочерние элементы. Если элемент не найден, будет показано сообщение об ошибке.
                        
            5) /help - ℹ️ Показать список команд и их описание.
                        
            6) /download - 📥 Скачать документ Excel с деревом категорий.
                        
            7) /upload - 📤 Загрузить документ Excel с деревом категорий и сохранить все элементы в базе данных.
            """;

    private static final String START_MESSAGE = """
            Добро пожаловать в Category Bot! 👋
            Этот бот позволяет создавать, просматривать и удалять дерево категорий.
            Напишите "/help", чтобы узнать, какие команды доступны.
            """;

    private static final String UNKNOWN_COMMAND = "❌ Неизвестная команда. Напишите /help, чтобы увидеть доступные команды.";

    private final Map<String, String> predefinedResponses = new HashMap<>();

    public DefaultCommandResponseFactory() {
        predefinedResponses.put("/start", START_MESSAGE);
        predefinedResponses.put("/help", RULES);
        predefinedResponses.put("/upload", UPLOAD_MESSAGE);
    }

    @Override
    public String createResponse(String messageText) {
        if (predefinedResponses.containsKey(messageText)) {
            return predefinedResponses.get(messageText);
        }

        return UNKNOWN_COMMAND;
    }
}
