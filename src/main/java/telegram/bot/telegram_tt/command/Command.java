package telegram.bot.telegram_tt.command;

// Это интерфейс для всех commands, в имплементациях можно увидеть реализацию Command Pattern
public interface Command {
    Object execute(String command, Long chatId);
}
