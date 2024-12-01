package telegram.bot.telegram_tt.command;

// This is an interface for all commands, in the implementations you can see the implementation of the Command Pattern
public interface Command {
    Object execute(String command, Long chatId);
}
