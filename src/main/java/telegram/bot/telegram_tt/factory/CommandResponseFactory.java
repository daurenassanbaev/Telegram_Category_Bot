package telegram.bot.telegram_tt.factory;

// This is the base interface for the Factory Method Pattern
public interface CommandResponseFactory {
    String createResponse(String messageText);
}
