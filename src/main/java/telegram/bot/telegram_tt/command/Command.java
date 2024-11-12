package telegram.bot.telegram_tt.command;

public interface Command {
    Object execute(String command, Long chatId);
}
